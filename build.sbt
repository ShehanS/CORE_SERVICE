name := """CORE_SERVICE"""
organization := "com.shehan.web"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)
scalaVersion := "2.13.0"

libraryDependencies += "org.json" % "json" % "20190722"
libraryDependencies += guice
libraryDependencies += ws
libraryDependencies += "org.mongodb" % "mongo-java-driver" % "3.11.0"
// https://mvnrepository.com/artifact/com.auth0/java-jwt
libraryDependencies += "org.mongodb" % "mongodb-driver" % "3.8.0-beta3"
//Thanks for using https://jar-download.com                
libraryDependencies += "com.auth0" % "java-jwt" % "3.1.0"
libraryDependencies += "com.lightbend.akka" %% "akka-stream-alpakka-mqtt" % "1.1.2"
// https://mvnrepository.com/artifact/com.google.code.gson/gson
libraryDependencies += "com.google.code.gson" % "gson" % "2.8.6"

libraryDependencies ++= Seq(
  filters
)

// https://mvnrepository.com/artifact/com.google.code.gson/gson
libraryDependencies += "com.google.code.gson" % "gson" % "2.8.6"
libraryDependencies += "com.rethinkdb" % "rethinkdb-driver" % "2.3.3"
libraryDependencies += "com.typesafe.play" %% "play-mailer" % "7.0.1"
libraryDependencies += "com.typesafe.play" %% "play-mailer-guice" % "8.0.0"


libraryDependencies += "org.apache.logging.log4j" % "log4j-api" % "2.11.2"
libraryDependencies += "org.apache.logging.log4j" % "log4j-core" % "2.11.2"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.5.30"
