import codequality.CheckStyle

CheckStyle.defaults

name <<= baseDirectory(_.name)

CheckStyle.failOnViolations := true