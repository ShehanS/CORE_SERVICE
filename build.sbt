name := """CORE_SERVICE"""
organization := "com.shehan.web"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.13.0"

libraryDependencies += guice
// https://mvnrepository.com/artifact/org.mongodb.scala/mongo-scala-driver
// https://mvnrepository.com/artifact/org.mongodb/mongo-java-driver
libraryDependencies += "org.mongodb" % "mongo-java-driver" % "3.11.0"
// https://mvnrepository.com/artifact/com.auth0/java-jwt
libraryDependencies += "com.auth0" % "java-jwt" % "3.1.0"
