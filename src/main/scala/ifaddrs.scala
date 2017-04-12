import scalanative.native._

@extern
object Ifaddrs {
  import IfaddrsH._

  def getifaddrs(ifap: Ptr[Ptr[Ifaddrs]]): CInt = extern
  def freeifaddrs(ifa: Ptr[Ifaddrs]): Unit = extern
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

  type Ifaddrs = CStruct7[
    Ptr[CInt],     // [1] ifa_next
    Ptr[CChar],    // [2] ifa_name
    CInt,          // [3] ifa_flags
    Ptr[SockAddr], // [4] ifa_addr
    Ptr[SockAddr], // [5] ifa_netmask
    Ptr[SockAddr], // [6] ifa_ifu
    Ptr[Byte]      // [7] ifa_data
  ]

  implicit class IfaddrsOps(val ptr: Ptr[Ifaddrs]) extends AnyVal {
    def next: Ptr[Ifaddrs] = (!(ptr._1)).cast[Ptr[Ifaddrs]]
    def name: Ptr[CChar] = !(ptr._2)
    def flags: SiocgifFlags = new SiocgifFlags(!(ptr._3))

    def iterator: Iterator[IfaddrsOps] = new Iterator[IfaddrsOps]{
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

  class SaFamily(val value: CInt)
  object SaFamily {
    final val AF_PACKET = new SaFamily(17)
    // ...
  }

  final val InterfaceNameSize = 14
  type _14 = Nat.Digit[Nat._1, Nat._4]

  type SockAddr = CStruct2[
    CInt,              // [1] sa_family
    CArray[CChar, _14] // [2] sa_data
  ]

  class SockAddrOps(val ptr: Ptr[SockAddr]) {
    def family: SaFamily = new SaFamily(!(ptr._1))
    def data: CArray[CChar, _14] = !(ptr._2)
  }
}