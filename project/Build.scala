import sbt._
import Keys._
import java.io.File

object ExperimentalBuild extends Build {

  lazy val project = Project("project", file("."),
    settings = buildSettings
  ).settings(sourceGenerators in Compile <+= addDefaultMainClass,
              resourceGenerators in Compile <+= addAResourceFile)

  // SETTINGS

  lazy val buildSettings: Seq[Setting[_]] = Defaults.defaultSettings ++ Seq(
    name := "sbt-cachebusttest",
    version := "1.0",
    scalaVersion := "2.9.2"
  )

  val addDefaultMainClass =  sourceManaged in Compile map { dir =>
    val file = dir / "demo" / "Test.scala"
    IO.write(file, """object Test extends App { println("Hi") }""")
    Seq(file)
  }

  val addAResourceFile = resourceManaged in Compile map { dir =>
    val file = dir / "demo" / "generated.properties"
    IO.write(file, """hello=world""")
    Seq(file)
  }
}


