import codequality.Keys._

failOnViolations in findbugs := true

name <<= baseDirectory(_.name)