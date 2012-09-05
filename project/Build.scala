import sbt._
import Keys._


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
        val mapOfOriginalToTarget = resourceDirs map {dir =>
            ((dir ** "*") filter { _.isFile}).get x rebase(dir, target.getPath)
          } flatten

        s.log.info("map of original to target\n" + mapOfOriginalToTarget)

        val mapOfOriginalToHashedFiles = mapOfOriginalToTarget map { case(original, dest) =>
            val nameComponents = dest.split('.')
            val newName = nameComponents.dropRight(1).toList ::: "hash" :: nameComponents.last :: Nil
            (original, file(newName.mkString(".")))
        }

        s.log.info("map of original to hashed files\n" + mapOfOriginalToHashedFiles)

        Sync(cacheFolder)(mapOfOriginalToHashedFiles)

        val result = mapOfOriginalToHashedFiles map {case(original, dest) => dest}

        s.log.info("Final Output\n" + result)
        result
    }
}


