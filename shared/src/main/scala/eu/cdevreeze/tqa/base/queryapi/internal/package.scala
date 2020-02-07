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

package eu.cdevreeze.tqa.base.queryapi

/**
 * Relationship query APIs, with implementations. These APIs are internal, used by the taxonomy query API traits, but may be used
 * in application code as well. These APIs depend on very limited state (input relationship collection or map). If these input
 * relationships are small collections, performance of the query API typically improves.
 *
 * @author Chris de Vreeze
 */
package object internal
