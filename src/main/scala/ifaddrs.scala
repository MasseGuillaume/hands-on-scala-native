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
      (value & other.value) == other.value
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
    Ptr[sockaddr], // [6] ifa_ifu
    Ptr[Byte]      // [7] ifa_data
  ]

  implicit class ifaddrsOps(ptr: Ptr[ifaddrs]) {
    def next: Ptr[ifaddrs] = (!(ptr._1)).cast[Ptr[ifaddrs]]
    def name: Ptr[CChar] = !(ptr._2)
    def flags: SiocgifFlags = new SiocgifFlags(!(ptr._3))
  }
  
  class SaFamily(val value: CInt)
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

  class sockaddrOps(val ptr: Ptr[sockaddr]) {
    def family: SaFamily = new SaFamily(!(ptr._1))
    def data: CArray[CChar, _14] = !(ptr._2)
  }
}