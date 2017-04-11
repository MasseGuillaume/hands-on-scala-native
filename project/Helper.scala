import scala.scalanative.sbtplugin.ScalaNativePlugin.autoImport._
import java.io.File
import sbt._, Keys._

object Helper {
  private def abs(file: File): String = file.getAbsolutePath

  val cCompile = Seq(
    compile in Compile := {
      val cDir = (sourceDirectory in Compile).value / "c"
      val out = target.value / "clib"
      val clang = nativeClang.value
      val logger = streams.value.log

      out.mkdirs()

      val cCompileSuccess = 
        (cDir ** "*.c").get.par.map{path =>
          val args = Seq(
              abs(clang), 
              "-c", abs(path),
              "-o", abs(out / path.getName) + ".o"
            )

          Process(
            args,
            Some(cDir)
          ) ! logger
        }
        .seq
        .forall(_ == 0)

      if(cCompileSuccess) {
        (compile in Compile).value
      } else {
        sys.error("C Compilation failed")
      }
    },
    nativeLinkingOptions ++= {
      val cDir = (sourceDirectory in Compile).value / "c"
      val out = target.value / "clib"
      (cDir ** "*.c").get.map(path =>
        abs(out / path.getName) + ".o"
      )
    }
  )
}