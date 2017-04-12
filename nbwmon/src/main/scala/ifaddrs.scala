import scalanative.native._

@extern
object ifaddrs {
  import ifaddrsh._

  def getifaddrs(ifap: Ptr[Ptr[ifaddrs]]): CInt = extern
  def freeifaddrs(ifa: Ptr[ifaddrs]): Unit = extern
}

object ifaddrsh {
  class SiocgifFlags(val value: CInt) extends AnyVal {
    def &(other: SiocgifFlags): Boolean = 
      (value & other.value) == 1

    override def toString: String = {
      import SiocgifFlags._

      List(
        s"value: $value",
        s"Up: ${this & Up}",
        s"Loopback: ${this & Loopback}",
        s"Running: ${this & Running}"
      ).mkString(", ")
    }
  }
  object SiocgifFlags {
    final val Up       = new SiocgifFlags(1 << 1)
    final val Loopback = new SiocgifFlags(1 << 4)
    final val Running  = new SiocgifFlags(1 << 6)
    // ...
  }

  type ifaddrs = CStruct7[
    Ptr[CInt],     // [1] ifa_next
    Ptr[CChar],    // [2] ifa_name
    CInt,          // [3] ifa_flags
    Ptr[sockaddr], // [4] ifa_addr
    Ptr[sockaddr], // [5] ifa_netmask
    Ptr[sockaddr], // [6] ifa_ifu union ifu_broadaddr | ifu_dstaddr
    Ptr[Byte]      // [7] ifa_data
  ]

  implicit class ifaddrsOps(val ptr: Ptr[ifaddrs]) extends AnyVal {
    @inline def next: Ptr[ifaddrs] = (!(ptr._1)).cast[Ptr[ifaddrs]]
    @inline def name: Ptr[CChar] = !(ptr._2)
    @inline def flags: SiocgifFlags = new SiocgifFlags(!(ptr._3))
    @inline def addr: Ptr[sockaddr] = !(ptr._4)
    @inline def netmask: Ptr[sockaddr] = !(ptr._5)
    @inline def ifu: Ptr[sockaddr] = !(ptr._6)
    @inline def broadaddr: Ptr[sockaddr] = ifu
    @inline def dstaddr: Ptr[sockaddr] = ifu
    @inline def data: Ptr[Byte] = !(ptr._7)
  }
  
  class SaFamily(val value: CInt) extends AnyVal
  object SaFamily {
    final val AF_PACKET = new SaFamily(17)
    // ...
  }

  final val InterfaceNameSize = 14
  type _14 = Nat.Digit[Nat._1, Nat._4]

  type sockaddr = CStruct2[
    CInt,              // [1] sa_family
    CArray[CChar, _14] // [2] sa_data
  ]

  implicit class sockaddrOps(val ptr: Ptr[sockaddr]) extends AnyVal {
    @inline def family: SaFamily = new SaFamily(!(ptr._1))
    @inline def data: CArray[CChar, _14] = !(ptr._2)
  }
}