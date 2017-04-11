lazy val sharedSettings = Seq(
  scalaVersion := "2.11.8"
)

lazy val `hands-on-scala-native` = project
  .in(file("."))
  .settings(sharedSettings)
  .dependsOn(extern, ncurses, stackalloc, nbwmon)
  .aggregate(extern, ncurses, stackalloc, nbwmon)

lazy val extern = project
  .enablePlugins(ScalaNativePlugin)
  .settings(sharedSettings)
  .settings(nativeLinkingOptions += "lib.o")

lazy val ncurses = project
  .enablePlugins(ScalaNativePlugin)
  .settings(sharedSettings)

lazy val stackalloc = project
  .enablePlugins(ScalaNativePlugin)
  .settings(sharedSettings)

lazy val nbwmon = project
  .enablePlugins(ScalaNativePlugin)
  .settings(sharedSettings)