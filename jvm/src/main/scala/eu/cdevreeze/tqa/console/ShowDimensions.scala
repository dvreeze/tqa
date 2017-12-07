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

package eu.cdevreeze.tqa.console

import java.io.File
import java.net.URI
import java.util.logging.Logger

import scala.collection.immutable

import eu.cdevreeze.tqa.backingelem.indexed.docbuilder.IndexedDocumentBuilder
import eu.cdevreeze.tqa.backingelem.nodeinfo.docbuilder.SaxonDocumentBuilder
import eu.cdevreeze.tqa.base.relationship.DefaultRelationshipFactory
import eu.cdevreeze.tqa.base.relationship.HasHypercubeRelationship
import eu.cdevreeze.tqa.base.taxonomy.BasicTaxonomy
import eu.cdevreeze.tqa.base.taxonomybuilder.DefaultDtsCollector
import eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyBuilder
import eu.cdevreeze.tqa.docbuilder.DocumentBuilder
import eu.cdevreeze.tqa.docbuilder.jvm.UriConverters
import eu.cdevreeze.yaidom.core.EName
import eu.cdevreeze.yaidom.parse.DocumentParserUsingStax
import net.sf.saxon.s9api.Processor

/**
 * Program that shows dimensional data in a given taxonomy.
 *
 * @author Chris de Vreeze
 */
object ShowDimensions {

  private val logger = Logger.getGlobal

  def main(args: Array[String]): Unit = {
    require(args.size >= 2, s"Usage: ShowDimensions <taxo root dir> <entrypoint URI 1> ...")
    val rootDir = new File(args(0))
    require(rootDir.isDirectory, s"Not a directory: $rootDir")

    val entrypointUris = args.drop(1).map(u => URI.create(u)).toSet
    val useSaxon = System.getProperty("useSaxon", "false").toBoolean

    val basicTaxo = buildTaxonomy(rootDir, entrypointUris, useSaxon)

    val rootElems = basicTaxo.taxonomyBase.rootElems

    logger.info(s"The taxonomy has ${rootElems.size} taxonomy root elements")
    logger.info(s"The taxonomy has ${basicTaxo.relationships.size} relationships")
    logger.info(s"The taxonomy has ${basicTaxo.findAllDimensionalRelationships.size} dimensional relationships")

    val hasHypercubes = basicTaxo.findAllHasHypercubeRelationships

    val hasHypercubeGroupsWithSameKey = hasHypercubes.distinct.groupBy(hh => (hh.primary, hh.elr)).filter(_._2.size >= 2)

    logger.info(s"Number of has_hypercube groups with more than 1 has-hypercube for the same primary and ELR: ${hasHypercubeGroupsWithSameKey.size}")

    showHasHypercubeTrees(hasHypercubes, basicTaxo)

    // Concrete primary items and inheritance of has-hypercubes

    showHasHypercubeInheritance(hasHypercubes, basicTaxo)

    logger.info("Ready")
  }

  private def showHasHypercubeTrees(hasHypercubes: immutable.IndexedSeq[HasHypercubeRelationship], basicTaxo: BasicTaxonomy): Unit = {
    hasHypercubes foreach { hasHypercube =>
      println(s"Has-hypercube found for primary ${hasHypercube.primary} and ELR ${hasHypercube.elr}")

      val hypercubeDimensions = basicTaxo.findAllConsecutiveHypercubeDimensionRelationships(hasHypercube)
      val dimensions = hypercubeDimensions.map(_.dimension).distinct.sortBy(_.toString)
      println(s"It has dimensions: ${dimensions.mkString("\n\t", ",\n\t", "")}")

      val usableDimensionMembers: Map[EName, Set[EName]] = basicTaxo.findAllUsableDimensionMembers(hasHypercube)

      usableDimensionMembers.toSeq.sortBy(_._1.toString) foreach {
        case (dimension, members) =>
          println(s"\tDimension: $dimension. Usable members:")

          members.toSeq.sortBy(_.toString) foreach { member =>
            println(s"\t\t$member")
          }
      }
    }
  }

  // scalastyle:off method.length
  private def showHasHypercubeInheritance(hasHypercubes: immutable.IndexedSeq[HasHypercubeRelationship], basicTaxo: BasicTaxonomy): Unit = {
    val concretePrimaryItemDecls = basicTaxo.findAllPrimaryItemDeclarations.filter(_.isConcrete)

    val concretePrimariesNotInheritingOrHavingHasHypercube = concretePrimaryItemDecls filter { itemDecl =>
      basicTaxo.findAllOwnOrInheritedHasHypercubes(itemDecl.targetEName).isEmpty
    } map (_.targetEName)

    val namespacesOfConcretePrimariesNotInheritingOrHavingHasHypercube =
      concretePrimariesNotInheritingOrHavingHasHypercube.flatMap(_.namespaceUriOption).distinct.sortBy(_.toString)

    logger.info(
      s"Number of (in total ${concretePrimaryItemDecls.size}) concrete primary items not inheriting or having any has-hypercube: " +
        s"${concretePrimariesNotInheritingOrHavingHasHypercube.distinct.size}")
    // scalastyle:off magic.number
    logger.info(
      s"Namespaces of concrete primary items not inheriting or having any has-hypercube (first 50):\n\t" +
        s"${namespacesOfConcretePrimariesNotInheritingOrHavingHasHypercube.take(50).mkString(", ")}")

    val inheritingPrimaries =
      (hasHypercubes flatMap { hh =>
        basicTaxo.filterOutgoingConsecutiveDomainMemberRelationshipPaths(hh.primary) { path =>
          path.firstRelationship.elr == hh.elr
        }
      } flatMap (_.relationships.map(_.member))).toSet

    val ownPrimaries = hasHypercubes.map(_.sourceConceptEName).toSet
    val inheritingOrOwnPrimaries = inheritingPrimaries.union(ownPrimaries)
    val inheritingOrOwnConcretePrimaries =
      inheritingOrOwnPrimaries filter (item => basicTaxo.findPrimaryItemDeclaration(item).exists(_.isConcrete))

    require(
      concretePrimariesNotInheritingOrHavingHasHypercube.toSet ==
        concretePrimaryItemDecls.map(_.targetEName).toSet.diff(inheritingOrOwnConcretePrimaries),
      s"Finding concrete primary items not inheriting or having any has-hypercube from 2 directions must give the same result")

    logger.info("Showing inheriting concrete items")

    val hasHypercubeInheritanceOrSelf = basicTaxo.computeHasHypercubeInheritanceOrSelfReturningElrToPrimariesMaps

    concretePrimaryItemDecls.map(_.targetEName).distinct.sortBy(_.toString) foreach { item =>
      val hasHypercubes = basicTaxo.findAllOwnOrInheritedHasHypercubes(item)
      val elrPrimariesPairs = hasHypercubes.groupBy(_.elr).mapValues(_.map(_.primary)).toSeq.sortBy(_._1)

      require(
        elrPrimariesPairs.toMap.mapValues(_.toSet) == hasHypercubeInheritanceOrSelf.getOrElse(item, Map.empty),
        s"Finding own or inherited has-hypercubes must be consistent with the bulk methods for has-hypercube inheritance-or-self")

      elrPrimariesPairs foreach {
        case (elr, primaries) =>
          println(
            s"Concrete item $item inherits or has has-hypercubes for ELR $elr and with primaries ${primaries.distinct.sortBy(_.toString).mkString(", ")}")
      }
    }
  }

  private def buildTaxonomy(rootDir: File, entrypointUris: Set[URI], useSaxon: Boolean): BasicTaxonomy = {
    val documentBuilder = getDocumentBuilder(useSaxon, rootDir)
    val documentCollector = DefaultDtsCollector(entrypointUris)

    val lenient = System.getProperty("lenient", "false").toBoolean

    val relationshipFactory =
      if (lenient) DefaultRelationshipFactory.LenientInstance else DefaultRelationshipFactory.StrictInstance

    val taxoBuilder =
      TaxonomyBuilder.
        withDocumentBuilder(documentBuilder).
        withDocumentCollector(documentCollector).
        withRelationshipFactory(relationshipFactory)

    logger.info(s"Starting building the DTS with entrypoint(s) ${entrypointUris.mkString(", ")}")

    val basicTaxo = taxoBuilder.build()
    basicTaxo
  }

  private def getDocumentBuilder(useSaxon: Boolean, rootDir: File): DocumentBuilder = {
    if (useSaxon) {
      val processor = new Processor(false)

      SaxonDocumentBuilder.usingUriConverter(processor.newDocumentBuilder(), UriConverters.uriToLocalUri(_, rootDir))
    } else {
      IndexedDocumentBuilder.usingUriConverter(DocumentParserUsingStax.newInstance(), UriConverters.uriToLocalUri(_, rootDir))
    }
  }
}
