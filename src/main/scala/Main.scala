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

    while (true) {
      gettimeofday(tv, null)
      if (timer < tv.tv_sec) {
        timer = tv.tv_sec
        redraw = true
      }

      if (redraw) {
        body
        redraw = false
      }
    }
  }

  def printGraphWindow(window: Ptr[Window],
                       title: CString,
                       interfaceName: Option[String],
                       color: Option[Attribute],
                       // size: Size,
                       history: CountersHistory): Unit = {

  }

  def showBytes(rate: Double): String = {
    val si = Array("B", "kB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB")
    val prefix = 1000.0

    var bytes = rate
    var i = 0
    while(bytes > prefix) {
      bytes = bytes / prefix
      i += 1
    }

    bytes + " " + si(i) + "/s"
  }

  def printStatsWindow(window: Ptr[Window],
                       title: CString,
                       history: CountersHistory,
                       project: Counters => CUnsignedLong): Unit = {

    eraseWindow(window)
    box(window, 0, 0)

    mvwprintw(window, 0, 1, c"[ %s ]", title)

    val stats = List(
      ("Current", history.current(project)),
      ("Maximum", history.maximum(project)),
      ("Minimum", history.minimum(project)),
      ("Average", history.average(project)),
      ("Total", history.total(project))
    )

    stats.zipWithIndex.foreach{ case ((label, stat), line) => 
      mvwprintw(
        window,
        line + 1,
        1, 
        toCString(label + stat.map(showBytes).getOrElse("Not Available"))
      )
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
    setCursorVisibility(CursorVisibility.Visible)
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

      // printGraphWindow(rxGraph, c"Received", Some(interfaceName), green, history)
      // printGraphWindow(txGraph, c"Transmitted", None, red, history)

      printStatsWindow(rxGraph, c"Received", history, _.rx)
      printStatsWindow(txGraph, c"Transmitted", history, _.tx)

      printStatsWindow(rxStats, c"Received", history, _.rx)
      printStatsWindow(txStats, c"Transmitted", history, _.tx)

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
