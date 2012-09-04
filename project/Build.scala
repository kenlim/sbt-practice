import sbt._
import Keys._
import java.io.File
import sbt.Path._

object ExperimentalBuild extends Build {

  lazy val project = Project("project", file("."),
    settings = buildSettings
  ).settings(
    resourceGenerators in Compile <+= addHashedResourceFilesWithIndex)

  lazy val buildSettings: Seq[Setting[_]] = Defaults.defaultSettings ++ Seq(
    name := "sbt-cachebusttest",
    version := "1.0",
    scalaVersion := "2.9.2"
  )

  // Should go and get all the unmanaged resources,
  // hash them, and put the result in the managed resource folder
  val addHashedResourceFilesWithIndex =
    (unmanagedResources, resourceManaged, cacheDirectory, resourceDirectories) map {
      (src, dest, cache,  dirs) =>
        val cacheFile = cache / "copy-resources"
        val mappings = (src --- dirs) x (rebase(dirs, dest) | flat(dest))
        mappings map { (source, target) =>
          val nameComponents = target.name.split('.')
          val newName = nameComponents.dropRight(1) :: "hash" :: nameComponents.last
          newName.mkString(".")
        }
        Sync(cacheFile)( mappings )

    }
}


