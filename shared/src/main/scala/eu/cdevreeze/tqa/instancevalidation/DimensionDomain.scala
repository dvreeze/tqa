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

package eu.cdevreeze.tqa.instancevalidation

import eu.cdevreeze.yaidom.core.EName

/**
 * Dimension domain, optimized for dimensional instance validation.
 *
 * @author Chris de Vreeze
 */
final class DimensionDomain(
    val dimension: EName,
    val dimensionDomainElr: String,
    val members: Map[EName, DimensionDomain.Member]) {

  require(members.forall(kv => kv._2.ename == kv._1), s"Corrupt dimension domain")
}

object DimensionDomain {

  final case class Member(ename: EName, usable: Boolean)
}
