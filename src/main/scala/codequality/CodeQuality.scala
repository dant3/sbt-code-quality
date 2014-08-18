package codequality

import sbt.TaskKey


object CodeQuality {
    val check = TaskKey[Unit]("check", "run all code-quality checks")

    lazy val defaults = Seq(
        CheckStyle.defaults,
        PMD.defaults,
        FindBugs.defaults
    ).flatten ++ Seq(
        check <<= check dependsOn PMD.pmd dependsOn CheckStyle.checkStyle
    )
}
