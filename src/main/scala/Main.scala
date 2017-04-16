package nbwmon

import scala.scalanative._, native._
import stdlib.exit
import runtime.GC
import string._
import stdio._

import ncurses._, ncursesh._
import posix._, posixh._

import Network._

/*
== Plan ==
  [x] 1: refresh loop
  [x] 2: fing network interface (ifaddrs api)
  [x] 3: display bitrate
  [ ] 4: draw windows (ncurses api)
  [ ] 5: draw bitrate windows
*/

object Main {
  def waitLoop(body: => Unit): Unit = {
    var timer  = 0L
    var redraw = false
    val tv     = stackalloc[timeval]

    var key = 0
    while (key != 'q') {
      gettimeofday(tv, null)
      if (timer < tv.tv_sec) {
        timer = tv.tv_sec
        redraw = true
      }

      if (redraw) {
        body
        redraw = false
      }
      key = getch()
    }
  }

  def printGraphWindow(window: Ptr[Window],
                       title: String,
                       interfaceName: Option[String],
                       color: Option[Attribute],
                       history: CountersHistory,
                       way: Way): Unit = {

    val size = windowSize(window)
    eraseWindow(window)
    box(window, 0, 0)

    // fade out the left column
    mvwvline(window, 0, 1, '-', size.height - 1)

    
    val padding = 5
    mvwprintw(
      window,
      0, 
      size.width - padding - title.size,
      c"[ %s ]",
      toCString(title)
    )

    interfaceName.foreach{ name =>
      val text = s"[ snbwmon | interface: $name ]"
      val center = (size.width - text.size) / 2
      mvwprintw(window, 0, center, toCString(text));
    }

    (history.minimum(way), history.maximum(way)) match {
      case (Some(min), Some(max)) => {
        val (rate, unit) = showBytes(max)
        mvwprintw(window, 0, 1, c"[ %3.2f%s/s ]", rate, toCString(unit))
    
        color.foreach(c => attributeOn(window, c))


        // mvwaddch(window, i + 1, j + 2, '*')

        color.foreach(c => attributeOff(window, c))
      }
      case _ => ()
    }

    wnoutrefresh(window)
  }

  def showBytes(rate: Double): (Double, String) = {
    val si = Array(" B", "kB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB")
    val prefix = 1000.0

    var bytes = rate
    var i = 0
    while(bytes > prefix) {
      bytes = bytes / prefix
      i += 1
    }


    (bytes, si(i))
  }

  def printStatsWindow(window: Ptr[Window],
                       title: CString,
                       history: CountersHistory,
                       way: Way): Unit = {

    eraseWindow(window)
    box(window, 0, 0)

    mvwprintw(window, 0, 1, c"[ %s ]", title)

    val stats = List(
      ("Current", history.current(way)),
      ("Maximum", history.maximum(way)),
      ("Minimum", history.minimum(way)),
      ("Average", history.average(way)),
      ("Total  ", history.total(way))
    )

    val size = windowSize(window)

    stats.zipWithIndex.foreach{ case ((label, stat), i) =>
      val line = i + 1

      stat.map(showBytes).foreach{ case (rate, unit) =>
        mvwprintw(
          window,
          line,
          1,
          toCString("%s %12.2f %s/s"),
          toCString(label),
          rate,
          toCString(unit)
        )
      }

    }

    wnoutrefresh(window)
  }


  def main(args: Array[String]): Unit = {

    val interfaceName =
      findInterface match {
        case Some(name) => name
        case _ => {
          println("Cannot find running network interface")
          sys.exit(1)
        }
      }

    val mainWindow = initialzeScreen()
    setCursorVisibility(CursorVisibility.Invisible)
    noecho()
    timeout(10)

    val (green, red) = 
      if(hasColors()) {
        startColor()
        useDefaultColors()
        val greenIndex = 1.toShort
        val redIndex = 2.toShort
        initPair(greenIndex, foreground = Color.Green, background = Color.Transparent)
        initPair(redIndex, foreground = Color.Red, background = Color.Transparent)

        (Some(colorPair(greenIndex)), Some(colorPair(redIndex)))
      }
      else (None, None)


    val size = windowSize(stdscr)
    val history = CountersHistory.empty(size.width)

    val graphHeight = (size.height - 7) / 2
    val statsHeight = size.height - graphHeight * 2

    val rxGraph = newWindow(graphHeight, size.width, 0, 0)
    val txGraph = newWindow(graphHeight, size.width, graphHeight, 0)
    val rxStats = newWindow(statsHeight, size.width / 2, graphHeight * 2, 0)
    val txStats = newWindow(statsHeight, size.width - size.width / 2, graphHeight * 2, size.width / 2)

    waitLoop {
      getCounter(interfaceName).foreach(data =>
        history += data
      )

      printGraphWindow(rxGraph, "Received", Some(interfaceName), green, history, RX)
      printGraphWindow(txGraph, "Transmitted", None, red, history, TX)

      printStatsWindow(rxStats, c"Received", history, RX)
      printStatsWindow(txStats, c"Transmitted", history, TX)

      doupdate()
    }

    deleteWindow(rxGraph)
    deleteWindow(txGraph)
    deleteWindow(rxStats)
    deleteWindow(txStats)
    deleteWindow(mainWindow)
    endWindow()
  } 
}
