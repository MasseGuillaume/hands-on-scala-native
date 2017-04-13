import scala.scalanative._, native._
import stdlib.exit
import runtime.GC
import string._
import stdio._

import ncurses._, ncursesh._
import posix._, posixh._
import Ifaddrs._, IfaddrsH._

/*
== Plan ==
  [1] refresh loop
  [2] fing network interface (ifaddrs api)
  [3] display bitrate
  [4] draw windows (ncurses api)
  [5] draw bitrate windows
*/

object Main {
  def main(args: Array[String]): Unit = {
    print("\033c")

    val interfaceName =
      findInterface match {
        case Some(name) => name
        case _ => {
          println("Cannot find running network interface")
          sys.exit(1)
        }
      }

    println(interfaceName)

    var timer  = 0L
    var redraw = false
    val tv     = stackalloc[timeval]

    while (true) {
      gettimeofday(tv, null)
      if (timer < tv.tv_sec) {
        timer = tv.tv_sec
        redraw = true;
      }

      if (redraw) {
        println(getCounter(interfaceName))
        redraw = false
      }
    }
  }

  case class Counters(
      rx: CUnsignedLong,
      tx: CUnsignedLong
  )

  object Counters {
    def apply(stats: Ptr[RtnlLinkStats]): Counters =
      Counters(stats.rxBytes, stats.txBytes)
  }

  def getCounter(interfaceName: String): Option[Counters] = {
    import SaFamily.Packet

    withIfaddrs(
      _.find(
        ifa =>
          fromCString(ifa.name) == interfaceName &&
            ifa.addr != null &&
            ifa.addr.family == Packet)
        .map(ifa => Counters(ifa.data.cast[Ptr[RtnlLinkStats]]))
    )
  }

  def findInterface: Option[String] = {
    import SiocgifFlags._

    withIfaddrs(
      _.find(
        ifa =>
          (ifa.flags & Up) &&
            (ifa.flags & Running) &&
            !(ifa.flags & Loopback))
        .map(ifa => fromCString(ifa.name))
    )
  }

  def withIfaddrs[A](f: Iterator[IfaddrsOps] => A): A = {
    val ifaddrs = stackalloc[Ptr[Ifaddrs]]
    if (getifaddrs(ifaddrs) != -1) {
      val ret = f((!ifaddrs).iterator)
      freeifaddrs(!ifaddrs)
      ret
    } else {
      println("failed to getifaddrs")
      sys.exit(1)
    }
  }
}
