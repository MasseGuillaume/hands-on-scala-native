import scalanative.native._
import lib._

object Main {
  def main(args: Array[String]): Unit = {
    println(add(1, 1))
  } 
}

@extern
object lib {
  def add(a: Int, b: Int): Int = extern
}
