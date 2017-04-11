lazy val sharedSettings = Seq(
  scalaVersion := "2.11.8"
)

lazy val `hands-on-scala-native` = project
  .in(file("."))
  .settings(sharedSettings)

lazy val extern = project
  .in(file("extern"))
  .enablePlugins(ScalaNativePlugin)
  .settings(sharedSettings)
  .settings(
    nativeLinkingOptions += {
      ((baseDirectory in ThisBuild).value / "lib.o").getAbsolutePath
    }
  )
