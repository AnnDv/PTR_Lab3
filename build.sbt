name := "server-scala-server"

version := "1.0"

scalaVersion := "2.13.1"

lazy val akkaVersion = "2.6.19"
val AkkaHttpVersion = "10.2.9"

// Run in a separate JVM, to make sure sbt waits until all threads have
// finished before returning.
// If you want to keep the application running while executing other
// sbt tasks, consider https://github.com/spray/sbt-revolver/
fork := true

enablePlugins(JavaAppPackaging)

libraryDependencies ++= Seq(
  "com.lightbend.akka" %% "akka-stream-alpakka-sse" % "2.0.2",
  "com.typesafe.akka" %% "akka-stream" % "2.5.31",
  "com.typesafe.akka" %% "akka-http" % "10.1.11",
  "com.typesafe.akka" %% "akka-actor" % "2.5.31",
  "com.lihaoyi" %% "upickle" % "0.9.5",
  "com.typesafe.play" %% "play-json" % "2.8.0",
  "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence-testkit" % akkaVersion,
  "org.iq80.leveldb"            % "leveldb"          % "0.7",
  "org.fusesource.leveldbjni"   % "leveldbjni-all"   % "1.8",
  "org.apache.commons" % "commons-lang3" % "3.3.2",
  "com.lightbend.akka" %% "akka-stream-alpakka-file" % "3.0.4",
  "com.typesafe.akka" %% "akka-stream" % akkaVersion
  // "commons-io" % "commons-io" % "2.11.0"
  // "ch.qos.logback" % "logback-classic" % "1.1.3" 
  
)
