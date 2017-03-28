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

package eu.cdevreeze.tqa

/**
 * Traits offering parts of a '''taxonomy query API'''. They can be assembled into "taxonomy classes".
 * There are purely abstract query API traits, and partial implementations of those traits.
 *
 * Examples of such traits are traits for querying schema content, for querying inter-concept relationships,
 * for querying dimensional relationships in particular, etc. These traits combined form the
 * [[eu.cdevreeze.tqa.queryapi.TaxonomyApi]] query API. The partial implementations combined form the
 * [[eu.cdevreeze.tqa.queryapi.TaxonomyLike]] trait, which implements most of the `TaxonomyApi` query API.
 *
 * Most query API methods are quite forgiving when the taxonomy is incomplete or incorrect. They just
 * return the queried data to the extent that it is found. Only the getXXX methods that expect precisely
 * one result will throw an exception if no (single) result is found.
 *
 * Ideally, the taxonomy query API is very easy to use for XBRL taxonomy scripting tasks in a Scala REPL. It must
 * also be easy to mix taxonomy query API traits, and to compose taxonomy implementations that know about specific
 * relationship types (such as in formulas or tables), or that store specific data that is queried quite often.
 *
 * TQA has no knowledge about XPath, so any XPath in the taxonomy is just text, as far as TQA is concerned.
 *
 * This package unidirectionally depends on the [[eu.cdevreeze.tqa.relationship]] and [[eu.cdevreeze.tqa.dom]] packages.
 *
 * ==Usage===
 *
 * In the following examples, assume that we have a `taxonomy` of type [[eu.cdevreeze.tqa.queryapi.TaxonomyApi]],
 * for example a [[eu.cdevreeze.tqa.taxonomy.BasicTaxonomy]]. It may or may not be closed under "DTS discovery rules".
 * The examples show how the taxonomy query API, along with the types in packages [[eu.cdevreeze.tqa.relationship]] and
 * [[eu.cdevreeze.tqa.dom]] can be used to query XBRL taxonomies.
 *
 * Suppose we want to query the taxonomy for all English verbose concept labels, grouped by the concept target `EName`.
 * Note that the "target EName" of a concept declaration is the name attribute along with the target namespace of the
 * schema, as yaidom EName. Here is how we can get the concept labels:
 *
 * {{{
 * import scala.reflect.classTag
 * import eu.cdevreeze.yaidom.core.EName
 * import eu.cdevreeze.tqa.relationship.ConceptLabelRelationship
 *
 * val concepts: Set[EName] =
 *   taxonomy.findAllConceptDeclarations.map(_.targetEName).toSet
 *
 * val conceptLabelRelationshipsByConceptEName = (concepts.toIndexedSeq map { conceptEName =>
 *   val conceptLabelRels =
 *     taxonomy.filterOutgoingStandardRelationshipsOfType(
 *       conceptEName,
 *       classTag[ConceptLabelRelationship]) { rel =>
 *
 *       rel.language == "en" && rel.resourceRole == "http://www.xbrl.org/2003/role/verboseLabel"
 *     }
 *
 *   (conceptEName -> conceptLabelRels)
 * }).toMap
 *
 * val verboseEnConceptLabels: Map[EName, Set[String]] =
 *   conceptLabelRelationshipsByConceptEName mapValues { rels =>
 *     rels.map(_.labelText).toSet
 *   }
 * }}}
 *
 * In the example above, each concept should have at most one English verbose label, unless relationship
 * prohibition/overriding is used. Validating this is an exercise for the reader, as they say. Note that in the example
 * above, we mainly used the following knowledge about XBRL taxonomies: we get concept-labels by querying for concept-label
 * relationships, which are standard relationships.
 *
 * Now suppose we want to find all English terse concept labels, grouped by the concept target `EName`, but only for
 * concrete primary items. So we exclude abstract concepts, tuples, hypercubes and dimensions. Here is how:
 *
 * {{{
 * val concepts: Set[EName] =
 *   taxonomy.filterPrimaryItemDeclarations(_.isConcrete).map(_.targetEName).toSet
 *
 * val conceptLabelRelationshipsByConceptEName = (concepts.toIndexedSeq map { conceptEName =>
 *   val conceptLabelRels =
 *     taxonomy.filterOutgoingStandardRelationshipsOfType(
 *       conceptEName,
 *       classTag[ConceptLabelRelationship]) { rel =>
 *
 *       rel.language == "en" && rel.resourceRole == "http://www.xbrl.org/2003/role/terseLabel"
 *     }
 *
 *   (conceptEName -> conceptLabelRels)
 * }).toMap
 *
 * val terseEnConceptLabels: Map[EName, Set[String]] =
 *   conceptLabelRelationshipsByConceptEName mapValues { rels =>
 *     rels.map(_.labelText).toSet
 *   }
 * }}}
 *
 * To simulate how TQA retrieves concrete primary item declarations, we could write more verbosely:
 *
 * {{{
 * import eu.cdevreeze.tqa.dom.ConceptDeclaration
 * import eu.cdevreeze.tqa.dom.PrimaryItemDeclaration
 *
 * val substitutionGroupMap = taxonomy.substitutionGroupMap
 * val conceptDeclarationBuilder = new ConceptDeclaration.Builder(substitutionGroupMap)
 *
 * val concepts: Set[EName] =
 *   taxonomy.filterGlobalElementDeclarations(_.isConcrete).
 *     flatMap(decl => conceptDeclarationBuilder.optConceptDeclaration(decl)).
 *     collect({ case decl: PrimaryItemDeclaration => decl }).map(_.targetEName).toSet
 * }}}
 *
 * To simulate how TQA filters the concept label relationships we are interested in, we could write more verbosely:
 *
 * {{{
 * import eu.cdevreeze.tqa.ENames
 *
 * val conceptLabelRelationshipsByConceptEName = (concepts.toIndexedSeq map { conceptEName =>
 *   val conceptLabelRels =
 *     taxonomy.filterOutgoingStandardRelationshipsOfType(
 *       conceptEName,
 *       classTag[ConceptLabelRelationship]) { rel =>
 *
 *       rel.resolvedTo.resolvedElem.attribute(ENames.XmlLangEName) == "en" &&
 *         rel.resolvedTo.resolvedElem.attributeOption(ENames.XLinkRoleEName) == Some("http://www.xbrl.org/2003/role/terseLabel")
 *     }
 *
 *   (conceptEName -> conceptLabelRels)
 * }).toMap
 *
 * val terseEnConceptLabels: Map[EName, Set[String]] =
 *   conceptLabelRelationshipsByConceptEName mapValues { rels =>
 *     rels.map(_.resolvedTo.resolvedElem.text).toSet
 *   }
 * }}}
 *
 * Suppose we want to query the taxonomy for the parent-child presentation hierarchies in some custom ELR (extended link role).
 * Note that the result can come from multiple linkbase documents, and TQA takes care of that if we query for parent-child
 * relationships instead of querying for the underlying XLink presentation arcs. Here is how we get the top 2 levels:
 *
 * {{{
 * import scala.collection.immutable
 * import eu.cdevreeze.tqa.relationship.ParentChildRelationship
 *
 * val parentChildRelationships =
 *   taxonomy.filterInterConceptRelationshipsOfType(classTag[ParentChildRelationship]) { rel =>
 *     rel.elr == customElr
 *   }
 *
 * val topLevelConcepts: Set[EName] =
 *   parentChildRelationships.map(_.sourceConceptEName).toSet.diff(
 *     parentChildRelationships.map(_.targetConceptEName).toSet)
 *
 * val topLevelParentChildren: Map[EName, immutable.IndexedSeq[EName]] =
 *   (topLevelConcepts.toIndexedSeq map { conceptEName =>
 *     val parentChildren =
 *       taxonomy.filterOutgoingInterConceptRelationshipsOfType(
 *         conceptEName,
 *         classTag[ParentChildRelationship]) { rel =>
 *
 *         rel.elr == customElr
 *       }
 *
 *     val childENames = parentChildren.sortBy(_.order).map(_.targetConceptEName)
 *
 *     (conceptEName -> childENames)
 *   }).toMap
 * }}}
 *
 * These examples only scratch the surface of what is possible. Dimensional relationship queries are typically more
 * interesting than the examples above, for example. See [[eu.cdevreeze.tqa.queryapi.DimensionalRelationshipContainerApi]]
 * for the dimensional query API that is part of [[eu.cdevreeze.tqa.queryapi.TaxonomyApi]].
 *
 * ==Notes on performance==
 *
 * The performance characteristics of the [[eu.cdevreeze.tqa.queryapi.TaxonomyApi]] trait and its implementations
 * partially depend on the concrete "taxonomy" class used. Still we can say in general that:
 *
 * <ul>
 * <li>Querying for concept declarations and schema components in general, based on the "target EName", is very fast.</li>
 * <li>Other than that, querying for concept declarations and schema components in general is slow.</li>
 * <li>Querying for outgoing and incoming standard relationships, given a concept EName, is very fast.</li>
 * <li>Other than that, querying for relationships is in general slow.</li>
 * </ul>
 *
 * In other words, in "inner loops", do not query for taxonomy content other than querying based on specific concept ENames!
 * Note that in the examples above, we started with a slow query, and used fast queries based on concept ENames after that.
 * Keep in mind that in taxonomies with millions of relationships the slow queries may have to filter in collections of all
 * those relationships.
 *
 * @author Chris de Vreeze
 */
package object queryapi
