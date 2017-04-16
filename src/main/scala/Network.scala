package nbwmon

import scala.scalanative._, native._

import Ifaddrs._, IfaddrsH._
import collection.mutable.Queue

sealed trait Way
case object RX extends Way
case object TX extends Way

object CountersHistory {
  def empty(maxSize: Int): CountersHistory =
    new CountersHistory(
      maxSize,
      None,
      None,
      Queue.empty[CUnsignedLong],
      Queue.empty[CUnsignedLong]
    )
}

class CountersHistory private(maxSize: Int,
                              private var lastTotal: Option[Counters],
                              private var lastElement: Option[Counters],
                              txQueue: Queue[CUnsignedLong],
                              rxQueue: Queue[CUnsignedLong]){
  def +=(v: Counters): this.type = {
    lastTotal.foreach{ lv =>
      if(txQueue.size > maxSize) {
        txQueue.dequeue()
      }

      if(rxQueue.size > maxSize) {
        rxQueue.dequeue()
      }

      val t = v - lv
      lastElement = Some(t)
      rxQueue += t.rx
      txQueue += t.tx
    }

    lastTotal = Some(v)

    this
  }

  def getQueue(way: Way): Queue[CUnsignedLong] = 
    way match {
      case RX => rxQueue
      case TX => txQueue
    }

  private def l(way: Way)(c: Counters): CUnsignedLong =
    way match {
      case RX => c.rx
      case TX => c.tx
    }

  def current(way: Way): Option[Double] =
    lastElement.map(l(way)).map(_.toDouble)

  def maximum(way: Way): Option[Double] =
    stats(way, _.max.toDouble)

  def minimum(way: Way): Option[Double] =
    stats(way, _.min.toDouble)

  def total(way: Way): Option[Double] =
    lastTotal.map(l(way)).map(_.toDouble)
    
  def average(way: Way): Option[Double] =
    stats(way, q => q.sum.toDouble / q.size.toDouble)

  private def stats(way: Way, statsF: Queue[CUnsignedLong] => Double): Option[Double] = {
    val queue = getQueue(way)

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