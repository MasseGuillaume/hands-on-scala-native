
import scalanative._
import native._

@extern
object posix {
  import posixh._
  def gettimeofday(tv: Ptr[timeval], tz: Ptr[timezone]): Unit = extern
}

object posixh {
  type time_t = CLong
  type suseconds_t = CLong
  type timeval = CStruct2[time_t, suseconds_t]
  type timezone = CStruct0

  implicit class timevalOps(val ptr: Ptr[timeval]) extends AnyVal {
    @inline def tv_sec: time_t = !(ptr._1)
    @inline def tv_usec: suseconds_t = !(ptr._2)

    @inline def tv_sec_=(v: time_t): Unit = !(ptr._1) = v
    @inline def tv_usec_=(v: suseconds_t): Unit = !(ptr._2) = v
  }
}

import posix._, posixh._

object Main {
  def main(args: Array[String]): Unit = {
    val tv = stackalloc[timeval]
    gettimeofday(tv, null)
    println(s"time: ${tv.tv_sec}(s) ${tv.tv_usec}(us)")
  }
}