def removeSnapshot(str: String): String = if (str.endsWith("-SNAPSHOT")) str.substring(0, str.length - 9) else str
def katLibDependecy(module: String) =
  "com.github.Katrix-.KatLib" % s"katlib-$module" % "f8003fc5f512ce58dc81fab8e180fd27f05f7904" % Provided

lazy val home =
  crossProject(SpongePlatform("5.0.0"), SpongePlatform("6.0.0"), SpongePlatform("7.0.0"))
    .settings(
      name := s"HomeSweetHome-${removeSnapshot(spongeApiVersion.value)}",
      organization := "io.github.katrix",
      version := "2.3.0",
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
          DependencyInfo(
            LoadOrder.Before,
            "katlib",
            Some(s"2.4.0-${removeSnapshot(spongeApiVersion.value)}"),
            optional = false
          )
        )
      ),
      oreDeploymentKey := (oreDeploymentKey in Scope.Global).?.value.flatten,
      //https://github.com/portable-scala/sbt-crossproject/issues/74
      Seq(Compile, Test).flatMap(inConfig(_) {
        unmanagedResourceDirectories ++= {
          unmanagedSourceDirectories.value
            .map(src => (src / ".." / "resources").getCanonicalFile)
            .filterNot(unmanagedResourceDirectories.value.contains)
            .distinct
        }
      })
    )
    .spongeSettings("5.0.0")(libraryDependencies += katLibDependecy("5-0-0"))
    .spongeSettings("6.0.0")(libraryDependencies += katLibDependecy("6-0-0"))
    .spongeSettings("7.0.0")(libraryDependencies += katLibDependecy("7-0-0"))

lazy val homeV500 = home.spongeProject("5.0.0")
lazy val homeV600 = home.spongeProject("6.0.0")
lazy val homeV700 = home.spongeProject("7.0.0")

lazy val homeRoot = (project in file("."))
  .disablePlugins(AssemblyPlugin)
  .aggregate(homeV500, homeV600, homeV700)
