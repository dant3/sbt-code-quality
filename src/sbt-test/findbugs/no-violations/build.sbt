import codequality.Keys._

name <<= baseDirectory(_.name)

failOnViolations in findbugs := true
