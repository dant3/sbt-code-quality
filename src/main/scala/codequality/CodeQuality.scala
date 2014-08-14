package codequality

import sbt.TaskKey


object CodeQuality {
    val check = TaskKey[Unit]("check", "run all code-quality checks")

    lazy val defaults = Seq(check <<= check dependsOn PMD.pmd dependsOn CheckStyle.checkStyle )
}
