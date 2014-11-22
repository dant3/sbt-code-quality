package codequality

import sbt._

object CodeQualityPlugin extends AutoPlugin {
    val autoImport = codequality.Keys

    override def requires = sbt.plugins.JvmPlugin
    override lazy val projectSettings: Seq[Def.Setting[_]] = rules.all

    // This plugin is automatically enabled for projects which are JvmPlugin.
    override def trigger = allRequirements
}
