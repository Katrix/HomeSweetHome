lazy val commonSettings = Seq(
	organization := "io.github.katrix",
	scalaVersion := "2.11.8",
	resolvers += "SpongePowered" at "https://repo.spongepowered.org/maven",
	resolvers += Resolver.mavenLocal,
	libraryDependencies += "io.github.katrix" % "katlib" % "1.0.0" % "provided",
	scalacOptions += "-Xexperimental",
	crossPaths := false,
	assemblyShadeRules in assembly := Seq(
		ShadeRule.rename("scala.**" -> "io.github.katrix.chateditor.shade.scala.@1").inProject
	),
	autoScalaLibrary := false
)

lazy val homeShared = project in file("shared") settings(commonSettings: _*)  settings(
	name := "HomeSweetHome",
	version := "1.0.0",
	assembleArtifact := false, //Why doesn't this one disable stuff?
	//Default version
	libraryDependencies += "org.spongepowered" % "spongeapi" % "4.1.0" % "provided"
	)

lazy val homeV410 = project in file("4.1.0") dependsOn homeShared settings(commonSettings: _*) settings(
	name := "HomeSweetHome-4.1.0",
	version := "1.0.0",
	libraryDependencies += "org.spongepowered" % "spongeapi" % "4.1.0" % "provided",
	libraryDependencies += "io.github.katrix" % "katlib-4-1-0" % "1.0.0" % "provided"
	)

lazy val homeV500 = project in file("5.0.0") dependsOn homeShared settings(commonSettings: _*) settings(
	name := "HomeSweetHome-5.0.0-SNAPSHOT",
	version := "1.0.0",
	libraryDependencies += "org.spongepowered" % "spongeapi" % "5.0.0-SNAPSHOT" % "provided",
	libraryDependencies += "io.github.katrix" % "katlib-5-0-0-snapshot" % "1.0.0" % "provided"
	)

lazy val homeRoot = project in file(".") settings (publishArtifact := false) disablePlugins AssemblyPlugin aggregate(homeV410, homeV500)