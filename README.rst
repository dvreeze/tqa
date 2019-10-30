===
TQA
===

**This is a fork of dvreeze/tqa. Currently this fork is in a state of flux. It will soon change the package names, among other things. Hence, until further notice the original at dvreeze/tqa should be used instead of this fork.**

XBRL Taxonomy Query API (TQA). It is based on the yaidom XML query API, and offers a Scala Collections processing
experience.

TQA contains 3 layers. The lowest layer is the taxonomy DOM, offering a more type-safe yaidom querying experience for
taxonomy data (linkbase content and taxonomy schema content). On top of that is the relationship layer, where XLink
arcs of the lowest layer are "resolved". On top of that is the layer of the taxonomy query API itself, which returns
relationship and type-safe DOM data.

Some design requirements are:

* The TQA query API is easy to learn for developers who know XBRL (Core and Dimensions), Scala and yaidom.
* It must be clear (to those developers), sufficiently complete but still as small as feasible.
* The scope of TQA is mainly querying (Core and Dimensional) taxonomy content; yet, it does know about XPath, but only in its "outside layer".
* TQA knows about networks of relationships, and about prohibition/overriding.
* It knows about XML base, embedded linkbases, XPointer (as used in an XBRL context), etc.
* The same query API is useful for very different use cases where taxonomy data is queried (ranging from very "lenient" to very "strict").
* The backing DOM implementation (exposed via the yaidom query API) is pluggable.
* TQA is immutable and thread-safe if the underlying DOM implementations are immutable and thread-safe.
* TQA is extensible; for example, formula and table support is included via "extensions".

TQA being a type-safe taxonomy (and instance) model makes it quite useful as a basis for more interesting applications like XBRL validators.
To a large extent TQA can help reduce the size and "conceptual weight" of an XBRL validator code base.

Some use cases where TQA must be useful are:

* Representing an XBRL-valid DTS, when validating an XBRL instance against it.
* Representing a potentially XBRL-invalid DTS, when checking it for XBRL validity.
* Representing a non-closed arbitrary collection of taxonomy documents, when validating those documents against some "best practices".
* Creating test taxonomies from "templates". This requires that TQA models are sufficiently easy to (functionally) update.
* Representing a potentially huge "all-entrypoint" DTS, for certain types of reports.
* Ad-hoc querying of taxonomies.

Note that non-closed arbitrary collection of taxonomy documents only become somewhat useful if sufficient knowledge about
substitution groups is provided as well.

The backing DOM implementation is pluggable for a reason. In production code, a yaidom wrapper around Saxon tiny trees
can be very attractive for its performance characteristics. In particular, the memory footprint of Saxon tiny trees is
very low. In test code, on the other hand, native yaidom DOM implementations can be handy because they are easier to
debug. In both cases the backing DOM trees are immutable, and by extension the TQA models are immutable as well.

TQA should be a good vehicle for explaining XBRL (for example dimensional validation) to developers.

TODO Can TQA itself become a good basis for (pluggable) schema validation as well?
