/*
 * Copyright 2011-2017 Chris de Vreeze
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.cdevreeze.tqa.docbuilder

import java.io.StringReader
import java.net.URI

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.xml.sax.InputSource

import eu.cdevreeze.yaidom.indexed.Elem
import eu.cdevreeze.yaidom.parse.DocumentParserUsingStax
import eu.cdevreeze.yaidom.resolved

/**
 * Simple XML Catalog test case.
 *
 * @author Chris de Vreeze
 */
@RunWith(classOf[JUnitRunner])
class SimpleCatalogTest extends FunSuite {

  test("testParseRelativeUriCatalog") {
    val docParser = DocumentParserUsingStax.newInstance()

    val catalogElem =
      Elem(docParser.parse(new InputSource(new StringReader(catalogXml1))).documentElement)

    val catalog = SimpleCatalog.fromElem(catalogElem)

    val expectedCatalog: SimpleCatalog =
      SimpleCatalog(
        None,
        Vector(
          SimpleCatalog.UriRewrite(
            None,
            "http://www.example.com/part1/2015-01-01/",
            "../part1/2015-01-01/"),
          SimpleCatalog.UriRewrite(
            None,
            "http://www.example.com/part2/2015-01-01/",
            "../part2/2015-01-01/")))

    assertResult(expectedCatalog) {
      catalog
    }

    assertResult(resolved.Elem(catalogElem.underlyingElem).removeAllInterElementWhitespace) {
      resolved.Elem(expectedCatalog.toElem).removeAllInterElementWhitespace
    }
  }

  test("testParseRelativeUriCatalogWithBaseUri") {
    val docParser = DocumentParserUsingStax.newInstance()

    val catalogElem =
      Elem(docParser.parse(new InputSource(new StringReader(catalogXml2))).documentElement)

    val catalog = SimpleCatalog.fromElem(catalogElem)

    val expectedCatalog: SimpleCatalog =
      SimpleCatalog(
        Some(URI.create("../")),
        Vector(
          SimpleCatalog.UriRewrite(
            None,
            "http://www.example.com/part1/2015-01-01/",
            "part1/2015-01-01/"),
          SimpleCatalog.UriRewrite(
            None,
            "http://www.example.com/part2/2015-01-01/",
            "part2/2015-01-01/")))

    assertResult(expectedCatalog) {
      catalog
    }

    assertResult(resolved.Elem(catalogElem.underlyingElem).removeAllInterElementWhitespace) {
      resolved.Elem(expectedCatalog.toElem).removeAllInterElementWhitespace
    }
  }

  test("testUseRelativeUriCatalog") {
    val docParser = DocumentParserUsingStax.newInstance()

    val catalogElem =
      Elem(docParser.parse(new InputSource(new StringReader(catalogXml1))).documentElement)

    val catalog = SimpleCatalog.fromElem(catalogElem)

    assertResult(Some(URI.create("../part1/2015-01-01/a/b/c/"))) {
      catalog.findMappedUri(URI.create("http://www.example.com/part1/2015-01-01/a/b/c/"))
    }

    assertResult(Some(URI.create("../part1/2015-01-01/a/b/c/d.txt"))) {
      catalog.findMappedUri(URI.create("http://www.example.com/part1/2015-01-01/a/b/c/d.txt"))
    }

    assertResult(Some(URI.create("../part1/2015-01-01/"))) {
      catalog.findMappedUri(URI.create("http://www.example.com/part1/2015-01-01/"))
    }

    assertResult(None) {
      catalog.findMappedUri(URI.create("http://www.otherExample.com/part1/2015-01-01/a/b/c/d.txt"))
    }
  }

  test("testUseRelativeUriCatalogWithBaseUri") {
    val docParser = DocumentParserUsingStax.newInstance()

    val catalogElem =
      Elem(docParser.parse(new InputSource(new StringReader(catalogXml2))).documentElement)

    val catalog = SimpleCatalog.fromElem(catalogElem)

    assertResult(Some(URI.create("../part1/2015-01-01/a/b/c/"))) {
      catalog.findMappedUri(URI.create("http://www.example.com/part1/2015-01-01/a/b/c/"))
    }

    assertResult(Some(URI.create("../part1/2015-01-01/a/b/c/d.txt"))) {
      catalog.findMappedUri(URI.create("http://www.example.com/part1/2015-01-01/a/b/c/d.txt"))
    }

    assertResult(Some(URI.create("../part1/2015-01-01/"))) {
      catalog.findMappedUri(URI.create("http://www.example.com/part1/2015-01-01/"))
    }

    assertResult(None) {
      catalog.findMappedUri(URI.create("http://www.otherExample.com/part1/2015-01-01/a/b/c/d.txt"))
    }
  }

  test("testUseRelativeUriCatalogWithoutTrailingSlash") {
    // Removing the trailing spaces from the URI start strings does not affect the relative URI
    // resolution and therefore does not affect URI mapping.

    val docParser = DocumentParserUsingStax.newInstance()

    val catalogElem =
      Elem(docParser.parse(new InputSource(new StringReader(catalogXml3))).documentElement)

    val catalog = SimpleCatalog.fromElem(catalogElem)

    assertResult(Some(URI.create("../part1/2015-01-01/a/b/c/"))) {
      catalog.findMappedUri(URI.create("http://www.example.com/part1/2015-01-01/a/b/c/"))
    }

    assertResult(Some(URI.create("../part1/2015-01-01/a/b/c/d.txt"))) {
      catalog.findMappedUri(URI.create("http://www.example.com/part1/2015-01-01/a/b/c/d.txt"))
    }

    assertResult(Some(URI.create("../part1/2015-01-01/"))) {
      catalog.findMappedUri(URI.create("http://www.example.com/part1/2015-01-01/"))
    }

    assertResult(None) {
      catalog.findMappedUri(URI.create("http://www.otherExample.com/part1/2015-01-01/a/b/c/d.txt"))
    }
  }

  test("testUseRelativeUriCatalogWithDoubleBaseUri") {
    val docParser = DocumentParserUsingStax.newInstance()

    val catalogElem =
      Elem(docParser.parse(new InputSource(new StringReader(catalogXml4))).documentElement)

    val catalog = SimpleCatalog.fromElem(catalogElem)

    assertResult(Some(URI.create("../part1/2015-01-01/a/b/c/"))) {
      catalog.findMappedUri(URI.create("http://www.example.com/part1/2015-01-01/a/b/c/"))
    }

    assertResult(Some(URI.create("../part1/2015-01-01/a/b/c/d.txt"))) {
      catalog.findMappedUri(URI.create("http://www.example.com/part1/2015-01-01/a/b/c/d.txt"))
    }

    assertResult(Some(URI.create("../part1/2015-01-01/"))) {
      catalog.findMappedUri(URI.create("http://www.example.com/part1/2015-01-01/"))
    }

    assertResult(None) {
      catalog.findMappedUri(URI.create("http://www.otherExample.com/part1/2015-01-01/a/b/c/d.txt"))
    }
  }

  test("testUseRelativeUriCatalogWithBaseUriUsingNormalization") {
    val docParser = DocumentParserUsingStax.newInstance()

    val catalogElem =
      Elem(docParser.parse(new InputSource(new StringReader(catalogXml5))).documentElement)

    val catalog = SimpleCatalog.fromElem(catalogElem)

    assertResult(Some(URI.create("../part1/2015-01-01/a/b/c/"))) {
      catalog.findMappedUri(URI.create("http://www.example.com/part1/2015-01-01/a/b/c/"))
    }

    assertResult(Some(URI.create("../part1/2015-01-01/a/b/c/d.txt"))) {
      catalog.findMappedUri(URI.create("http://www.example.com/part1/2015-01-01/a/b/c/d.txt"))
    }

    assertResult(Some(URI.create("../part1/2015-01-01/"))) {
      catalog.findMappedUri(URI.create("http://www.example.com/part1/2015-01-01/"))
    }

    assertResult(None) {
      catalog.findMappedUri(URI.create("http://www.otherExample.com/part1/2015-01-01/a/b/c/d.txt"))
    }
  }

  test("testUseAbsoluteUriCatalog") {
    val docParser = DocumentParserUsingStax.newInstance()

    val catalogElem =
      Elem(docParser.parse(new InputSource(new StringReader(catalogXml6))).documentElement)

    val catalog = SimpleCatalog.fromElem(catalogElem)

    assertResult(Some(URI.create("file:///home/user/part1/2015-01-01/a/b/c/"))) {
      catalog.findMappedUri(URI.create("http://www.example.com/part1/2015-01-01/a/b/c/"))
    }

    assertResult(Some(URI.create("file:///home/user/part1/2015-01-01/a/b/c/d.txt"))) {
      catalog.findMappedUri(URI.create("http://www.example.com/part1/2015-01-01/a/b/c/d.txt"))
    }

    assertResult(Some(URI.create("file:///home/user/part1/2015-01-01/"))) {
      catalog.findMappedUri(URI.create("http://www.example.com/part1/2015-01-01/"))
    }

    assertResult(None) {
      catalog.findMappedUri(URI.create("http://www.otherExample.com/part1/2015-01-01/a/b/c/d.txt"))
    }
  }

  test("testUseAbsoluteUriCatalogWithBaseUri") {
    val docParser = DocumentParserUsingStax.newInstance()

    val catalogElem =
      Elem(docParser.parse(new InputSource(new StringReader(catalogXml7))).documentElement)

    val catalog = SimpleCatalog.fromElem(catalogElem)

    assertResult(Some(URI.create("file:///home/user/part1/2015-01-01/a/b/c/"))) {
      catalog.findMappedUri(URI.create("http://www.example.com/part1/2015-01-01/a/b/c/"))
    }

    assertResult(Some(URI.create("file:///home/user/part1/2015-01-01/a/b/c/d.txt"))) {
      catalog.findMappedUri(URI.create("http://www.example.com/part1/2015-01-01/a/b/c/d.txt"))
    }

    assertResult(Some(URI.create("file:///home/user/part1/2015-01-01/"))) {
      catalog.findMappedUri(URI.create("http://www.example.com/part1/2015-01-01/"))
    }

    assertResult(None) {
      catalog.findMappedUri(URI.create("http://www.otherExample.com/part1/2015-01-01/a/b/c/d.txt"))
    }
  }

  // See the examples in the XBRL Packages specification. They have been used below.

  private val catalogXml1: String = {
    """<catalog
  xmlns="urn:oasis:names:tc:entity:xmlns:xml:catalog">
<rewriteURI uriStartString="http://www.example.com/part1/2015-01-01/" rewritePrefix="../part1/2015-01-01/"/>
<rewriteURI uriStartString="http://www.example.com/part2/2015-01-01/" rewritePrefix="../part2/2015-01-01/"/>
</catalog>"""
  }

  private val catalogXml2: String = {
    """<catalog
  xmlns="urn:oasis:names:tc:entity:xmlns:xml:catalog" xml:base="../">
<rewriteURI uriStartString="http://www.example.com/part1/2015-01-01/" rewritePrefix="part1/2015-01-01/"/>
<rewriteURI uriStartString="http://www.example.com/part2/2015-01-01/" rewritePrefix="part2/2015-01-01/"/>
</catalog>"""
  }

  private val catalogXml3: String = {
    """<catalog
  xmlns="urn:oasis:names:tc:entity:xmlns:xml:catalog" xml:base="../">
<rewriteURI uriStartString="http://www.example.com/part1/2015-01-01" rewritePrefix="part1/2015-01-01/"/>
<rewriteURI uriStartString="http://www.example.com/part2/2015-01-01" rewritePrefix="part2/2015-01-01/"/>
</catalog>"""
  }

  private val catalogXml4: String = {
    """<catalog
  xmlns="urn:oasis:names:tc:entity:xmlns:xml:catalog" xml:base="../">
<rewriteURI xml:base="part1/" uriStartString="http://www.example.com/part1/2015-01-01/" rewritePrefix="2015-01-01/"/>
<rewriteURI xml:base="part2/" uriStartString="http://www.example.com/part2/2015-01-01/" rewritePrefix="2015-01-01/"/>
</catalog>"""
  }

  private val catalogXml5: String = {
    """<catalog
  xmlns="urn:oasis:names:tc:entity:xmlns:xml:catalog" xml:base="../">
<rewriteURI uriStartString="http://www.example.com/part1/../part1/2015-01-01/" rewritePrefix="part1/2015-01-01/"/>
<rewriteURI uriStartString="http://www.example.com/part2/../part2/2015-01-01/" rewritePrefix="part2/2015-01-01/"/>
</catalog>"""
  }

  private val catalogXml6: String = {
    """<catalog
  xmlns="urn:oasis:names:tc:entity:xmlns:xml:catalog">
<rewriteURI uriStartString="http://www.example.com/part1/2015-01-01/" rewritePrefix="file:///home/user/part1/2015-01-01/"/>
<rewriteURI uriStartString="http://www.example.com/part2/2015-01-01/" rewritePrefix="file:///home/user/part2/2015-01-01/"/>
</catalog>"""
  }

  private val catalogXml7: String = {
    """<catalog
  xmlns="urn:oasis:names:tc:entity:xmlns:xml:catalog" xml:base="file:///home/user/">
<rewriteURI uriStartString="http://www.example.com/part1/2015-01-01/" rewritePrefix="part1/2015-01-01/"/>
<rewriteURI uriStartString="http://www.example.com/part2/2015-01-01/" rewritePrefix="part2/2015-01-01/"/>
</catalog>"""
  }
}
