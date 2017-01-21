===
TQA
===

Improved leaner XBRL Taxonomy Query API (TQA). There are just 3 layers: taxonomy DOM, relationships, and taxonomies
that combine the 2 (as a taxonomy query API).

It knows about taxonomy data as type-safe XML in an XBRL context, supporting several different DOM abstractions
(core/dimensional, formula, table etc.), and it knows about relationships connecting these type-safe DOM elements.

It can be used for validating XBRL instances, including (pluggable) schema validation of XBRL instances.

It is immutable if the underlying yaidom elements are immutable. The underlying backing elements can be any BackingElemApi implementation.

It understands networks of relationships, and prohibition/overriding.

It tries to keep memory footprint low, even if the underlying XML is always available. One trick to minimize memory footprint is almost emptying label/reference linkbases without breaking DTS discovery.

Examples can be written against this API in tutorials explaining XBRL (for example dimensional validation) to developers.

Ad-hoc querying of taxonomy data must be made very easy with this TQA.

Creation of taxonomy DOM elements or of relationships should never fail, so that TQA can be used for taxonomy validation
as well. Instance methods on DOM elements, relationships and taxonomies can fail however, so it may be needed to
query at a lower level of abstraction if the loaded taxonomy has not at all been validated yet.

Technically, there is a clear distinction between data and behavior, except maybe in the taxonomy Scala package. Relationships
do not carry around entire taxonomies as context, but use the ResolvedLocatorOrResource abstraction instead. Hence, creation
of a relationship from scratch is feasible now.

Some design choices in this TQA are:

* Relationships and concept declarations can be queried on the same "taxonomy" object, without unwrapping
* We can build rich wrappers around taxonomy objects, but the same query API (plus more) is retained (think: decorator pattern)
* The query API is clear and sufficiently easy to use from a REPL session
* Objects that are expensive to create are created by a factory method with an appropriate name, making the constructor sufficiently private
* Most objects can be created even if the taxonomy is invalid, so TQA can also be used for validating taxonomies
* In particular, incomplete taxonomies can be created, as long as we pass knowledge about known substitution groups along
* It is up to the user of the API if taxonomy objects are created in a very lenient way or a very strict way
* DTS discovery can be tweaked, and so can post-processing of parsed taxonomy documents
* Finding relationships and schema content on concept (target) expanded names is fast
* The backing element implementations can be native yaidom but can also be Saxon tiny trees!
