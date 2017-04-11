
import scalanative._
import native._

@extern
object posix {
  import posixh._
  def gettimeofday(tv: timeval, tz: Ptr[timezone]): Unit = extern
}

object posixh {
  type time_t = CLong
  type suseconds_t = CLong
  type _timeval = CStruct2[time_t, suseconds_t]

  type timezone = CStruct0

  object timeval {
    @inline def stackalloc: timeval =
      new timeval(native.stackalloc[_timeval])
  }

  class timeval(val ptr: Ptr[_timeval]) extends AnyVal {
    def tv_sec: time_t = !ptr._1
    def tv_usec: time_t = !ptr._2
  }
}

import posix._, posixh._

object Main {
  def main(args: Array[String]): Unit = {
    val tv = timeval.stackalloc
    gettimeofday(tv, null)
    println(s"time: ${tv.tv_sec}(s) ${tv.tv_usec}(us)")
  }
}