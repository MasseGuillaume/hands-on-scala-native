lazy val sharedSettings = Seq(
  scalaVersion := "2.11.8"
)

lazy val extern = project
  .in(file("extern"))
  .enablePlugins(ScalaNativePlugin)
  .settings(sharedSettings)
  .settings(Helper.cCompile)
