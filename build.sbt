def removeSnapshot(str: String): String = if (str.endsWith("-SNAPSHOT")) str.substring(0, str.length - 9) else str
def katLibDependecy(module: String) = "com.github.Katrix-.KatLib" % s"katlib-$module" % "f8003fc5f512ce58dc81fab8e180fd27f05f7904" % Provided

def deployKeySetting = oreDeploymentKey := (oreDeploymentKey in Scope.Global).?.value.flatten

lazy val commonSettings = Seq(
  name := s"HomeSweetHome-${removeSnapshot(spongeApiVersion.value)}",
  organization := "io.github.katrix",
  version := "2.2.2",
  scalaVersion := "2.12.2",
  resolvers += "jitpack" at "https://jitpack.io",
  libraryDependencies += katLibDependecy("shared"),
  libraryDependencies += "org.jetbrains" % "annotations" % "15.0" % Provided,
  scalacOptions ++= Seq(
    "-deprecation",
    "-feature",
    "-unchecked",
    "-Xlint",
    "-Yno-adapted-args",
    "-Ywarn-dead-code",
    "-Ywarn-unused-import"
  ),
  crossPaths := false,
  assemblyShadeRules in assembly := Seq(
    ShadeRule.rename("scala.**"     -> "io.github.katrix.katlib.shade.scala.@1").inAll,
    ShadeRule.rename("shapeless.**" -> "io.github.katrix.katlib.shade.shapeless.@1").inAll
  ),
  autoScalaLibrary := false,
  assemblyJarName := s"${name.value}-assembly-${version.value}.jar",
  spongePluginInfo := spongePluginInfo.value.copy(
    id = "homesweethome",
    name = Some("HomeSweetHome"),
    version = Some(s"${version.value}-${removeSnapshot(spongeApiVersion.value)}"),
    authors = Seq("Katrix"),
    dependencies = Set(
      DependencyInfo(LoadOrder.None, "spongeapi", Some(removeSnapshot(spongeApiVersion.value)), optional = false),
      DependencyInfo(LoadOrder.Before, "katlib", Some(s"2.4.0-${removeSnapshot(spongeApiVersion.value)}"), optional = false)
    )
  )
)

lazy val homeShared = (project in file("shared"))
  .enablePlugins(SpongePlugin)
  .settings(
    commonSettings,
    name := "HomeSweetHome-Shared",
    oreDeploy := None,
    assembleArtifact := false,
    spongeMetaCreate := false,
    //Default version, needs to build correctly against all supported versions
    spongeApiVersion := "5.0.0"
  )

lazy val homeV500 = (project in file("5.0.0"))
  .enablePlugins(SpongePlugin)
  .dependsOn(homeShared)
  .settings(commonSettings, spongeApiVersion := "5.0.0", libraryDependencies += katLibDependecy("5-0-0"))

lazy val homeV600 = (project in file("6.0.0"))
  .enablePlugins(SpongePlugin)
  .dependsOn(homeShared)
  .settings(commonSettings, spongeApiVersion := "6.0.0", libraryDependencies += katLibDependecy("6-0-0"))

lazy val homeV700 = (project in file("7.0.0"))
  .enablePlugins(SpongePlugin)
  .dependsOn(homeShared)
  .settings(commonSettings, spongeApiVersion := "7.0.0-SNAPSHOT", libraryDependencies += katLibDependecy("7-0-0"))

lazy val homeRoot = (project in file("."))
  .disablePlugins(AssemblyPlugin)
  .aggregate(homeV500, homeV600, homeV700)
