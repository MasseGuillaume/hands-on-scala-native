import scalanative.native._

@link("ncurses")
@extern
object ncurses {
  import ncursesh._

  def initscr(): Ptr[Window] = extern
  def mvaddstr(x: CInt, y: CInt, msg: CString): Unit = extern
  def delwin(window: Ptr[Window]): Unit = extern
  def endwin(): Unit = extern
  def refresh(): Unit = extern
}

object ncursesh {
  type Window = CStruct0
} 