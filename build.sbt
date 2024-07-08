
ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.3"

lazy val root = (project in file("."))
  .settings(
    name := "YoutubeAPI"
  )
libraryDependencies ++= Seq(
  // Akka HTTP for building reactive web servers
  "com.typesafe.akka" %% "akka-http" % "10.5.3",
  "com.typesafe.akka" %% "akka-actor-typed" % "2.8.5",
  "com.typesafe.akka" %% "akka-stream" % "2.8.5",
  // JSON handling with Spray
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.5.3",
  "io.spray" %% "spray-json" % "1.3.6",
  "io.circe" %% "circe-generic" % "0.14.1",
  "io.circe" %% "circe-parser" % "0.14.1",
  "io.circe" %% "circe-core" % "0.14.1",
  // Logging
  "ch.qos.logback" % "logback-classic" % "1.2.10",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
  // Google API Client libraries
  "com.google.apis" % "google-api-services-youtube" % "v3-rev222-1.25.0",
  "com.google.auth" % "google-auth-library-oauth2-http" % "1.4.0",
  "com.github.scribejava" % "scribejava-core" % "8.3.3",
  "com.github.scribejava" % "scribejava-apis" % "8.3.3",
)