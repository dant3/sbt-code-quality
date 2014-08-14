import codequality.PMD

TaskKey[Unit]("check-pmd") <<= (PMD.pmd) map {
    (result) => if (result.violations <= 0) error("PMD didn't found errors, but there was at least one")
}