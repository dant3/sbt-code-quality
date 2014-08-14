import codequality.PMD

PMD.defaults

name := "pmd-detect-failure"

PMD.format := PMD.ReportFormat.xml

PMD.outputFile := Some(file("target/pmd.xml"))