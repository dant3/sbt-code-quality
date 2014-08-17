import codequality.FindBugs

FindBugs.defaults

FindBugs.failOnViolation := true

name <<= baseDirectory(_.name)