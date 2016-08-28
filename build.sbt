name := """play-creds-seed"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

resolvers ++= Seq(
  "Atlassian Releases" at "https://maven.atlassian.com/public/",
  "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases",
  Resolver.sonatypeRepo("snapshots")
 )


libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  filters,
  "com.mohiva" %% "play-silhouette-testkit" % "4.0.0" % "test",
  "com.mohiva" %% "play-silhouette" % "4.0.0",
  "org.reactivemongo" %% "play2-reactivemongo" % "0.11.14",
  "net.codingwell" %% "scala-guice" % "4.0.0",
  "net.ceedubs" %% "ficus" % "1.1.2",
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
  "com.typesafe.play.plugins" %% "play-plugins-mailer" % "2.3.0",
  "com.typesafe.play" %% "play-mailer" % "5.0.0"
)

libraryDependencies ++= Seq(
  "com.mohiva" %% "play-silhouette-password-bcrypt" % "4.0.0"
)

libraryDependencies ++= Seq(
  "com.mohiva" %% "play-silhouette-persistence" % "4.0.0"
)
resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
