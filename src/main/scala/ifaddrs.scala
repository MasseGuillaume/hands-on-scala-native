import scalanative.native._
import scalanative.runtime.undefined

@extern
object Ifaddrs {
  import IfaddrsH._

  def getifaddrs(ifap: Ptr[Ptr[Ifaddrs]]): CInt = extern
  def freeifaddrs(ifa: Ptr[Ifaddrs]): Unit      = extern
}

object IfaddrsH {
  class SiocgifFlags(val value: CInt) extends AnyVal {
    def &(other: SiocgifFlags): Boolean =
      (value & other.value) == other.value
  }
  object SiocgifFlags {
    final val Up       = new SiocgifFlags(1 << 1)
    final val Loopback = new SiocgifFlags(1 << 4)
    final val Running  = new SiocgifFlags(1 << 6)
    // ...
  }

  // format: off
  type Ifaddrs = CStruct7[
    // scala-native#634 should be Ptr[Ifaddrs]
    Ptr[CInt],     // [1] ifa_next
    Ptr[CChar],    // [2] ifa_name
    CInt,          // [3] ifa_flags
    Ptr[SockAddr], // [4] ifa_addr
    Ptr[SockAddr], // [5] ifa_netmask
    Ptr[SockAddr], // [6] ifa_ifu
    Ptr[Byte]      // [7] ifa_data
  ]
  // format: on

  implicit class IfaddrsOps(val ptr: Ptr[Ifaddrs]) extends AnyVal {
    // scala-native#634
    // format: off
    def next: Ptr[Ifaddrs]  = (!(ptr._1)).cast[Ptr[Ifaddrs]]
    def name: Ptr[CChar]    = !(ptr._2)
    def flags: SiocgifFlags = new SiocgifFlags(!(ptr._3))
    def addr: Ptr[SockAddr] = !(ptr._4)
    def data: Ptr[Byte]     = !(ptr._7)
    // format: on

    def iterator: Iterator[IfaddrsOps] = new Iterator[IfaddrsOps] {
      private var current = new IfaddrsOps(ptr)

      def hasNext: Boolean = {
        current.ptr.next != null
      }

      def next(): IfaddrsOps = {
        current = new IfaddrsOps(current.next)
        current
      }
    }
  }

  // format: off
  type RtnlLinkStats = CStruct22[
    CUnsignedInt, //  [1] rx_packets
    CUnsignedInt, //  [2] tx_packets
    CUnsignedInt, //  [3] rx_bytes
    CUnsignedInt, //  [4] tx_bytes
    CUnsignedInt, //  [5] rx_errors
    CUnsignedInt, //  [6] tx_errors
    CUnsignedInt, //  [7] rx_dropped
    CUnsignedInt, //  [8] tx_dropped
    CUnsignedInt, //  [9] multicast
    CUnsignedInt, // [10] collisions
    CUnsignedInt, // [11] rx_length_errors
    CUnsignedInt, // [12] rx_over_errors
    CUnsignedInt, // [13] rx_crc_errors
    CUnsignedInt, // [14] rx_frame_errors
    CUnsignedInt, // [15] rx_fifo_errors
    CUnsignedInt, // [16] rx_missed_errors
    CUnsignedInt, // [17] tx_aborted_errors
    CUnsignedInt, // [18] tx_carrier_errors
    CUnsignedInt, // [19] tx_fifo_errors
    CUnsignedInt, // [20] tx_heartbeat_errors
    CUnsignedInt, // [21] tx_window_errors
    CUnsignedInt  // [22] rx_compressed

    // we are limited to 22 fields scala-native#637
    // it's ok to ignore those since we don't allocate RtnlLinkStats64
    // CUnsignedInt, // [23] tx_compressed
    // CUnsignedInt  // [24] rx_nohandler
  ]
  // format: on

  implicit class RtnlLinkStatsOps(val ptr: Ptr[RtnlLinkStats]) extends AnyVal {
    def rxPackets: CUnsignedInt         = !(ptr._1)
    def txPackets: CUnsignedInt         = !(ptr._2)
    def rxBytes: CUnsignedInt           = !(ptr._3)
    def txBytes: CUnsignedInt           = !(ptr._4)
    def rxErrors: CUnsignedInt          = !(ptr._5)
    def txErrors: CUnsignedInt          = !(ptr._6)
    def rxDropped: CUnsignedInt         = !(ptr._7)
    def txDropped: CUnsignedInt         = !(ptr._8)
    def multicast: CUnsignedInt         = !(ptr._9)
    def collisions: CUnsignedInt        = !(ptr._10)
    def rxLengthErrors: CUnsignedInt    = !(ptr._11)
    def rxOverErrors: CUnsignedInt      = !(ptr._12)
    def rxCrcErrors: CUnsignedInt       = !(ptr._13)
    def rxFrameErrors: CUnsignedInt     = !(ptr._14)
    def rxFifoErrors: CUnsignedInt      = !(ptr._15)
    def rxMissedErrors: CUnsignedInt    = !(ptr._16)
    def txAbortedErrors: CUnsignedInt   = !(ptr._17)
    def txCarrierErrors: CUnsignedInt   = !(ptr._18)
    def txFifoErrors: CUnsignedInt      = !(ptr._19)
    def txHeartbeatErrors: CUnsignedInt = !(ptr._20)
    def txWindowErrors: CUnsignedInt    = !(ptr._21)
    def rxCompressed: CUnsignedInt      = !(ptr._22)

    // def txCompressed: CUnsignedInt = !(ptr._23),
    // def rxNohandler: CUnsignedInt = !(ptr._24)
  }

  class SaFamily(val value: CInt) extends AnyVal
  object SaFamily {
    final val Packet = new SaFamily(17)
    // ...
  }

  final val InterfaceNameSize = 14
  type _14 = Nat.Digit[Nat._1, Nat._4]

  // format: off
  type SockAddr = CStruct2[
    CInt,              // [1] sa_family
    CArray[CChar, _14] // [2] sa_data
  ]
  // format: on

  implicit class SockAddrOps(val ptr: Ptr[SockAddr]) {
    def family: SaFamily         = new SaFamily(!(ptr._1))
    def data: CArray[CChar, _14] = !(ptr._2)
  }
}
