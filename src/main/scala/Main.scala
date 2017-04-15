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
    println(size)
    

    // size depends on screen size
    val history = CountersHistory.empty(140)

    waitLoop {
      getCounter(interfaceName).foreach(data =>
        history += data
      )
    }


    // delwin(rxgraph)
    // delwin(txgraph)
    // delwin(rxstats)
    // delwin(txstats)
    deleteWindow(mainWindow)
    endWindow()
  } 
}
