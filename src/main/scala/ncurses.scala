package nbwmon

import scalanative.native._

// http://invisible-island.net/ncurses/man/ncurses.3x.html

@link("ncurses")
@extern
object ncurses {
  import ncursesh._

  @name("initscr")
  def initialzeScreen(): Ptr[Window] = extern

  @name("curs_set")
  def setCursorVisibility(visibility: CursorVisibility): CInt = extern

  @name("newwin")
  def newWindow(nlines: Int, ncols: Int, begin_y: Int, begin_x: Int): Ptr[Window] = extern

  @name("delwin")
  def deleteWindow(window: Ptr[Window]): Unit = extern

  @name("endwin")
  def endWindow(): Unit = extern

  @name("werase")
  def eraseWindow(window: Ptr[Window]): CInt = extern

  @name("wnoutrefresh")
  def refreshWindow(window: Ptr[Window]): CInt = extern

  def doupdate(): CInt = extern

  def box(window: Ptr[Window], verch: ChType, horch: ChType): CInt = extern

  @name("mvwprintw")
  def printFormatted(window: Ptr[Window], y: CInt, x: CInt, fmt: CString, args: CVararg*): CInt = extern

  @name("mvwaddch")
  def printChar(window: Ptr[Window], y: CInt, x: CInt, ch: ChType): CInt = extern

  @name("mvwvline")
  def printLine(WINDwindow: Ptr[Window], y: CInt, x: CInt, ch: ChType, n: CInt): CInt = extern

  def noecho(): Unit = extern

  def getch(): CInt = extern

  def timeout(delay: CInt): Unit = extern

  @name("has_colors")
  def hasColors(): Boolean = extern

  @name("start_color")
  def startColor(): Unit = extern

  @name("use_default_colors")
  def useDefaultColors(): Unit = extern

  @name("wattron")
  def attributeOn(window: Ptr[Window], attribute: Attribute): CInt = extern

  @name("wattroff")
  def attributeOff(window: Ptr[Window], attribute: Attribute): CInt = extern

  @name("init_pair")
  def initPair(pair: CShort, foreground: Color, background: Color): CInt = extern

  @name("COLOR_PAIR")
  def colorPair0(pair: CShort): CInt = extern

  @name("getmaxx")
  def getmaxx0(window: Ptr[Window]): Int = extern

  @name("getmaxy")
  def getmaxy0(window: Ptr[Window]): Int = extern

  val stdscr: Ptr[Window] = extern
}

object ncursesh {

  type ChType = CChar

  case class Size(width: Int, height: Int)

  def colorPair(pair: CShort): Attribute = new Attribute(ncurses.colorPair0(pair))

  def windowSize(window: Ptr[Window]): Size = {
    val width = ncurses.getmaxx0(window)
    val height = ncurses.getmaxy0(window)
    Size(width, height)
  }

  type Window = CStruct0

  class CursorVisibility(val value: CInt) extends AnyVal
  object CursorVisibility {
    final val Invisible = new CursorVisibility(0)
    final val Visible = new CursorVisibility(1)
    final val VeryVisible = new CursorVisibility(2)
  }

  class Color(val value: CInt) extends AnyVal
  object Color {
    final val Transparent = new Color(-1)
    final val Black   = new Color(0)
    final val Red     = new Color(1)
    final val Green   = new Color(2)
    final val Yellow  = new Color(3)
    final val Blue    = new Color(4)
    final val Magenta = new Color(5)
    final val Cyan    = new Color(6)
    final val White   = new Color(7)
  }
  class Attribute(val value: CInt) extends AnyVal
}
