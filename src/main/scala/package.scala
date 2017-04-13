import scala.scalanative._, native._

package object nbwmon {
  def clear() = print("\033c")

  trait CUnsignedLongIsIntegral extends Integral[CUnsignedLong] {
    def plus(x: CUnsignedLong, y: CUnsignedLong): CUnsignedLong = x + y
    def minus(x: CUnsignedLong, y: CUnsignedLong): CUnsignedLong = x - y
    def times(x: CUnsignedLong, y: CUnsignedLong): CUnsignedLong = x * y
    def quot(x: CUnsignedLong, y: CUnsignedLong): CUnsignedLong = x / y
    def rem(x: CUnsignedLong, y: CUnsignedLong): CUnsignedLong = x % y
    def negate(x: CUnsignedLong): CUnsignedLong = -x
    def fromInt(x: Int): CUnsignedLong = x.toULong
    def toInt(x: CUnsignedLong): Int = x.toInt
    def toLong(x: CUnsignedLong): Long = x.toLong
    def toFloat(x: CUnsignedLong): Float = x.toFloat
    def toDouble(x: CUnsignedLong): Double = x.toDouble
  }

  trait CUnsignedLongOrdering extends Ordering[CUnsignedLong] {
    def compare(x: CUnsignedLong, y: CUnsignedLong): Int =
      if(x > y) 1
      else if(x < y) -1
      else 0
  }
  
  implicit object CUnsignedLongIsIntegral 
    extends CUnsignedLongIsIntegral
    with CUnsignedLongOrdering
}