lazy val exclusions = Seq(ExclusionRule("org.spongepowered", "spongeapi"), ExclusionRule("com.typesafe", "config"))

def removeSnapshot(str: String): String = if (str.endsWith("-SNAPSHOT")) str.substring(0, str.length - 9) else str
def katLibDependecy(module: String) =
  "com.github.Katrix-.KatLib" % s"katlib-$module" % "develop3.0.0-SNAPSHOT" excludeAll (exclusions: _*)

lazy val home =
  crossProject(SpongePlatform("5.1.0"), SpongePlatform("6.0.0"), SpongePlatform("7.0.0"))
    .enablePlugins(SbtProguard)
    .settings(
      name := s"HomeSweetHome-${removeSnapshot(spongeApiVersion.value)}",
      organization := "net.katsstuff",
      version := "3.0.0-SNAPSHOT",
      scalaVersion := "2.12.5",
      resolvers += "jitpack" at "https://jitpack.io",
      addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.4"),
      libraryDependencies += "org.jetbrains" % "annotations" % "15.0" % Provided,
      scalacOptions ++= Seq(
        "-deprecation",
        "-feature",
        "-unchecked",
        "-Xlint",
        "-Yno-adapted-args",
        "-Ywarn-dead-code",
        "-Ywarn-unused-import",
        "-opt:l:inline",
        "-opt-inline-from:**",
        "-opt-warnings:_"
      ),
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
      crossPaths := false,
      assemblyShadeRules in assembly := Seq(
        ShadeRule.rename("cats.**"                     -> "net.katsstuff.homesweethomeshade.cats.@1").inAll,
        ShadeRule.rename("com.google.protobuf.**"      -> "net.katsstuff.homesweethomeshade.protobuf.@1").inAll,
        ShadeRule.rename("com.trueaccord.lenses.**"    -> "net.katsstuff.homesweethomeshade.lenses.@1").inAll,
        ShadeRule.rename("com.trueaccord.scalapb.**"   -> "net.katsstuff.homesweethomeshade.scalapb.@1").inAll,
        ShadeRule.rename("fansi.**"                    -> "net.katsstuff.homesweethomeshade.fansi.@1").inAll,
        ShadeRule.rename("fastparse.**"                -> "net.katsstuff.homesweethomeshade.fastparse.@1").inAll,
        ShadeRule.rename("io.circe.**"                 -> "net.katsstuff.homesweethomeshade.circe.@1").inAll,
        ShadeRule.rename("jawn.**"                     -> "net.katsstuff.homesweethomeshade.jawn.@1").inAll,
        ShadeRule.rename("machinist.**"                -> "net.katsstuff.homesweethomeshade.machinist.@1").inAll, //Zap?
        ShadeRule.rename("metaconfig.**"               -> "net.katsstuff.homesweethomeshade.metaconfig.@1").inAll,
        ShadeRule.rename("org.langmeta.**"             -> "net.katsstuff.homesweethomeshade.langmeta.@1").inAll,
        ShadeRule.rename("org.scalameta.**"            -> "net.katsstuff.homesweethomeshade.scalameta.@1").inAll,
        ShadeRule.rename("org.typelevel.paiges.**"     -> "net.katsstuff.homesweethomeshade.paiges.@1").inAll,
        ShadeRule.rename("pprint.**"                   -> "net.katsstuff.homesweethomeshade.pprint.@1").inAll,
        ShadeRule.rename("scala.**"                    -> "net.katsstuff.homesweethomeshade.scala.@1").inAll,
        ShadeRule.rename("scalapb.**"                  -> "net.katsstuff.homesweethomeshade.scalapb.@1").inAll,
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
    .spongeSettings("5.1.0")(libraryDependencies += katLibDependecy("5-1-0"))
    .spongeSettings("6.0.0")(libraryDependencies += katLibDependecy("6-0-0"))
    .spongeSettings("7.0.0")(libraryDependencies += katLibDependecy("7-0-0"))

lazy val homeV500 = home.spongeProject("5.1.0")
lazy val homeV600 = home.spongeProject("6.0.0")
lazy val homeV700 = home.spongeProject("7.0.0")

lazy val homeRoot = (project in file("."))
  .disablePlugins(AssemblyPlugin)
  .aggregate(homeV500, homeV600, homeV700)
