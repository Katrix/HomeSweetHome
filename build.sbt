import sbt.Keys.resolvers

lazy val exclusions = Seq(ExclusionRule("org.spongepowered", "spongeapi"), ExclusionRule("com.typesafe", "config"))

def removeSnapshot(str: String): String = if (str.endsWith("-SNAPSHOT")) str.substring(0, str.length - 9) else str
def katLibDependecy(module: String) =
  "com.github.Katrix-.KatLib" % s"katlib-$module" % "feature~one-true-katlib-SNAPSHOT" excludeAll (exclusions: _*)

lazy val commonSettings = Seq(
  organization := "net.katsstuff",
  version := "3.0.0-SNAPSHOT",
  scalaVersion := "2.12.6",
  addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.6"),
  libraryDependencies += "org.typelevel" %% "cats-effect" % "1.0.0-RC2", //TODO: Update in KatLib
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding",
    "utf-8",
    "-explaintypes",
    "-feature",
    "-unchecked",
    "-Xcheckinit",
    "-Xlint",
    "-Yno-adapted-args",
    "-Ypartial-unification",
    "-Ywarn-dead-code",
    "-Ywarn-infer-any",
    "-Ywarn-nullary-override",
    "-Ywarn-nullary-unit",
    "-Ywarn-unused",
    "-language:higherKinds"
  ),
  crossPaths := false,
  resolvers += "jitpack" at "https://jitpack.io",
)

lazy val homeBase =
  project.settings(commonSettings, name := s"homesweethome-base", libraryDependencies += katLibDependecy("base"))

lazy val homeSponge =
  crossProject(SpongePlatform("5.1.0"), SpongePlatform("6.0.0"), SpongePlatform("7.0.0"))
    .configure(_.dependsOn(homeBase))
    .enablePlugins(SbtProguard)
    .settings(
      commonSettings,
      name := s"homesweethome-sponge${removeSnapshot(spongeApiVersion.value)}",
      scalacOptions ++= Seq("-opt:l:inline", "-opt-inline-from:**", "-opt-warnings:_"),
      proguardOptions in Proguard ++= Seq(
        "-dontwarn", //"-dontwarn io.github.katrix.homesweethome.shade.scala.**",
        "-dontnote",
        "-dontoptimize",
        "-dontobfuscate",
        "-keepparameternames",
        "-keepattributes *",
        "-keep public class net.katsstuff.homesweethome.HomeSweetHome",
        """|-keepclassmembers class * {
           |    ** MODULE$;
           |    @org.spongepowered.api.event.Listener *;
           |    @com.google.inject.Inject *;
           |}""".stripMargin
      ),
      proguardInputs in Proguard := Seq(assembly.value),
      javaOptions in (Proguard, proguard) := Seq("-Xmx1G"),
      assemblyShadeRules in assembly := Seq(
        ShadeRule.rename("cats.**"                     -> "net.katsstuff.homesweethomeshade.cats.@1").inAll,
        ShadeRule.rename("fansi.**"                    -> "net.katsstuff.homesweethomeshade.fansi.@1").inAll,
        ShadeRule.rename("fastparse.**"                -> "net.katsstuff.homesweethomeshade.fastparse.@1").inAll,
        ShadeRule.rename("io.circe.**"                 -> "net.katsstuff.homesweethomeshade.circe.@1").inAll,
        ShadeRule.rename("jawn.**"                     -> "net.katsstuff.homesweethomeshade.jawn.@1").inAll,
        ShadeRule.rename("machinist.**"                -> "net.katsstuff.homesweethomeshade.machinist.@1").inAll, //Zap?
        ShadeRule.rename("org.typelevel.paiges.**"     -> "net.katsstuff.homesweethomeshade.paiges.@1").inAll,
        ShadeRule.rename("pprint.**"                   -> "net.katsstuff.homesweethomeshade.pprint.@1").inAll,
        ShadeRule.rename("scala.**"                    -> "net.katsstuff.homesweethomeshade.scala.@1").inAll,
        ShadeRule.rename("shapeless.**"                -> "net.katsstuff.homesweethomeshade.shapeless.@1").inAll,
        ShadeRule.rename("sourcecode.**"               -> "net.katsstuff.homesweethomeshade.sourcecode.@1").inAll,
        ShadeRule.rename("net.katsstuff.katlib.**"     -> "net.katsstuff.homesweethomeshade.katlib.@1").inAll,
        ShadeRule.rename("net.katsstuff.scammander.**" -> "net.katsstuff.homesweethomeshade.scammander.@1").inAll,
        ShadeRule.zap("macrocompat.**").inAll,
      ),
      assemblyJarName := s"${name.value}-assembly-${version.value}.jar",
      spongePluginInfo := spongePluginInfo.value.copy(
        id = "homesweethome",
        name = Some("HomeSweetHome"),
        version = Some(s"${version.value}-${removeSnapshot(spongeApiVersion.value)}"),
        authors = Seq("Katrix"),
        dependencies = Set(
          DependencyInfo(LoadOrder.None, "spongeapi", Some(removeSnapshot(spongeApiVersion.value)), optional = false)
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
    .spongeSettings("5.1.0")(libraryDependencies += katLibDependecy("sponge5-1-0"))
    .spongeSettings("6.0.0")(libraryDependencies += katLibDependecy("sponge6-0-0"))
    .spongeSettings("7.0.0")(libraryDependencies += katLibDependecy("sponge7-0-0"))

lazy val homeBukkit = project
  .dependsOn(homeBase)
  .settings(
    commonSettings,
    name := "homesweethome-bukkit",
    libraryDependencies += katLibDependecy("bukkit"),
    resolvers += "spigotmc-snapshots" at "https://hub.spigotmc.org/nexus/content/repositories/snapshots",
    resolvers += Resolver.sonatypeRepo("snapshots"),
    assemblyShadeRules in assembly := Seq(
      ShadeRule.rename("cats.**"                     -> "net.katsstuff.katlib.shade.cats.@1").inProject,
      ShadeRule.rename("fansi.**"                    -> "net.katsstuff.katlib.shade.fansi.@1").inProject,
      ShadeRule.rename("fastparse.**"                -> "net.katsstuff.katlib.shade.fastparse.@1").inProject,
      ShadeRule.rename("io.circe.**"                 -> "net.katsstuff.katlib.shade.circe.@1").inProject,
      ShadeRule.rename("jawn.**"                     -> "net.katsstuff.katlib.shade.jawn.@1").inProject,
      ShadeRule.rename("machinist.**"                -> "net.katsstuff.katlib.shade.machinist.@1").inProject,
      ShadeRule.rename("org.typelevel.paiges.**"     -> "net.katsstuff.katlib.shade.paiges.@1").inProject,
      ShadeRule.rename("pprint.**"                   -> "net.katsstuff.katlib.shade.pprint.@1").inProject,
      ShadeRule.rename("scala.**"                    -> "net.katsstuff.katlib.shade.scala.@1").inProject,
      ShadeRule.rename("shapeless.**"                -> "net.katsstuff.katlib.shade.shapeless.@1").inProject,
      ShadeRule.rename("sourcecode.**"               -> "net.katsstuff.katlib.shade.sourcecode.@1").inProject,
      ShadeRule.rename("net.katsstuff.katlib.**"     -> "net.katsstuff.katlib.shade.katlib.@1").inProject,
      ShadeRule.rename("net.katsstuff.scammander.**" -> "net.katsstuff.katlib.shade.scammander.@1").inProject,
      ShadeRule.zap("macrocompat.**").inAll,
    ),
  )

lazy val homeSpongeV500 = homeSponge.spongeProject("5.1.0")
lazy val homeSpongeV600 = homeSponge.spongeProject("6.0.0")
lazy val homeSpongeV700 = homeSponge.spongeProject("7.0.0")

lazy val homeRoot = (project in file("."))
  .disablePlugins(AssemblyPlugin)
  .aggregate(homeBase, homeSpongeV500, homeSpongeV600, homeSpongeV700, homeBukkit)
