package nbwmon

import scalanative.native._

// scala-native#104 should provide those bindings
@extern
object posix {
  import posixh._
  def gettimeofday(tv: Ptr[timeval], tz: Ptr[timezone]): Unit = extern
}

object posixh {
  type time_t      = CLong
  type suseconds_t = CLong
  type timeval     = CStruct2[time_t, suseconds_t]
  type timezone    = CStruct0

  implicit class timevalOps(val ptr: Ptr[timeval]) extends AnyVal {
    def tv_sec: time_t       = !(ptr._1)
    def tv_usec: suseconds_t = !(ptr._2)
  }
}
