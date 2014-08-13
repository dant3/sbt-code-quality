import codequality.PMD

TaskKey[Unit]("check-pmd") <<= (PMD.pmd) map {
    (pmdResultOk) => if (pmdResultOk) error("PMD didn't found errors")
}