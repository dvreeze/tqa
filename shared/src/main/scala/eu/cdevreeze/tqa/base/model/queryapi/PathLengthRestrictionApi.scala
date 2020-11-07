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

package eu.cdevreeze.tqa.base.model.queryapi

/**
 * API used by "relationship path" query API calls, to limit the length of paths that
 * have cycles in them.
 *
 * @author Chris de Vreeze
 */
trait PathLengthRestrictionApi {

  /**
   * Returns the max length of a path beyond the first cycle found in the path, if any.
   */
  def maxPathLengthBeyondCycle: Int
}
