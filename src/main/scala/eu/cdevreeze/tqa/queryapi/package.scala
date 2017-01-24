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
 * Traits offering parts of a taxonomy query API. They can be assembled into "taxonomy classes".
 * There are purely abstract query API traits, and partial implementations of those traits.
 *
 * Examples of such traits are traits for querying schema content, for querying inter-concept relationships,
 * for querying dimensional relationships in particular, etc.
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
 * @author Chris de Vreeze
 */
package object queryapi
