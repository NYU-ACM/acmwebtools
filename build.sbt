name := """ACM-WEBTOOLS"""

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala, RpmPlugin)

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.0-RC1" % Test,
  "org.jfree" % "jfreechart" % "1.0.19",
  "org.apache.pdfbox" % "pdfbox" % "1.8.10"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

maintainer in Linux := "Donald Mennerich <don.mennerich@nyu.edu>"

packageSummary in Linux := "acm-webtools"

packageDescription := "Tools for born digital processing"

rpmRelease := "1"

rpmVendor := "dlts.nyu.edu"

rpmUrl := Some("Don:archives#179@http://nexus-dev.dlts.org/acm-webtools-development/")

rpmLicense := Some("Apache v2")

defaultLinuxInstallLocation := "/opt"
