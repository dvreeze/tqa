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

package eu.cdevreeze.tqa.extension.table

/**
 * Type-safe XBRL '''table linkbase relationship extension'''. This package contains models for relationships
 * in a table linkbase context.
 *
 * Like for the normal relationship model, instantiation of the table relationship extensions should be successful
 * even if the table linkbase content is not schema-valid, but the less forgiving query methods should be used defensively
 * on potentially non-schema-valid table linkbase content.
 *
 * @author Chris de Vreeze
 */
package object relationship
