package eu.cdevreeze.tqa.docbuilder.jvm

import java.net.URI
import java.nio.file.Paths

import org.scalatest.FunSuite

class UriConvertersTest extends FunSuite {
  test("find single local catalog by single remote URI") {
    val remoteURI = URI.create("http://www.xbrl.org/kvk/some.xsd")
    val localURI = URI.create("file:///home/www.xbrl.org/kvk/")
    val catalog = Map(remoteURI -> List(localURI))
    val converter = UriConverters.fromUriConverterWithPseudoMultiCatalog(catalog)
    assertResult(Option(localURI)) {
      converter.apply(remoteURI)
    }
  }

  test("resolve two paths - one existing, another non-existing") {
    val testResourcesUri = Paths.get(getClass.getResource("/").toURI).toUri
    val catalog = Map(URI.create("http://xbrl.sec.gov/stpr/") -> List(
      testResourcesUri.resolve("xbrl.sec.gov/sic/"),
      testResourcesUri.resolve("xbrl.sec.gov/stpr/")))
    val converter = UriConverters.fromUriConverterWithPseudoMultiCatalog(catalog)
    assertResult(Option(testResourcesUri.resolve("xbrl.sec.gov/stpr/2011/stpr-2011-01-31.xsd"))) {
      converter.apply(URI.create("http://xbrl.sec.gov/stpr/2011/stpr-2011-01-31.xsd"))
    }
  }
}

