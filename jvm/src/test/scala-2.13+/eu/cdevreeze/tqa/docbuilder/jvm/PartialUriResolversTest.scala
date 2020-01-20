package eu.cdevreeze.tqa.docbuilder.jvm

import java.io.File
import java.net.URI
import java.util.zip.ZipFile

import org.scalatest.FunSuite

import scala.jdk.javaapi.CollectionConverters

class PartialUriResolversTest extends FunSuite {
  val zipFile = new ZipFile(new File(getClass.getResource("/partial-local-mirror.zip").toURI))

  test("zip file resource presented") {
    assert(zipFile != null)
  }

  test("read existing file from zip") {
    val zipEntriesIterator = CollectionConverters.asScala(zipFile.entries())
    val zipEntry = zipEntriesIterator.filter(e => "cache/www.xbrl.org/2008/concept-filter.xsd" == e.getName).next()
    assert(zipEntry != null)
    val is = zipFile.getInputStream(zipEntry)
    val expectedContent = scala.io.Source.fromInputStream(is).mkString
    assert(expectedContent.nonEmpty)
    val resolver = PartialUriResolvers.forZipFileContainingLocalMirror(zipFile, Option(URI.create("cache")))
    assertResult(expectedContent) {
      val byteStream = resolver.apply(URI.create("http://www.xbrl.org/2008/concept-filter.xsd")).get.getByteStream
      scala.io.Source.fromInputStream(byteStream).mkString
    }
  }

  test("non existing path - no exception") {
    val resolver = PartialUriResolvers.forZipFileContainingLocalMirror(zipFile, Option(URI.create("cache")))
    assertResult(true) {
      resolver.apply(URI.create("http://www.xbrl.org/2014/table.xsd")).isEmpty // no exception
    }
  }
}
