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
import java.io.FileInputStream
import java.net.URI
import java.util.logging.Logger
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

import scala.collection.immutable
import scala.util.chaining._

import eu.cdevreeze.tqa.base.relationship.HasHypercubeRelationship
import eu.cdevreeze.tqa.base.taxonomy.BasicTaxonomy
import eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyBuilder
import eu.cdevreeze.yaidom.core.EName

/**
 * Program that shows dimensional data in a given taxonomy.
 *
 * @author Chris de Vreeze
 */
object ShowDimensions {

  private val logger = Logger.getGlobal

  def main(args: Array[String]): Unit = {
    require(args.size >= 2, s"Usage: ShowDimensions <taxonomy package ZIP file> <entry point URI 1> ...")
    val zipInputFile: File = new File(args(0)).ensuring(_.isFile)
    val zipFile = new ZipFile(zipInputFile)

    val entryPointUris = args.drop(1).map(u => URI.create(u)).toSet
    val useSaxon = System.getProperty("useSaxon", "false").toBoolean
    val lenient = System.getProperty("lenient", "false").toBoolean
    val useZipStreams = System.getProperty("useZipStreams", "false").toBoolean

    logger.info(s"Starting building the DTS with entry point(s) ${entryPointUris.mkString(", ")}")

    val basicTaxo: BasicTaxonomy =
      (if (useZipStreams) {
         ConsoleUtil.createTaxonomyFromZipStreams(
           entryPointUris,
           () => new ZipInputStream(new FileInputStream(zipInputFile)),
           lenient)
       } else {
         val taxoBuilder: TaxonomyBuilder =
           ConsoleUtil.createTaxonomyBuilder(zipFile, useSaxon, lenient)
         taxoBuilder.build(entryPointUris)
       }).tap(_ => zipFile.close) // Not robust

    val rootElems = basicTaxo.taxonomyBase.rootElems

    logger.info(s"The taxonomy has ${rootElems.size} taxonomy root elements")
    logger.info(s"The taxonomy has ${basicTaxo.relationships.size} relationships")
    logger.info(s"The taxonomy has ${basicTaxo.findAllDimensionalRelationships.size} dimensional relationships")

    val hasHypercubes = basicTaxo.findAllHasHypercubeRelationships

    val hasHypercubeGroupsWithSameKey =
      hasHypercubes.distinct.groupBy(hh => (hh.primary, hh.elr)).filter(_._2.size >= 2)

    logger.info(
      s"Number of has_hypercube groups with more than 1 has-hypercube for the same primary and ELR: ${hasHypercubeGroupsWithSameKey.size}")

    showHasHypercubeTrees(hasHypercubes, basicTaxo)

    // Concrete primary items and inheritance of has-hypercubes

    showHasHypercubeInheritance(hasHypercubes, basicTaxo)

    logger.info("Ready")
  }

  private def showHasHypercubeTrees(
      hasHypercubes: immutable.IndexedSeq[HasHypercubeRelationship],
      basicTaxo: BasicTaxonomy): Unit = {
    hasHypercubes.foreach { hasHypercube =>
      println(s"Has-hypercube found for primary ${hasHypercube.primary} and ELR ${hasHypercube.elr}")

      val hypercubeDimensions = basicTaxo.findAllConsecutiveHypercubeDimensionRelationships(hasHypercube)
      val dimensions = hypercubeDimensions.map(_.dimension).distinct.sortBy(_.toString)
      println(s"It has dimensions: ${dimensions.mkString("\n\t", ",\n\t", "")}")

      val usableDimensionMembers: Map[EName, Set[EName]] = basicTaxo.findAllUsableDimensionMembers(hasHypercube)

      usableDimensionMembers.toSeq.sortBy(_._1.toString).foreach {
        case (dimension, members) =>
          println(s"\tDimension: $dimension. Usable members:")

          members.toSeq.sortBy(_.toString).foreach { member =>
            println(s"\t\t$member")
          }
      }
    }
  }

  // scalastyle:off method.length
  private def showHasHypercubeInheritance(
      hasHypercubes: immutable.IndexedSeq[HasHypercubeRelationship],
      basicTaxo: BasicTaxonomy): Unit = {
    val concretePrimaryItemDecls = basicTaxo.findAllPrimaryItemDeclarations.filter(_.isConcrete)

    val concretePrimariesNotInheritingOrHavingHasHypercube = concretePrimaryItemDecls
      .filter { itemDecl =>
        basicTaxo.findAllOwnOrInheritedHasHypercubes(itemDecl.targetEName).isEmpty
      }
      .map(_.targetEName)

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
      hasHypercubes
        .flatMap { hh =>
          basicTaxo.filterOutgoingConsecutiveDomainMemberRelationshipPaths(hh.primary) { path =>
            path.firstRelationship.elr == hh.elr
          }
        }
        .flatMap(_.relationships.map(_.member))
        .toSet

    val ownPrimaries = hasHypercubes.map(_.sourceConceptEName).toSet
    val inheritingOrOwnPrimaries = inheritingPrimaries.union(ownPrimaries)
    val inheritingOrOwnConcretePrimaries =
      inheritingOrOwnPrimaries.filter(item => basicTaxo.findPrimaryItemDeclaration(item).exists(_.isConcrete))

    require(
      concretePrimariesNotInheritingOrHavingHasHypercube.toSet ==
        concretePrimaryItemDecls.map(_.targetEName).toSet.diff(inheritingOrOwnConcretePrimaries),
      s"Finding concrete primary items not inheriting or having any has-hypercube from 2 directions must give the same result"
    )

    logger.info("Showing inheriting concrete items")

    val hasHypercubeInheritanceOrSelf = basicTaxo.computeHasHypercubeInheritanceOrSelfReturningElrToPrimariesMaps

    concretePrimaryItemDecls.map(_.targetEName).distinct.sortBy(_.toString).foreach { item =>
      val hasHypercubes = basicTaxo.findAllOwnOrInheritedHasHypercubes(item)
      val elrPrimariesPairs = hasHypercubes.groupBy(_.elr).view.mapValues(_.map(_.primary)).toSeq.sortBy(_._1)

      require(
        elrPrimariesPairs.toMap.view.mapValues(_.toSet).toMap == hasHypercubeInheritanceOrSelf
          .getOrElse(item, Map.empty),
        s"Finding own or inherited has-hypercubes must be consistent with the bulk methods for has-hypercube inheritance-or-self"
      )

      elrPrimariesPairs.foreach {
        case (elr, primaries) =>
          println(
            s"Concrete item $item inherits or has has-hypercubes for ELR $elr and with primaries ${primaries.distinct.sortBy(_.toString).mkString(", ")}")
      }
    }
  }
}
