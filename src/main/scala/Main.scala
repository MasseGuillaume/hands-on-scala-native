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
  [x] 2: find network interface (ifaddrs api)
  [x] 3: display bitrate
  [x] 4: draw windows (ncurses api)
  [x] 5: draw bitrate windows
*/

object Main {
  def waitLoop(ci: Boolean)(body: => Unit): Unit = {
    var timer  = 0L
    var redraw = false
    val tv     = stackalloc[timeval]
    var i = 0
    var ciOk = true
    var key = 0

    while (key != 'q' && ciOk) {
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
      if(ci && redraw) {
        i += 1
        ciOk = i < 10
      }
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
    printLine(window, 0, 1, '-', size.height - 1)

    val padding = 5
    printFormatted(
      window,
      0, 
      size.width - padding - title.size,
      c"[ %s ]",
      toCString(title)
    )

    interfaceName.foreach{ name =>
      val text = s"[ snbwmon | interface: $name ]"
      val center = (size.width - text.size) / 2
      printFormatted(window, 0, center, toCString(text));
    }

    history.maximum(way).foreach{ max =>
      val (rate, unit) = showBytes(max)
  
      printFormatted(window, 0, 1, c"[%.2f %s/s]", rate, toCString(unit))

      color.foreach(c => attributeOn(window, c))

      history.getQueue(way).reverse.zipWithIndex.foreach{ case (value, i) =>
        val col = size.width - i - 2

        val border = 2
        val h = Math.ceil(value.toDouble / max.toDouble * (size.height - border).toDouble)

        var j = size.height - 2
        var jj = 0
        while(j > 0 && jj < h) {
          printChar(window, j, col, '*')
          j -= 1
          jj += 1
        }
      }

      color.foreach(c => attributeOff(window, c))
    }

    refreshWindow(window)
  }

  def showBytes(rate: Double): (Double, String) = {
    val si = Array(" ", "k", "M", "G", "T")
    val prefix = 1000.0

    var bytes = rate
    var i = 0
    while(bytes > prefix) {
      bytes = bytes / prefix
      i += 1
    }

    (bytes, si(i) + "B")
  }

  def printStatsWindow(window: Ptr[Window],
                       title: CString,
                       history: CountersHistory,
                       way: Way): Unit = {

    eraseWindow(window)
    box(window, 0, 0)

    printFormatted(window, 0, 1, c"[ %s ]", title)

    val stats = List(
      ("Current", history.current(way), true),
      ("Maximum", history.maximum(way), true),
      ("Average", history.average(way), true),
      ("Minimum", history.minimum(way), true),
      ("Total  ", history.total(way), false)
    )

    val size = windowSize(window)

    stats.zipWithIndex.foreach{ case ((label, stat, isRate), i) =>
      val line = i + 1

      stat.map(showBytes).foreach{ case (value, unit) =>
        val fmt = "%s %12.2f %s" + (if(isRate) "/s" else "")
        printFormatted(
          window,
          line,
          1,
          toCString(fmt),
          toCString(label),
          value,
          toCString(unit)
        )
      }
    }

    refreshWindow(window)
  }


  def main(args: Array[String]): Unit = {

    val ci = !args.isEmpty 

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
    

    val graphHeight = (size.height - 7) / 2
    val statsHeight = size.height - graphHeight * 2

    val history = CountersHistory.empty(size.width - 4)

    val rxGraph = newWindow(graphHeight, size.width, 0, 0)
    val txGraph = newWindow(graphHeight, size.width, graphHeight, 0)
    val rxStats = newWindow(statsHeight, size.width / 2, graphHeight * 2, 0)
    val txStats = newWindow(statsHeight, size.width - size.width / 2, graphHeight * 2, size.width / 2)

    waitLoop(ci) {
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
