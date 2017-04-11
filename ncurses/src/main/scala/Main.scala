/*
http://www.paulgriffiths.net/program/c/srcs/curhellosrc.html
(c) Copyright Paul Griffiths 1999
*/

import scala.scalanative._, native._
import posix.unistd.sleep
import stdlib.exit

import ncurses._, ncursesh._

object Main {
  def main(args: Array[String]): Unit = {
      
    var mainwin: Ptr[Window] = null
    mainwin = initscr()
    if(mainwin == null) {
      exit(1)
    }

    mvaddstr(13, 33, c"Hello, world!")
    refresh()
    sleep(3.toUInt)

    delwin(mainwin)
    endwin()
    refresh()


  }
}