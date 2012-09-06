import sbt._
import Keys._


object ExperimentalBuild extends Build {

  lazy val project = Project("project", file("."),
    settings = buildSettings
  ).settings(
    unmanagedResourceDirectories in Compile <+= baseDirectory / "booyah",
    resourceGenerators in Compile <+= generateHashedResourcesWithIndexFile)

  lazy val buildSettings: Seq[Setting[_]] = Defaults.defaultSettings ++ Seq(
    name := "sbt-cachebusttest",
    version := "1.0",
    scalaVersion := "2.9.2"
  )

  // Get all the unmanaged resources, hash them,
  // then put the result in the managed resource folder
  val generateHashedResourcesWithIndexFile =
    (unmanagedResourceDirectories in Compile, resourceManaged in Compile, cacheDirectory, streams) map {
      (resourceDirs, target, cache, s) =>
        val cacheFolder = cache / "hashed-resources"
        val indexFile = target / "hashindex.properties"

        s.log.info("This is the unmanaged folders:" + resourceDirs)
        // Forcing it to go to "
        val mapOfOriginalToTarget = resourceDirs map {dir =>
            ((dir ** "*") filter { _.isFile}).get x rebase(dir, target)
          } flatten

        s.log.info("map of original to target\n" + mapOfOriginalToTarget)

        val mapOfOriginalToHashedFiles = mapOfOriginalToTarget map { case(original, dest) =>
            val nameComponents = dest.getPath.split('.')
            val newName = nameComponents.dropRight(1).toList ::: hash(original) :: nameComponents.last :: Nil
            (original, file(newName.mkString(".")))
        }


        mapOfOriginalToHashedFiles foreach { case(original, dest) =>
          val resourceFile = ((original x relativeTo(resourceDirs)) head)._2
          val hashedFile = dest.relativeTo(target).get
          IO.write(indexFile, "%s=%s\n".format(resourceFile, hashedFile, IO.defaultCharset, true))
        }

        s.log.info("map of original to hashed files\n" + mapOfOriginalToHashedFiles)

        Sync(cacheFolder)(mapOfOriginalToHashedFiles)

        val result = mapOfOriginalToHashedFiles map {case(original, dest) => dest}

        s.log.info("Final Output\n" + result)
        Seq(indexFile) ++ result
    }


  private val digester = java.security.MessageDigest.getInstance("SHA-1")

  private def hash(file: File): String = {
    val source = scala.io.Source.fromFile(file, "latin1")
    val bytes = source.map(_.toByte).toArray
    source.close()
    val byteData = digester.digest(bytes)
    byteData.foldLeft("") {
      (a, b) => a + (Integer.toString((b & 0xff) + 0x100, 16).substring(1))
    }.take(8)
  }
}


