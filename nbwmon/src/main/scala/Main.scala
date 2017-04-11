package nbwmon

import scala.scalanative._, native._
import stdlib.exit

import ncurses._, ncursesh._
import posix._, posixh._

object Main {
  def main(args: Array[String]): Unit = {
    var timer = 0L
    var redraw = false
    val tv = stackalloc[timeval]

    while(true) {
      gettimeofday(tv, null)
      if (timer < tv.tv_sec) {
        timer = tv.tv_sec
        redraw = true;
      }

      if(redraw) {
        println("draw")
        redraw = false
      }
    }
  }
}