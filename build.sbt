name := "reviyou"

//import com.typesafe.config._

//val conf = ConfigFactory.parseFile(new File("conf/application.conf")).resolve()

//version := conf.getString("app.version")
version := "1.0-SNAPSHOT"

scalaVersion := "2.11.1"

//organization := "com.reviyou"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  filters,
  cache,
  //"org.reactivemongo" %% "reactivemongo" % "0.10.5.akka23-SNAPSHOT",
  "org.reactivemongo" %% "play2-reactivemongo" % "0.10.5.0.akka22",
    "junit"          % "junit" % "4.11" % "test",
    "org.scalatest" %% "scalatest" % "2.1.2" % "test",
    "org.specs2"    %% "specs2"    % "2.3",      //% "test"
    "org.mockito" % "mockito-core" % "1.9.5",
    "net.databinder.dispatch" %% "dispatch-core" % "0.11.0",
    "net.databinder.dispatch" %% "dispatch-json4s-jackson" % "0.11.0",
    //working with json libraries(json4s) - added for the future use
    "org.json4s" %% "json4s-native" % "3.2.8",
    "org.json4s" %% "json4s-jackson" % "3.2.6",
    "com.github.lookfirst" % "sardine" % "5.3",
    "com.sendgrid" % "sendgrid-java" % "2.0.0",
    "org.mindrot" % "jbcrypt" % "0.3m",
    "com.notnoop.apns" % "apns" % "1.0.0.Beta6"
)
//Calculating an Object Graph's Size on the JVM
//libraryDependencies += "org.openjdk.jol" % "jol-core" % "0.2"

play.Project.playScalaSettings
//val main = play.Project("reviyou", "1.0-SNAPSHOT", libraryDependencies.transform).settings(
//  javaOptions in Test += "-Dconfig.file=conf/test.conf"
//)

//resolvers += Resolver.sonatypeRepo("snapshots")
//resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

resolvers += "Sonatype releases" at "http://oss.sonatype.org/content/repositories/releases/"
