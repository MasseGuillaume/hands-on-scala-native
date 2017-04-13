package nbwmon

import scala.scalanative._, native._

import Ifaddrs._, IfaddrsH._
import collection.mutable.Queue

object CountersHistory {
  def empty(maxSize: Int): CountersHistory =
    new CountersHistory(maxSize, None, None, Queue.empty[Counters])
}

class CountersHistory private(maxSize: Int,
                              private var lastTotal: Option[Counters],
                              private var lastElement: Option[Counters],
                              queue: Queue[Counters]){
  def +=(v: Counters): this.type = {
    lastTotal.foreach{lv =>
      if(queue.size > maxSize) {
        queue.dequeue()
      }
      val t = v - lv
      lastElement = Some(t)
      queue += t
    }

    lastTotal = Some(v)

    this
  }

  def print(): Unit = {
    rxCurrent match {
      case Some(current) => {
        val si = Array("B", "kB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB")
        val prefix = 1000.0

        var bytes = current.toDouble
        var i = 0
        while(bytes > prefix) {
          bytes = bytes / prefix
          i += 1
        }

        clear()
        println(bytes + " " + si(i) + "/s")
      }
      case _ => () 
    }
  }

  def rxCurrent: Option[CUnsignedLong] = lastElement.map(_.rx)

  def rxMaximum: Option[CUnsignedLong] = 
    if(queue.isEmpty) None 
    else Some(queue.maxBy(_.rx).rx)

  def rxMinimum: Option[CUnsignedLong] =
    if(queue.isEmpty) None
    else Some(queue.minBy(_.rx).rx)

  def rxAverage: Option[Double] =
    if(queue.isEmpty) None
    else Some((queue.map(_.rx).sum.toDouble / queue.size.toDouble))
}

case class Counters(val rx: CUnsignedLong, val tx: CUnsignedLong) {
  def -(other: Counters): Counters = {
    Counters(rx - other.rx, tx - other.tx)
  }
}

object Counters {
  def apply(stats: Ptr[RtnlLinkStats]): Counters =
    Counters(stats.rxBytes, stats.txBytes)
}

object Network {
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