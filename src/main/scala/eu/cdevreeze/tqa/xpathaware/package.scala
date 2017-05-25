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
 * XPath-aware taxonomy querying support. In TQA no taxonomy XPath awareness is anywhere outside of this namespace.
 * This implies that all of TQA outside of this namespace (and the xpath namespace) can be used without the need for
 * any XPath processor. Everything inside this namespace does need an XPath processor, though.
 *
 * Dependency on XPath processing for taxonomy querying and processing is not without its costs:
 * <ul>
 * <li>Some XPath expressions may be somewhat brittle and give inconsistent results among XPath processors</li>
 * <li>All prefixes used in XPath expressions must be bound to namespaces</li>
 * <li>The backing element implementation used is coupled to the XPath processor used. In practice, use Saxon for XPath processing and backing elements</li>
 * <li>Configuration of the XPath processor may be sensitive, not only w.r.t. namespaces, but also w.r.t. the base URI, etc.</li>
 * </ul>
 *
 * @author Chris de Vreeze
 */
package object xpathaware
