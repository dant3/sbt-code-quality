import codequality.Keys._

name <<= baseDirectory(_.name)

failOnViolations in pmd := true

TaskKey[Unit]("check-pmd") <<= (pmd) map {
  (result) => if (result.violations <= 0) error("PMD didn't found errors, but there was at least one")
}