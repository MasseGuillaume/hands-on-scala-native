import scala.scalanative._, native._
import stdlib.exit
import runtime.GC
import string._
import stdio._

import ncurses._, ncursesh._
import posix._, posixh._
import ifaddrs._, ifaddrsh._

/*

== Plan ==

  [1] refresh loop
  [2] fing network interface
     * ifaddrs api
  [3] display bitrate
  [4] draw windows
     * ncurses api 
  [5] draw bitrate windows

*/

object Main {
  def main(args: Array[String]): Unit = {

    println(findInterface())

    // var timer = 0L
    // var redraw = false
    // val tv = stackalloc[timeval]

    // while(true) {
    //   gettimeofday(tv, null)
    //   if (timer < tv.tv_sec) {
    //     timer = tv.tv_sec
    //     redraw = true;
    //   }

    //   if(redraw) {
    //     println("draw")
    //     redraw = false
    //   }
    // }
  }

  def findInterface(): Option[CString] = {
    val ifaddrs = stackalloc[Ptr[ifaddrs]]
    var ifa = stackalloc[ifaddrs]

    if (getifaddrs(ifaddrs) != -1) {
      ifa = !ifaddrs

      var found = false
      while(ifa != null && !found) {
        import SiocgifFlags._
        
        fwrite(ifa.name, sizeof[CChar], InterfaceNameSize, stdout)
        println(": " + ifa.flags)

        if((ifa.flags & Up) &&
           (ifa.flags & Running) &&
          !(ifa.flags & Loopback)
        ) {
          println("found")
          found = true
        }
        else {
          ifa = ifa.next
        }
      }

      val ret =
        if(found) {
          None
          // val cstr = GC.malloc(sizeof[CChar] * InterfaceNameSize).cast[Ptr[CChar]]
          // Some(strncpy(cstr, ifa.name, InterfaceNameSize))
        }
        else None

      freeifaddrs(!ifaddrs)

      ret
    } else {
      None
    }
  }
}