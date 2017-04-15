package nbwmon

import scala.scalanative._, native._

import Ifaddrs._, IfaddrsH._
import collection.mutable.Queue

sealed trait Way
case object RX extends Way
case object TX extends Way

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

  def apply(i: Int): Option[Counters] = 
    if(i >= queue.size) None
    else Some(queue(i))
  
  def current(f: Counters => CUnsignedLong): Option[Double] =
    lastElement.map(f).map(_.toDouble)

  def maximum(f: Counters => CUnsignedLong): Option[Double] = 
    stats(q => f(q.maxBy(f)).toDouble)

  def minimum(f: Counters => CUnsignedLong): Option[Double] =
    stats(q => f(q.minBy(f)).toDouble)

  def total(f: Counters => CUnsignedLong): Option[Double] =
    stats(q => q.map(f).sum.toDouble)

  def average(f: Counters => CUnsignedLong): Option[Double] =
    total(f).map(_ / queue.size.toDouble)

  private def stats(statsF: Queue[Counters] => Double): Option[Double] = {
    if(queue.isEmpty) None
    else Some(statsF(queue))
  }
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