import codequality.PMD

PMD.defaults

name <<= baseDirectory(_.name)

PMD.format := PMD.ReportFormat.xml

PMD.outputFile := Some(file("target/pmd.xml"))