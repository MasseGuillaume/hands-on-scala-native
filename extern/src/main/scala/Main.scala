import scalanative.native._

object Main {
  def main(args: Array[String]): Unit = {
    println(lib.add(1, 1))
  } 
}

@extern
object lib {
  def add(a: Int, b: Int): Int = extern
}
