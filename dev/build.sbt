name := "SCALA_Play_Framework_Example"
 
version := "1.0" 
      
lazy val `scala_play_framework_example` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
      
scalaVersion := "2.12.2"

libraryDependencies ++= Seq( jdbc , ehcache , ws , specs2 % Test , guice )

libraryDependencies += "com.typesafe.play" %% "play-slick" % "3.0.0"
libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.24"
libraryDependencies += "org.mindrot" % "jbcrypt" % "0.4"
libraryDependencies += "com.adrianhurt" %% "play-bootstrap" % "1.4-P26-B4-SNAPSHOT"


unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  

      