===
TQA
===

Improved leaner XBRL Taxonomy Query API (TQA).

It knows about taxonomy data as type-safe XML in an XBRL context, supporting several levels of DOM abstractions. It knows about resolved relationships.

It can be used for validating XBRL instances, including (pluggable) schema validation of XBRL instances.

It is immutable if the underlying yaidom elements are immutable. The underlying backing elements can be any BackingElemApi implementation.

It understands networks of relationships, and prohibition/overriding.

It tries to keep memory footprint low, even if the underlying XML is always available. One trick to minimize memory footprint is almost emptying label/reference linkbases without breaking DTS discovery.

Examples can be written against this API in tutorials explaining XBRL (for example dimensional validation) to developers.

Ad-hoc querying of taxonomy data must be made very easy with this TQA.
