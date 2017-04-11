lazy val sharedSettings = Seq(
  scalaVersion := "2.11.8"
)

lazy val `hands-on-scala-native` = project
  .in(file("."))
  .settings(sharedSettings)
  .dependsOn(extern, ncurses)
  .aggregate(extern, ncurses)

lazy val extern = project
  .in(file("extern"))
  .enablePlugins(ScalaNativePlugin)
  .settings(sharedSettings)
  .settings(nativeLinkingOptions += "lib.o")

lazy val ncurses = project
  .in(file("ncurses"))
  .enablePlugins(ScalaNativePlugin)
  .settings(sharedSettings)