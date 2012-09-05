import sbt._
import Keys._
import java.io.File
import sbt.Path._

object ExperimentalBuild extends Build {

  lazy val project = Project("project", file("."),
    settings = buildSettings
  ).settings(
    resourceGenerators in Compile <+= generateHashedResourcesWithIndexFile)

  lazy val buildSettings: Seq[Setting[_]] = Defaults.defaultSettings ++ Seq(
    name := "sbt-cachebusttest",
    version := "1.0",
    scalaVersion := "2.9.2"
  )

  // Get all the unmanaged resources, hash them,
  // then put the result in the managed resource folder
  val generateHashedResourcesWithIndexFile =
    (unmanagedResourceDirectories in Compile, sourceManaged, cacheDirectory, streams) map {
      (resourceDirs, target, cache, s) =>
        val cacheFolder = cache / "hashed-resources"
        val allResources = (resourceDirs map { dir => (dir ** "*") get}).flatten
        val resourcesToHash = allResources map { resource =>
          s.log.info("Name: %s".format(resource.getPath) )
          val nameComponents = resource.getPath.split('.')
          val newName = nameComponents.dropRight(1).toList ::: "hash" :: nameComponents.last :: Nil
          (resource, file(newName.mkString(".")))
        }
        Sync(cacheFolder)(resourcesToHash)
        resourcesToHash map { case(original, hashed) => hashed }
    }


//    (unmanagedResources, resourceManaged, resourceDirectories, cacheDirectory) map {
//      (originalResources, managedResourceFolder, resourceDirectories, cache) =>
//        val cacheFile = cache / "copy-resources"
//        val mappings = (originalResources --- resourceDirectories) x (rebase(resourceDirectories, managedResourceFolder))
//        val newMappings = mappings map { case(source, target) =>
//          val nameComponents = target.name.split('.')
//          val newName = nameComponents.dropRight(1).toList ::: "hash" :: nameComponents.last
//          newName.mkString(".")
//        }
//        Sync(cacheFile)( mappings )
//        newMappings map { case(source, target) => target}
//    }
}


