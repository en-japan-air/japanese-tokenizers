name := "knp-tokenizer"
description := "Scala Japanese tokenizers."
organization := "com.enjapan"

scalaVersion := "2.11.7"

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Xlint",
  "-Ywarn-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-inaccessible",
  "-Ywarn-infer-any",
  "-Ywarn-nullary-override",
  "-Ywarn-nullary-unit",
  "-Ywarn-numeric-widen",
  "-Ywarn-unused-import",
  "-Ywarn-value-discard",
  "-target:jvm-1.7",
  "-encoding", "UTF-8"
)

resolvers += "en-japan Maven OSS" at "http://dl.bintray.com/en-japan/maven-oss"

libraryDependencies ++= Seq(
  "com.github.mariten" % "kanatools-java" % "1.2.0",
  "com.atilika.kuromoji" % "kuromoji-ipadic" % "0.9.0",
  "com.enjapan" %% "scala-juman-knp" % "0.0.3",
  "org.typelevel" %% "cats" % "0.4.0",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test"
)

bintrayOrganization := Some("en-japan")
bintrayRepository := "maven-oss"
licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))
bintrayVcsUrl := Some("https://github.com/en-japan/scala-juman-knp.git")
bintrayPackageLabels := Seq("scala", "japanese", "tokenizers")
bintrayPackageAttributes ~= (_ ++ Map(
  "issue_tracker_url" -> Seq(bintry.Attr.String("https://github.com/en-japan/japanese-tokenizers/issues")),
  "github_repo" -> Seq(bintry.Attr.String("en-japan/japanese-tokenizers"))
))
