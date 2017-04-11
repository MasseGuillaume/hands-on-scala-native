package nbwmon

import scala.scalanative._, native._
import posix.unistd.sleep
import stdlib.exit

import ncurses._, ncursesh._

import nbwmon.

object Main {
  def main(args: Array[String]): Unit = {
    val tv = stackalloc[timeval]
    gettimeofday(tv, null)
    println(s"time: ${tv.tv_sec}(s) ${tv.tv_usec}(us)")
  }
}