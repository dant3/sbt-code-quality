import codequality.PMD

PMD.defaults

name <<= baseDirectory(_.name)

PMD.failOnViolations := true