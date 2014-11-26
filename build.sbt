sbtPlugin := true

name := "sbt-code-quality"

organization := "com.github.dant3"

version := "0.3.0"

scalaVersion := "2.10.4"

crossScalaVersions := Seq("2.10.4")

scalacOptions in Test ++= Seq("-Yrangepos")
