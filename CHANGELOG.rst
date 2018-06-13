=========
CHANGELOG
=========


0.8.4
=====

Compared with release 0.8.3, the main changes in this version are:

* The ("XML-free") models for formulas and tables now hold the optional IDs for "resources"
* Added class ``Relationship.UniqueKey``

Breaking changes compared to version 0.8.3 (in SBT, run: tqaJVM/*:mimaReportBinaryIssues):

* the type hierarchy of object eu.cdevreeze.tqa.extension.formula.model.SegmentFilter is different in current version. Missing types {scala.runtime.AbstractFunction1}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.formula.model.SegmentFilter$")
* method apply(scala.Option)eu.cdevreeze.tqa.extension.formula.model.SegmentFilter in object eu.cdevreeze.tqa.extension.formula.model.SegmentFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.SegmentFilter.apply")
* the type hierarchy of object eu.cdevreeze.tqa.extension.formula.model.ExistenceAssertion is different in current version. Missing types {scala.runtime.AbstractFunction6}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.formula.model.ExistenceAssertion$")
* method apply(Boolean,eu.cdevreeze.tqa.aspect.AspectModel,scala.Option,scala.collection.immutable.IndexedSeq,scala.collection.immutable.IndexedSeq,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.extension.formula.model.ExistenceAssertion in object eu.cdevreeze.tqa.extension.formula.model.ExistenceAssertion does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.ExistenceAssertion.apply")
* abstract method idOption()scala.Option in interface eu.cdevreeze.tqa.extension.formula.model.Resource is inherited by class VariableOrParameter in current version.
  filter with: ProblemFilters.exclude[InheritedNewAbstractMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.Resource.idOption")
* abstract method idOption()scala.Option in interface eu.cdevreeze.tqa.extension.formula.model.Resource is inherited by class MatchFilter in current version.
  filter with: ProblemFilters.exclude[InheritedNewAbstractMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.Resource.idOption")
* synthetic method copy$default$2()Boolean in class eu.cdevreeze.tqa.extension.formula.model.MatchConceptFilter has a different result type in current version, where it is eu.cdevreeze.yaidom.core.EName rather than Boolean
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchConceptFilter.copy$default$2")
* method copy(eu.cdevreeze.yaidom.core.EName,Boolean)eu.cdevreeze.tqa.extension.formula.model.MatchConceptFilter in class eu.cdevreeze.tqa.extension.formula.model.MatchConceptFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchConceptFilter.copy")
* synthetic method copy$default$1()eu.cdevreeze.yaidom.core.EName in class eu.cdevreeze.tqa.extension.formula.model.MatchConceptFilter has a different result type in current version, where it is scala.Option rather than eu.cdevreeze.yaidom.core.EName
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchConceptFilter.copy$default$1")
* method this(eu.cdevreeze.yaidom.core.EName,Boolean)Unit in class eu.cdevreeze.tqa.extension.formula.model.MatchConceptFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchConceptFilter.this")
* the type hierarchy of object eu.cdevreeze.tqa.extension.formula.model.LocationFilter is different in current version. Missing types {scala.runtime.AbstractFunction2}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.formula.model.LocationFilter$")
* method apply(eu.cdevreeze.yaidom.core.EName,eu.cdevreeze.tqa.ScopedXPathString)eu.cdevreeze.tqa.extension.formula.model.LocationFilter in object eu.cdevreeze.tqa.extension.formula.model.LocationFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.LocationFilter.apply")
* the type hierarchy of object eu.cdevreeze.tqa.extension.formula.model.MatchLocationFilter is different in current version. Missing types {scala.runtime.AbstractFunction2}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchLocationFilter$")
* method apply(eu.cdevreeze.yaidom.core.EName,Boolean)eu.cdevreeze.tqa.extension.formula.model.MatchLocationFilter in object eu.cdevreeze.tqa.extension.formula.model.MatchLocationFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchLocationFilter.apply")
* abstract method idOption()scala.Option in interface eu.cdevreeze.tqa.extension.formula.model.Resource is inherited by class UnitFilter in current version.
  filter with: ProblemFilters.exclude[InheritedNewAbstractMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.Resource.idOption")
* the type hierarchy of object eu.cdevreeze.tqa.extension.formula.model.Parameter is different in current version. Missing types {scala.runtime.AbstractFunction4}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.formula.model.Parameter$")
* method apply(eu.cdevreeze.yaidom.core.EName,scala.Option,scala.Option,scala.Option)eu.cdevreeze.tqa.extension.formula.model.Parameter in object eu.cdevreeze.tqa.extension.formula.model.Parameter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.Parameter.apply")
* abstract method idOption()scala.Option in interface eu.cdevreeze.tqa.extension.formula.model.Resource is inherited by class Filter in current version.
  filter with: ProblemFilters.exclude[InheritedNewAbstractMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.Resource.idOption")
* synthetic method copy$default$2()scala.Option in class eu.cdevreeze.tqa.extension.formula.model.PeriodEndFilter has a different result type in current version, where it is eu.cdevreeze.tqa.ScopedXPathString rather than scala.Option
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.PeriodEndFilter.copy$default$2")
* method copy(eu.cdevreeze.tqa.ScopedXPathString,scala.Option)eu.cdevreeze.tqa.extension.formula.model.PeriodEndFilter in class eu.cdevreeze.tqa.extension.formula.model.PeriodEndFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.PeriodEndFilter.copy")
* synthetic method copy$default$1()eu.cdevreeze.tqa.ScopedXPathString in class eu.cdevreeze.tqa.extension.formula.model.PeriodEndFilter has a different result type in current version, where it is scala.Option rather than eu.cdevreeze.tqa.ScopedXPathString
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.PeriodEndFilter.copy$default$1")
* method this(eu.cdevreeze.tqa.ScopedXPathString,scala.Option)Unit in class eu.cdevreeze.tqa.extension.formula.model.PeriodEndFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.PeriodEndFilter.this")
* method copy(eu.cdevreeze.tqa.ENameValueOrExpr)eu.cdevreeze.tqa.extension.formula.model.SingleMeasureFilter in class eu.cdevreeze.tqa.extension.formula.model.SingleMeasureFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.SingleMeasureFilter.copy")
* synthetic method copy$default$1()eu.cdevreeze.tqa.ENameValueOrExpr in class eu.cdevreeze.tqa.extension.formula.model.SingleMeasureFilter has a different result type in current version, where it is scala.Option rather than eu.cdevreeze.tqa.ENameValueOrExpr
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.SingleMeasureFilter.copy$default$1")
* method this(eu.cdevreeze.tqa.ENameValueOrExpr)Unit in class eu.cdevreeze.tqa.extension.formula.model.SingleMeasureFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.SingleMeasureFilter.this")
* method copy(eu.cdevreeze.tqa.ENameValueOrExpr)eu.cdevreeze.tqa.extension.formula.model.ParentFilter in class eu.cdevreeze.tqa.extension.formula.model.ParentFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.ParentFilter.copy")
* synthetic method copy$default$1()eu.cdevreeze.tqa.ENameValueOrExpr in class eu.cdevreeze.tqa.extension.formula.model.ParentFilter has a different result type in current version, where it is scala.Option rather than eu.cdevreeze.tqa.ENameValueOrExpr
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.ParentFilter.copy$default$1")
* method this(eu.cdevreeze.tqa.ENameValueOrExpr)Unit in class eu.cdevreeze.tqa.extension.formula.model.ParentFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.ParentFilter.this")
* synthetic method copy$default$2()Boolean in class eu.cdevreeze.tqa.extension.formula.model.MatchSegmentFilter has a different result type in current version, where it is eu.cdevreeze.yaidom.core.EName rather than Boolean
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchSegmentFilter.copy$default$2")
* method copy(eu.cdevreeze.yaidom.core.EName,Boolean)eu.cdevreeze.tqa.extension.formula.model.MatchSegmentFilter in class eu.cdevreeze.tqa.extension.formula.model.MatchSegmentFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchSegmentFilter.copy")
* synthetic method copy$default$1()eu.cdevreeze.yaidom.core.EName in class eu.cdevreeze.tqa.extension.formula.model.MatchSegmentFilter has a different result type in current version, where it is scala.Option rather than eu.cdevreeze.yaidom.core.EName
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchSegmentFilter.copy$default$1")
* method this(eu.cdevreeze.yaidom.core.EName,Boolean)Unit in class eu.cdevreeze.tqa.extension.formula.model.MatchSegmentFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchSegmentFilter.this")
* abstract method idOption()scala.Option in interface eu.cdevreeze.tqa.extension.formula.model.Resource is inherited by class PeriodAspectFilter in current version.
  filter with: ProblemFilters.exclude[InheritedNewAbstractMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.Resource.idOption")
* synthetic method copy$default$6()scala.Option in class eu.cdevreeze.tqa.extension.formula.model.Formula has a different result type in current version, where it is scala.collection.immutable.IndexedSeq rather than scala.Option
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.Formula.copy$default$6")
* synthetic method copy$default$2()eu.cdevreeze.tqa.aspect.AspectModel in class eu.cdevreeze.tqa.extension.formula.model.Formula has a different result type in current version, where it is Boolean rather than eu.cdevreeze.tqa.aspect.AspectModel
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.Formula.copy$default$2")
* synthetic method copy$default$5()scala.collection.immutable.IndexedSeq in class eu.cdevreeze.tqa.extension.formula.model.Formula has a different result type in current version, where it is eu.cdevreeze.tqa.ScopedXPathString rather than scala.collection.immutable.IndexedSeq
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.Formula.copy$default$5")
* synthetic method copy$default$8()scala.collection.immutable.IndexedSeq in class eu.cdevreeze.tqa.extension.formula.model.Formula has a different result type in current version, where it is scala.Option rather than scala.collection.immutable.IndexedSeq
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.Formula.copy$default$8")
* method copy(Boolean,eu.cdevreeze.tqa.aspect.AspectModel,scala.Option,eu.cdevreeze.tqa.ScopedXPathString,scala.collection.immutable.IndexedSeq,scala.Option,scala.Option,scala.collection.immutable.IndexedSeq,scala.collection.immutable.IndexedSeq,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.extension.formula.model.Formula in class eu.cdevreeze.tqa.extension.formula.model.Formula does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.Formula.copy")
* synthetic method copy$default$1()Boolean in class eu.cdevreeze.tqa.extension.formula.model.Formula has a different result type in current version, where it is scala.Option rather than Boolean
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.Formula.copy$default$1")
* synthetic method copy$default$4()eu.cdevreeze.tqa.ScopedXPathString in class eu.cdevreeze.tqa.extension.formula.model.Formula has a different result type in current version, where it is scala.Option rather than eu.cdevreeze.tqa.ScopedXPathString
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.Formula.copy$default$4")
* synthetic method copy$default$3()scala.Option in class eu.cdevreeze.tqa.extension.formula.model.Formula has a different result type in current version, where it is eu.cdevreeze.tqa.aspect.AspectModel rather than scala.Option
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.Formula.copy$default$3")
* method this(Boolean,eu.cdevreeze.tqa.aspect.AspectModel,scala.Option,eu.cdevreeze.tqa.ScopedXPathString,scala.collection.immutable.IndexedSeq,scala.Option,scala.Option,scala.collection.immutable.IndexedSeq,scala.collection.immutable.IndexedSeq,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.extension.formula.model.Formula does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.Formula.this")
* the type hierarchy of object eu.cdevreeze.tqa.extension.formula.model.ParentFilter is different in current version. Missing types {scala.runtime.AbstractFunction1}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.formula.model.ParentFilter$")
* method apply(eu.cdevreeze.tqa.ENameValueOrExpr)eu.cdevreeze.tqa.extension.formula.model.ParentFilter in object eu.cdevreeze.tqa.extension.formula.model.ParentFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.ParentFilter.apply")
* synthetic method copy$default$2()eu.cdevreeze.tqa.ScopedXPathString in class eu.cdevreeze.tqa.extension.formula.model.LocationFilter has a different result type in current version, where it is eu.cdevreeze.yaidom.core.EName rather than eu.cdevreeze.tqa.ScopedXPathString
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.LocationFilter.copy$default$2")
* method copy(eu.cdevreeze.yaidom.core.EName,eu.cdevreeze.tqa.ScopedXPathString)eu.cdevreeze.tqa.extension.formula.model.LocationFilter in class eu.cdevreeze.tqa.extension.formula.model.LocationFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.LocationFilter.copy")
* synthetic method copy$default$1()eu.cdevreeze.yaidom.core.EName in class eu.cdevreeze.tqa.extension.formula.model.LocationFilter has a different result type in current version, where it is scala.Option rather than eu.cdevreeze.yaidom.core.EName
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.LocationFilter.copy$default$1")
* method this(eu.cdevreeze.yaidom.core.EName,eu.cdevreeze.tqa.ScopedXPathString)Unit in class eu.cdevreeze.tqa.extension.formula.model.LocationFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.LocationFilter.this")
* synthetic method copy$default$2()Boolean in class eu.cdevreeze.tqa.extension.formula.model.MatchUnitFilter has a different result type in current version, where it is eu.cdevreeze.yaidom.core.EName rather than Boolean
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchUnitFilter.copy$default$2")
* method copy(eu.cdevreeze.yaidom.core.EName,Boolean)eu.cdevreeze.tqa.extension.formula.model.MatchUnitFilter in class eu.cdevreeze.tqa.extension.formula.model.MatchUnitFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchUnitFilter.copy")
* synthetic method copy$default$1()eu.cdevreeze.yaidom.core.EName in class eu.cdevreeze.tqa.extension.formula.model.MatchUnitFilter has a different result type in current version, where it is scala.Option rather than eu.cdevreeze.yaidom.core.EName
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchUnitFilter.copy$default$1")
* method this(eu.cdevreeze.yaidom.core.EName,Boolean)Unit in class eu.cdevreeze.tqa.extension.formula.model.MatchUnitFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchUnitFilter.this")
* the type hierarchy of object eu.cdevreeze.tqa.extension.formula.model.ConceptDataTypeFilter is different in current version. Missing types {scala.runtime.AbstractFunction2}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.formula.model.ConceptDataTypeFilter$")
* method apply(eu.cdevreeze.tqa.ENameValueOrExpr,Boolean)eu.cdevreeze.tqa.extension.formula.model.ConceptDataTypeFilter in object eu.cdevreeze.tqa.extension.formula.model.ConceptDataTypeFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.ConceptDataTypeFilter.apply")
* the type hierarchy of object eu.cdevreeze.tqa.extension.formula.model.Precondition is different in current version. Missing types {scala.runtime.AbstractFunction1}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.formula.model.Precondition$")
* method apply(eu.cdevreeze.tqa.ScopedXPathString)eu.cdevreeze.tqa.extension.formula.model.Precondition in object eu.cdevreeze.tqa.extension.formula.model.Precondition does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.Precondition.apply")
* method copy(eu.cdevreeze.yaidom.core.EName,eu.cdevreeze.yaidom.core.EName,Boolean)eu.cdevreeze.tqa.extension.formula.model.MatchDimensionFilter in class eu.cdevreeze.tqa.extension.formula.model.MatchDimensionFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchDimensionFilter.copy")
* synthetic method copy$default$1()eu.cdevreeze.yaidom.core.EName in class eu.cdevreeze.tqa.extension.formula.model.MatchDimensionFilter has a different result type in current version, where it is scala.Option rather than eu.cdevreeze.yaidom.core.EName
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchDimensionFilter.copy$default$1")
* synthetic method copy$default$3()Boolean in class eu.cdevreeze.tqa.extension.formula.model.MatchDimensionFilter has a different result type in current version, where it is eu.cdevreeze.yaidom.core.EName rather than Boolean
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchDimensionFilter.copy$default$3")
* method this(eu.cdevreeze.yaidom.core.EName,eu.cdevreeze.yaidom.core.EName,Boolean)Unit in class eu.cdevreeze.tqa.extension.formula.model.MatchDimensionFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchDimensionFilter.this")
* the type hierarchy of object eu.cdevreeze.tqa.extension.formula.model.AspectCoverFilter is different in current version. Missing types {scala.runtime.AbstractFunction3}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.formula.model.AspectCoverFilter$")
* method apply(scala.collection.immutable.Set,scala.collection.immutable.IndexedSeq,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.extension.formula.model.AspectCoverFilter in object eu.cdevreeze.tqa.extension.formula.model.AspectCoverFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.AspectCoverFilter.apply")
* abstract method idOption()scala.Option in interface eu.cdevreeze.tqa.extension.formula.model.Resource is inherited by class DimensionFilter in current version.
  filter with: ProblemFilters.exclude[InheritedNewAbstractMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.Resource.idOption")
* the type hierarchy of object eu.cdevreeze.tqa.extension.formula.model.ForeverFilter is different in current version. Missing types {eu.cdevreeze.tqa.extension.formula.model.Filter,scala.Product,eu.cdevreeze.tqa.extension.formula.model.PeriodAspectFilter,scala.Equals}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.formula.model.ForeverFilter$")
* method productElement(Int)java.lang.Object in object eu.cdevreeze.tqa.extension.formula.model.ForeverFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.ForeverFilter.productElement")
* method productArity()Int in object eu.cdevreeze.tqa.extension.formula.model.ForeverFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.ForeverFilter.productArity")
* method canEqual(java.lang.Object)Boolean in object eu.cdevreeze.tqa.extension.formula.model.ForeverFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.ForeverFilter.canEqual")
* method productIterator()scala.collection.Iterator in object eu.cdevreeze.tqa.extension.formula.model.ForeverFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.ForeverFilter.productIterator")
* method productPrefix()java.lang.String in object eu.cdevreeze.tqa.extension.formula.model.ForeverFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.ForeverFilter.productPrefix")
* method toString()java.lang.String in object eu.cdevreeze.tqa.extension.formula.model.ForeverFilter is declared final in current version
  filter with: ProblemFilters.exclude[FinalMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.ForeverFilter.toString")
* method copy(eu.cdevreeze.tqa.ScopedXPathString)eu.cdevreeze.tqa.extension.formula.model.IdentifierFilter in class eu.cdevreeze.tqa.extension.formula.model.IdentifierFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.IdentifierFilter.copy")
* synthetic method copy$default$1()eu.cdevreeze.tqa.ScopedXPathString in class eu.cdevreeze.tqa.extension.formula.model.IdentifierFilter has a different result type in current version, where it is scala.Option rather than eu.cdevreeze.tqa.ScopedXPathString
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.IdentifierFilter.copy$default$1")
* method this(eu.cdevreeze.tqa.ScopedXPathString)Unit in class eu.cdevreeze.tqa.extension.formula.model.IdentifierFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.IdentifierFilter.this")
* synthetic method copy$default$2()java.lang.String in class eu.cdevreeze.tqa.extension.formula.model.InstantDurationFilter has a different result type in current version, where it is eu.cdevreeze.yaidom.core.EName rather than java.lang.String
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.InstantDurationFilter.copy$default$2")
* method copy(eu.cdevreeze.yaidom.core.EName,java.lang.String)eu.cdevreeze.tqa.extension.formula.model.InstantDurationFilter in class eu.cdevreeze.tqa.extension.formula.model.InstantDurationFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.InstantDurationFilter.copy")
* synthetic method copy$default$1()eu.cdevreeze.yaidom.core.EName in class eu.cdevreeze.tqa.extension.formula.model.InstantDurationFilter has a different result type in current version, where it is scala.Option rather than eu.cdevreeze.yaidom.core.EName
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.InstantDurationFilter.copy$default$1")
* method this(eu.cdevreeze.yaidom.core.EName,java.lang.String)Unit in class eu.cdevreeze.tqa.extension.formula.model.InstantDurationFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.InstantDurationFilter.this")
* the type hierarchy of object eu.cdevreeze.tqa.extension.formula.model.SpecificIdentifierFilter is different in current version. Missing types {scala.runtime.AbstractFunction2}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.formula.model.SpecificIdentifierFilter$")
* method apply(eu.cdevreeze.tqa.ScopedXPathString,eu.cdevreeze.tqa.ScopedXPathString)eu.cdevreeze.tqa.extension.formula.model.SpecificIdentifierFilter in object eu.cdevreeze.tqa.extension.formula.model.SpecificIdentifierFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.SpecificIdentifierFilter.apply")
* the type hierarchy of object eu.cdevreeze.tqa.extension.formula.model.ExplicitDimensionFilter is different in current version. Missing types {scala.runtime.AbstractFunction2}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.formula.model.ExplicitDimensionFilter$")
* method apply(eu.cdevreeze.tqa.ENameValueOrExpr,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.extension.formula.model.ExplicitDimensionFilter in object eu.cdevreeze.tqa.extension.formula.model.ExplicitDimensionFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.ExplicitDimensionFilter.apply")
* the type hierarchy of object eu.cdevreeze.tqa.extension.formula.model.ConceptCustomAttributeFilter is different in current version. Missing types {scala.runtime.AbstractFunction2}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.formula.model.ConceptCustomAttributeFilter$")
* method apply(eu.cdevreeze.tqa.ENameValueOrExpr,scala.Option)eu.cdevreeze.tqa.extension.formula.model.ConceptCustomAttributeFilter in object eu.cdevreeze.tqa.extension.formula.model.ConceptCustomAttributeFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.ConceptCustomAttributeFilter.apply")
* the type hierarchy of object eu.cdevreeze.tqa.extension.formula.model.PeriodFilter is different in current version. Missing types {scala.runtime.AbstractFunction1}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.formula.model.PeriodFilter$")
* method apply(eu.cdevreeze.tqa.ScopedXPathString)eu.cdevreeze.tqa.extension.formula.model.PeriodFilter in object eu.cdevreeze.tqa.extension.formula.model.PeriodFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.PeriodFilter.apply")
* synthetic method copy$default$2()eu.cdevreeze.tqa.ScopedXPathString in class eu.cdevreeze.tqa.extension.formula.model.GeneralVariable has a different result type in current version, where it is Boolean rather than eu.cdevreeze.tqa.ScopedXPathString
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.GeneralVariable.copy$default$2")
* method copy(Boolean,eu.cdevreeze.tqa.ScopedXPathString)eu.cdevreeze.tqa.extension.formula.model.GeneralVariable in class eu.cdevreeze.tqa.extension.formula.model.GeneralVariable does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.GeneralVariable.copy")
* synthetic method copy$default$1()Boolean in class eu.cdevreeze.tqa.extension.formula.model.GeneralVariable has a different result type in current version, where it is scala.Option rather than Boolean
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.GeneralVariable.copy$default$1")
* method this(Boolean,eu.cdevreeze.tqa.ScopedXPathString)Unit in class eu.cdevreeze.tqa.extension.formula.model.GeneralVariable does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.GeneralVariable.this")
* the type hierarchy of object eu.cdevreeze.tqa.extension.formula.model.SpecificSchemeFilter is different in current version. Missing types {scala.runtime.AbstractFunction1}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.formula.model.SpecificSchemeFilter$")
* method apply(eu.cdevreeze.tqa.ScopedXPathString)eu.cdevreeze.tqa.extension.formula.model.SpecificSchemeFilter in object eu.cdevreeze.tqa.extension.formula.model.SpecificSchemeFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.SpecificSchemeFilter.apply")
* the type hierarchy of object eu.cdevreeze.tqa.extension.formula.model.MatchSegmentFilter is different in current version. Missing types {scala.runtime.AbstractFunction2}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchSegmentFilter$")
* method apply(eu.cdevreeze.yaidom.core.EName,Boolean)eu.cdevreeze.tqa.extension.formula.model.MatchSegmentFilter in object eu.cdevreeze.tqa.extension.formula.model.MatchSegmentFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchSegmentFilter.apply")
* abstract method idOption()scala.Option in interface eu.cdevreeze.tqa.extension.formula.model.Resource is inherited by class ValueFilter in current version.
  filter with: ProblemFilters.exclude[InheritedNewAbstractMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.Resource.idOption")
* abstract method idOption()scala.Option in interface eu.cdevreeze.tqa.extension.formula.model.Resource is inherited by class ConceptFilter in current version.
  filter with: ProblemFilters.exclude[InheritedNewAbstractMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.Resource.idOption")
* synthetic method copy$default$2()eu.cdevreeze.tqa.aspect.AspectModel in class eu.cdevreeze.tqa.extension.formula.model.ExistenceAssertion has a different result type in current version, where it is Boolean rather than eu.cdevreeze.tqa.aspect.AspectModel
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.ExistenceAssertion.copy$default$2")
* method copy(Boolean,eu.cdevreeze.tqa.aspect.AspectModel,scala.Option,scala.collection.immutable.IndexedSeq,scala.collection.immutable.IndexedSeq,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.extension.formula.model.ExistenceAssertion in class eu.cdevreeze.tqa.extension.formula.model.ExistenceAssertion does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.ExistenceAssertion.copy")
* synthetic method copy$default$1()Boolean in class eu.cdevreeze.tqa.extension.formula.model.ExistenceAssertion has a different result type in current version, where it is scala.Option rather than Boolean
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.ExistenceAssertion.copy$default$1")
* synthetic method copy$default$4()scala.collection.immutable.IndexedSeq in class eu.cdevreeze.tqa.extension.formula.model.ExistenceAssertion has a different result type in current version, where it is scala.Option rather than scala.collection.immutable.IndexedSeq
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.ExistenceAssertion.copy$default$4")
* synthetic method copy$default$3()scala.Option in class eu.cdevreeze.tqa.extension.formula.model.ExistenceAssertion has a different result type in current version, where it is eu.cdevreeze.tqa.aspect.AspectModel rather than scala.Option
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.ExistenceAssertion.copy$default$3")
* method this(Boolean,eu.cdevreeze.tqa.aspect.AspectModel,scala.Option,scala.collection.immutable.IndexedSeq,scala.collection.immutable.IndexedSeq,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.extension.formula.model.ExistenceAssertion does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.ExistenceAssertion.this")
* the type hierarchy of object eu.cdevreeze.tqa.extension.formula.model.GeneralFilter is different in current version. Missing types {scala.runtime.AbstractFunction1}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.formula.model.GeneralFilter$")
* method apply(scala.Option)eu.cdevreeze.tqa.extension.formula.model.GeneralFilter in object eu.cdevreeze.tqa.extension.formula.model.GeneralFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.GeneralFilter.apply")
* synthetic method copy$default$2()eu.cdevreeze.tqa.aspect.AspectModel in class eu.cdevreeze.tqa.extension.formula.model.ValueAssertion has a different result type in current version, where it is Boolean rather than eu.cdevreeze.tqa.aspect.AspectModel
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.ValueAssertion.copy$default$2")
* method copy(Boolean,eu.cdevreeze.tqa.aspect.AspectModel,eu.cdevreeze.tqa.ScopedXPathString,scala.collection.immutable.IndexedSeq,scala.collection.immutable.IndexedSeq,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.extension.formula.model.ValueAssertion in class eu.cdevreeze.tqa.extension.formula.model.ValueAssertion does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.ValueAssertion.copy")
* synthetic method copy$default$1()Boolean in class eu.cdevreeze.tqa.extension.formula.model.ValueAssertion has a different result type in current version, where it is scala.Option rather than Boolean
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.ValueAssertion.copy$default$1")
* synthetic method copy$default$4()scala.collection.immutable.IndexedSeq in class eu.cdevreeze.tqa.extension.formula.model.ValueAssertion has a different result type in current version, where it is eu.cdevreeze.tqa.ScopedXPathString rather than scala.collection.immutable.IndexedSeq
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.ValueAssertion.copy$default$4")
* synthetic method copy$default$3()eu.cdevreeze.tqa.ScopedXPathString in class eu.cdevreeze.tqa.extension.formula.model.ValueAssertion has a different result type in current version, where it is eu.cdevreeze.tqa.aspect.AspectModel rather than eu.cdevreeze.tqa.ScopedXPathString
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.ValueAssertion.copy$default$3")
* method this(Boolean,eu.cdevreeze.tqa.aspect.AspectModel,eu.cdevreeze.tqa.ScopedXPathString,scala.collection.immutable.IndexedSeq,scala.collection.immutable.IndexedSeq,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.extension.formula.model.ValueAssertion does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.ValueAssertion.this")
* method copy(eu.cdevreeze.tqa.ENameValueOrExpr)eu.cdevreeze.tqa.extension.formula.model.AncestorFilter in class eu.cdevreeze.tqa.extension.formula.model.AncestorFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.AncestorFilter.copy")
* synthetic method copy$default$1()eu.cdevreeze.tqa.ENameValueOrExpr in class eu.cdevreeze.tqa.extension.formula.model.AncestorFilter has a different result type in current version, where it is scala.Option rather than eu.cdevreeze.tqa.ENameValueOrExpr
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.AncestorFilter.copy$default$1")
* method this(eu.cdevreeze.tqa.ENameValueOrExpr)Unit in class eu.cdevreeze.tqa.extension.formula.model.AncestorFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.AncestorFilter.this")
* synthetic method copy$default$2()Boolean in class eu.cdevreeze.tqa.extension.formula.model.MatchNonXDTSegmentFilter has a different result type in current version, where it is eu.cdevreeze.yaidom.core.EName rather than Boolean
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchNonXDTSegmentFilter.copy$default$2")
* method copy(eu.cdevreeze.yaidom.core.EName,Boolean)eu.cdevreeze.tqa.extension.formula.model.MatchNonXDTSegmentFilter in class eu.cdevreeze.tqa.extension.formula.model.MatchNonXDTSegmentFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchNonXDTSegmentFilter.copy")
* synthetic method copy$default$1()eu.cdevreeze.yaidom.core.EName in class eu.cdevreeze.tqa.extension.formula.model.MatchNonXDTSegmentFilter has a different result type in current version, where it is scala.Option rather than eu.cdevreeze.yaidom.core.EName
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchNonXDTSegmentFilter.copy$default$1")
* method this(eu.cdevreeze.yaidom.core.EName,Boolean)Unit in class eu.cdevreeze.tqa.extension.formula.model.MatchNonXDTSegmentFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchNonXDTSegmentFilter.this")
* synthetic method copy$default$2()scala.Option in class eu.cdevreeze.tqa.extension.formula.model.TypedDimensionFilter has a different result type in current version, where it is eu.cdevreeze.tqa.ENameValueOrExpr rather than scala.Option
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.TypedDimensionFilter.copy$default$2")
* method copy(eu.cdevreeze.tqa.ENameValueOrExpr,scala.Option)eu.cdevreeze.tqa.extension.formula.model.TypedDimensionFilter in class eu.cdevreeze.tqa.extension.formula.model.TypedDimensionFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.TypedDimensionFilter.copy")
* synthetic method copy$default$1()eu.cdevreeze.tqa.ENameValueOrExpr in class eu.cdevreeze.tqa.extension.formula.model.TypedDimensionFilter has a different result type in current version, where it is scala.Option rather than eu.cdevreeze.tqa.ENameValueOrExpr
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.TypedDimensionFilter.copy$default$1")
* method this(eu.cdevreeze.tqa.ENameValueOrExpr,scala.Option)Unit in class eu.cdevreeze.tqa.extension.formula.model.TypedDimensionFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.TypedDimensionFilter.this")
* the type hierarchy of object eu.cdevreeze.tqa.extension.formula.model.MatchUnitFilter is different in current version. Missing types {scala.runtime.AbstractFunction2}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchUnitFilter$")
* method apply(eu.cdevreeze.yaidom.core.EName,Boolean)eu.cdevreeze.tqa.extension.formula.model.MatchUnitFilter in object eu.cdevreeze.tqa.extension.formula.model.MatchUnitFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchUnitFilter.apply")
* the type hierarchy of object eu.cdevreeze.tqa.extension.formula.model.ValueAssertion is different in current version. Missing types {scala.runtime.AbstractFunction6}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.formula.model.ValueAssertion$")
* method apply(Boolean,eu.cdevreeze.tqa.aspect.AspectModel,eu.cdevreeze.tqa.ScopedXPathString,scala.collection.immutable.IndexedSeq,scala.collection.immutable.IndexedSeq,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.extension.formula.model.ValueAssertion in object eu.cdevreeze.tqa.extension.formula.model.ValueAssertion does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.ValueAssertion.apply")
* method copy(eu.cdevreeze.tqa.ScopedXPathString,eu.cdevreeze.tqa.ScopedXPathString)eu.cdevreeze.tqa.extension.formula.model.SpecificIdentifierFilter in class eu.cdevreeze.tqa.extension.formula.model.SpecificIdentifierFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.SpecificIdentifierFilter.copy")
* synthetic method copy$default$1()eu.cdevreeze.tqa.ScopedXPathString in class eu.cdevreeze.tqa.extension.formula.model.SpecificIdentifierFilter has a different result type in current version, where it is scala.Option rather than eu.cdevreeze.tqa.ScopedXPathString
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.SpecificIdentifierFilter.copy$default$1")
* method this(eu.cdevreeze.tqa.ScopedXPathString,eu.cdevreeze.tqa.ScopedXPathString)Unit in class eu.cdevreeze.tqa.extension.formula.model.SpecificIdentifierFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.SpecificIdentifierFilter.this")
* the type hierarchy of object eu.cdevreeze.tqa.extension.formula.model.FactVariable is different in current version. Missing types {scala.runtime.AbstractFunction5}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.formula.model.FactVariable$")
* method apply(Boolean,scala.Option,scala.Option,scala.Option,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.extension.formula.model.FactVariable in object eu.cdevreeze.tqa.extension.formula.model.FactVariable does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.FactVariable.apply")
* method copy(eu.cdevreeze.tqa.ScopedXPathString)eu.cdevreeze.tqa.extension.formula.model.GeneralMeasuresFilter in class eu.cdevreeze.tqa.extension.formula.model.GeneralMeasuresFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.GeneralMeasuresFilter.copy")
* synthetic method copy$default$1()eu.cdevreeze.tqa.ScopedXPathString in class eu.cdevreeze.tqa.extension.formula.model.GeneralMeasuresFilter has a different result type in current version, where it is scala.Option rather than eu.cdevreeze.tqa.ScopedXPathString
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.GeneralMeasuresFilter.copy$default$1")
* method this(eu.cdevreeze.tqa.ScopedXPathString)Unit in class eu.cdevreeze.tqa.extension.formula.model.GeneralMeasuresFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.GeneralMeasuresFilter.this")
* the type hierarchy of object eu.cdevreeze.tqa.extension.formula.model.SiblingFilter is different in current version. Missing types {scala.runtime.AbstractFunction1}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.formula.model.SiblingFilter$")
* method apply(eu.cdevreeze.yaidom.core.EName)eu.cdevreeze.tqa.extension.formula.model.SiblingFilter in object eu.cdevreeze.tqa.extension.formula.model.SiblingFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.SiblingFilter.apply")
* method copy(scala.Option)eu.cdevreeze.tqa.extension.formula.model.GeneralFilter in class eu.cdevreeze.tqa.extension.formula.model.GeneralFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.GeneralFilter.copy")
* method this(scala.Option)Unit in class eu.cdevreeze.tqa.extension.formula.model.GeneralFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.GeneralFilter.this")
* synthetic method copy$default$2()Boolean in class eu.cdevreeze.tqa.extension.formula.model.ConceptSubstitutionGroupFilter has a different result type in current version, where it is eu.cdevreeze.tqa.ENameValueOrExpr rather than Boolean
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.ConceptSubstitutionGroupFilter.copy$default$2")
* method copy(eu.cdevreeze.tqa.ENameValueOrExpr,Boolean)eu.cdevreeze.tqa.extension.formula.model.ConceptSubstitutionGroupFilter in class eu.cdevreeze.tqa.extension.formula.model.ConceptSubstitutionGroupFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.ConceptSubstitutionGroupFilter.copy")
* synthetic method copy$default$1()eu.cdevreeze.tqa.ENameValueOrExpr in class eu.cdevreeze.tqa.extension.formula.model.ConceptSubstitutionGroupFilter has a different result type in current version, where it is scala.Option rather than eu.cdevreeze.tqa.ENameValueOrExpr
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.ConceptSubstitutionGroupFilter.copy$default$1")
* method this(eu.cdevreeze.tqa.ENameValueOrExpr,Boolean)Unit in class eu.cdevreeze.tqa.extension.formula.model.ConceptSubstitutionGroupFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.ConceptSubstitutionGroupFilter.this")
* the type hierarchy of object eu.cdevreeze.tqa.extension.formula.model.InstantDurationFilter is different in current version. Missing types {scala.runtime.AbstractFunction2}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.formula.model.InstantDurationFilter$")
* method apply(eu.cdevreeze.yaidom.core.EName,java.lang.String)eu.cdevreeze.tqa.extension.formula.model.InstantDurationFilter in object eu.cdevreeze.tqa.extension.formula.model.InstantDurationFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.InstantDurationFilter.apply")
* the type hierarchy of object eu.cdevreeze.tqa.extension.formula.model.ConceptBalanceFilter is different in current version. Missing types {scala.runtime.AbstractFunction1}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.formula.model.ConceptBalanceFilter$")
* method apply(java.lang.String)eu.cdevreeze.tqa.extension.formula.model.ConceptBalanceFilter in object eu.cdevreeze.tqa.extension.formula.model.ConceptBalanceFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.ConceptBalanceFilter.apply")
* method copy(scala.Option)eu.cdevreeze.tqa.extension.formula.model.ScenarioFilter in class eu.cdevreeze.tqa.extension.formula.model.ScenarioFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.ScenarioFilter.copy")
* method this(scala.Option)Unit in class eu.cdevreeze.tqa.extension.formula.model.ScenarioFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.ScenarioFilter.this")
* synthetic method copy$default$2()Boolean in class eu.cdevreeze.tqa.extension.formula.model.MatchPeriodFilter has a different result type in current version, where it is eu.cdevreeze.yaidom.core.EName rather than Boolean
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchPeriodFilter.copy$default$2")
* method copy(eu.cdevreeze.yaidom.core.EName,Boolean)eu.cdevreeze.tqa.extension.formula.model.MatchPeriodFilter in class eu.cdevreeze.tqa.extension.formula.model.MatchPeriodFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchPeriodFilter.copy")
* synthetic method copy$default$1()eu.cdevreeze.yaidom.core.EName in class eu.cdevreeze.tqa.extension.formula.model.MatchPeriodFilter has a different result type in current version, where it is scala.Option rather than eu.cdevreeze.yaidom.core.EName
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchPeriodFilter.copy$default$1")
* method this(eu.cdevreeze.yaidom.core.EName,Boolean)Unit in class eu.cdevreeze.tqa.extension.formula.model.MatchPeriodFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchPeriodFilter.this")
* the type hierarchy of object eu.cdevreeze.tqa.extension.formula.model.IdentifierFilter is different in current version. Missing types {scala.runtime.AbstractFunction1}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.formula.model.IdentifierFilter$")
* method apply(eu.cdevreeze.tqa.ScopedXPathString)eu.cdevreeze.tqa.extension.formula.model.IdentifierFilter in object eu.cdevreeze.tqa.extension.formula.model.IdentifierFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.IdentifierFilter.apply")
* the type hierarchy of object eu.cdevreeze.tqa.extension.formula.model.ConceptPeriodTypeFilter is different in current version. Missing types {scala.runtime.AbstractFunction1}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.formula.model.ConceptPeriodTypeFilter$")
* method apply(java.lang.String)eu.cdevreeze.tqa.extension.formula.model.ConceptPeriodTypeFilter in object eu.cdevreeze.tqa.extension.formula.model.ConceptPeriodTypeFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.ConceptPeriodTypeFilter.apply")
* method copy(eu.cdevreeze.tqa.ScopedXPathString)eu.cdevreeze.tqa.extension.formula.model.SpecificSchemeFilter in class eu.cdevreeze.tqa.extension.formula.model.SpecificSchemeFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.SpecificSchemeFilter.copy")
* synthetic method copy$default$1()eu.cdevreeze.tqa.ScopedXPathString in class eu.cdevreeze.tqa.extension.formula.model.SpecificSchemeFilter has a different result type in current version, where it is scala.Option rather than eu.cdevreeze.tqa.ScopedXPathString
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.SpecificSchemeFilter.copy$default$1")
* method this(eu.cdevreeze.tqa.ScopedXPathString)Unit in class eu.cdevreeze.tqa.extension.formula.model.SpecificSchemeFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.SpecificSchemeFilter.this")
* method copy(java.lang.String)eu.cdevreeze.tqa.extension.formula.model.ConceptBalanceFilter in class eu.cdevreeze.tqa.extension.formula.model.ConceptBalanceFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.ConceptBalanceFilter.copy")
* synthetic method copy$default$1()java.lang.String in class eu.cdevreeze.tqa.extension.formula.model.ConceptBalanceFilter has a different result type in current version, where it is scala.Option rather than java.lang.String
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.ConceptBalanceFilter.copy$default$1")
* method this(java.lang.String)Unit in class eu.cdevreeze.tqa.extension.formula.model.ConceptBalanceFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.ConceptBalanceFilter.this")
* the type hierarchy of object eu.cdevreeze.tqa.extension.formula.model.ScenarioFilter is different in current version. Missing types {scala.runtime.AbstractFunction1}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.formula.model.ScenarioFilter$")
* method apply(scala.Option)eu.cdevreeze.tqa.extension.formula.model.ScenarioFilter in object eu.cdevreeze.tqa.extension.formula.model.ScenarioFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.ScenarioFilter.apply")
* the type hierarchy of object eu.cdevreeze.tqa.extension.formula.model.MatchNonXDTScenarioFilter is different in current version. Missing types {scala.runtime.AbstractFunction2}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchNonXDTScenarioFilter$")
* method apply(eu.cdevreeze.yaidom.core.EName,Boolean)eu.cdevreeze.tqa.extension.formula.model.MatchNonXDTScenarioFilter in object eu.cdevreeze.tqa.extension.formula.model.MatchNonXDTScenarioFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchNonXDTScenarioFilter.apply")
* the type hierarchy of object eu.cdevreeze.tqa.extension.formula.model.GeneralVariable is different in current version. Missing types {scala.runtime.AbstractFunction2}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.formula.model.GeneralVariable$")
* method apply(Boolean,eu.cdevreeze.tqa.ScopedXPathString)eu.cdevreeze.tqa.extension.formula.model.GeneralVariable in object eu.cdevreeze.tqa.extension.formula.model.GeneralVariable does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.GeneralVariable.apply")
* the type hierarchy of object eu.cdevreeze.tqa.extension.formula.model.MatchConceptFilter is different in current version. Missing types {scala.runtime.AbstractFunction2}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchConceptFilter$")
* method apply(eu.cdevreeze.yaidom.core.EName,Boolean)eu.cdevreeze.tqa.extension.formula.model.MatchConceptFilter in object eu.cdevreeze.tqa.extension.formula.model.MatchConceptFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchConceptFilter.apply")
* abstract method idOption()scala.Option in interface eu.cdevreeze.tqa.extension.formula.model.Resource is inherited by class SegmentScenarioFilter in current version.
  filter with: ProblemFilters.exclude[InheritedNewAbstractMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.Resource.idOption")
* abstract method idOption()scala.Option in interface eu.cdevreeze.tqa.extension.formula.model.Resource is inherited by class EntityFilter in current version.
  filter with: ProblemFilters.exclude[InheritedNewAbstractMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.Resource.idOption")
* method copy(java.lang.String)eu.cdevreeze.tqa.extension.formula.model.RegexpSchemeFilter in class eu.cdevreeze.tqa.extension.formula.model.RegexpSchemeFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.RegexpSchemeFilter.copy")
* synthetic method copy$default$1()java.lang.String in class eu.cdevreeze.tqa.extension.formula.model.RegexpSchemeFilter has a different result type in current version, where it is scala.Option rather than java.lang.String
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.RegexpSchemeFilter.copy$default$1")
* method this(java.lang.String)Unit in class eu.cdevreeze.tqa.extension.formula.model.RegexpSchemeFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.RegexpSchemeFilter.this")
* abstract method idOption()scala.Option in interface eu.cdevreeze.tqa.extension.formula.model.Resource is inherited by class TupleFilter in current version.
  filter with: ProblemFilters.exclude[InheritedNewAbstractMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.Resource.idOption")
* method copy(scala.Option)eu.cdevreeze.tqa.extension.formula.model.SegmentFilter in class eu.cdevreeze.tqa.extension.formula.model.SegmentFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.SegmentFilter.copy")
* method this(scala.Option)Unit in class eu.cdevreeze.tqa.extension.formula.model.SegmentFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.SegmentFilter.this")
* the type hierarchy of object eu.cdevreeze.tqa.extension.formula.model.RegexpIdentifierFilter is different in current version. Missing types {scala.runtime.AbstractFunction1}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.formula.model.RegexpIdentifierFilter$")
* method apply(java.lang.String)eu.cdevreeze.tqa.extension.formula.model.RegexpIdentifierFilter in object eu.cdevreeze.tqa.extension.formula.model.RegexpIdentifierFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.RegexpIdentifierFilter.apply")
* synthetic method copy$default$2()Boolean in class eu.cdevreeze.tqa.extension.formula.model.MatchScenarioFilter has a different result type in current version, where it is eu.cdevreeze.yaidom.core.EName rather than Boolean
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchScenarioFilter.copy$default$2")
* method copy(eu.cdevreeze.yaidom.core.EName,Boolean)eu.cdevreeze.tqa.extension.formula.model.MatchScenarioFilter in class eu.cdevreeze.tqa.extension.formula.model.MatchScenarioFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchScenarioFilter.copy")
* synthetic method copy$default$1()eu.cdevreeze.yaidom.core.EName in class eu.cdevreeze.tqa.extension.formula.model.MatchScenarioFilter has a different result type in current version, where it is scala.Option rather than eu.cdevreeze.yaidom.core.EName
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchScenarioFilter.copy$default$1")
* method this(eu.cdevreeze.yaidom.core.EName,Boolean)Unit in class eu.cdevreeze.tqa.extension.formula.model.MatchScenarioFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchScenarioFilter.this")
* the type hierarchy of object eu.cdevreeze.tqa.extension.formula.model.PeriodEndFilter is different in current version. Missing types {scala.runtime.AbstractFunction2}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.formula.model.PeriodEndFilter$")
* method apply(eu.cdevreeze.tqa.ScopedXPathString,scala.Option)eu.cdevreeze.tqa.extension.formula.model.PeriodEndFilter in object eu.cdevreeze.tqa.extension.formula.model.PeriodEndFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.PeriodEndFilter.apply")
* method copy(eu.cdevreeze.yaidom.core.EName)eu.cdevreeze.tqa.extension.formula.model.SiblingFilter in class eu.cdevreeze.tqa.extension.formula.model.SiblingFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.SiblingFilter.copy")
* synthetic method copy$default$1()eu.cdevreeze.yaidom.core.EName in class eu.cdevreeze.tqa.extension.formula.model.SiblingFilter has a different result type in current version, where it is scala.Option rather than eu.cdevreeze.yaidom.core.EName
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.SiblingFilter.copy$default$1")
* method this(eu.cdevreeze.yaidom.core.EName)Unit in class eu.cdevreeze.tqa.extension.formula.model.SiblingFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.SiblingFilter.this")
* synthetic method copy$default$2()scala.Option in class eu.cdevreeze.tqa.extension.formula.model.PeriodStartFilter has a different result type in current version, where it is eu.cdevreeze.tqa.ScopedXPathString rather than scala.Option
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.PeriodStartFilter.copy$default$2")
* method copy(eu.cdevreeze.tqa.ScopedXPathString,scala.Option)eu.cdevreeze.tqa.extension.formula.model.PeriodStartFilter in class eu.cdevreeze.tqa.extension.formula.model.PeriodStartFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.PeriodStartFilter.copy")
* synthetic method copy$default$1()eu.cdevreeze.tqa.ScopedXPathString in class eu.cdevreeze.tqa.extension.formula.model.PeriodStartFilter has a different result type in current version, where it is scala.Option rather than eu.cdevreeze.tqa.ScopedXPathString
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.PeriodStartFilter.copy$default$1")
* method this(eu.cdevreeze.tqa.ScopedXPathString,scala.Option)Unit in class eu.cdevreeze.tqa.extension.formula.model.PeriodStartFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.PeriodStartFilter.this")
* method copy(scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.extension.formula.model.AndFilter in class eu.cdevreeze.tqa.extension.formula.model.AndFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.AndFilter.copy")
* synthetic method copy$default$1()scala.collection.immutable.IndexedSeq in class eu.cdevreeze.tqa.extension.formula.model.AndFilter has a different result type in current version, where it is scala.Option rather than scala.collection.immutable.IndexedSeq
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.AndFilter.copy$default$1")
* method this(scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.extension.formula.model.AndFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.AndFilter.this")
* the type hierarchy of object eu.cdevreeze.tqa.extension.formula.model.SingleMeasureFilter is different in current version. Missing types {scala.runtime.AbstractFunction1}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.formula.model.SingleMeasureFilter$")
* method apply(eu.cdevreeze.tqa.ENameValueOrExpr)eu.cdevreeze.tqa.extension.formula.model.SingleMeasureFilter in object eu.cdevreeze.tqa.extension.formula.model.SingleMeasureFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.SingleMeasureFilter.apply")
* method copy(scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.extension.formula.model.ConceptNameFilter in class eu.cdevreeze.tqa.extension.formula.model.ConceptNameFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.ConceptNameFilter.copy")
* synthetic method copy$default$1()scala.collection.immutable.IndexedSeq in class eu.cdevreeze.tqa.extension.formula.model.ConceptNameFilter has a different result type in current version, where it is scala.Option rather than scala.collection.immutable.IndexedSeq
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.ConceptNameFilter.copy$default$1")
* method this(scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.extension.formula.model.ConceptNameFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.ConceptNameFilter.this")
* the type hierarchy of object eu.cdevreeze.tqa.extension.formula.model.GeneralMeasuresFilter is different in current version. Missing types {scala.runtime.AbstractFunction1}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.formula.model.GeneralMeasuresFilter$")
* method apply(eu.cdevreeze.tqa.ScopedXPathString)eu.cdevreeze.tqa.extension.formula.model.GeneralMeasuresFilter in object eu.cdevreeze.tqa.extension.formula.model.GeneralMeasuresFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.GeneralMeasuresFilter.apply")
* abstract method idOption()scala.Option in interface eu.cdevreeze.tqa.extension.formula.model.Resource is inherited by class VariableSet in current version.
  filter with: ProblemFilters.exclude[InheritedNewAbstractMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.Resource.idOption")
* the type hierarchy of object eu.cdevreeze.tqa.extension.formula.model.ConceptRelationFilter is different in current version. Missing types {scala.runtime.AbstractFunction8}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.formula.model.ConceptRelationFilter$")
* method apply(eu.cdevreeze.tqa.ENameValueOrExpr,eu.cdevreeze.tqa.StringValueOrExpr,scala.Option,eu.cdevreeze.tqa.StringValueOrExpr,scala.Option,eu.cdevreeze.tqa.extension.formula.common.ConceptRelationFilters#Axis,scala.Option,scala.Option)eu.cdevreeze.tqa.extension.formula.model.ConceptRelationFilter in object eu.cdevreeze.tqa.extension.formula.model.ConceptRelationFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.ConceptRelationFilter.apply")
* method copy(eu.cdevreeze.tqa.ScopedXPathString)eu.cdevreeze.tqa.extension.formula.model.PrecisionFilter in class eu.cdevreeze.tqa.extension.formula.model.PrecisionFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.PrecisionFilter.copy")
* synthetic method copy$default$1()eu.cdevreeze.tqa.ScopedXPathString in class eu.cdevreeze.tqa.extension.formula.model.PrecisionFilter has a different result type in current version, where it is scala.Option rather than eu.cdevreeze.tqa.ScopedXPathString
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.PrecisionFilter.copy$default$1")
* method this(eu.cdevreeze.tqa.ScopedXPathString)Unit in class eu.cdevreeze.tqa.extension.formula.model.PrecisionFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.PrecisionFilter.this")
* abstract method idOption()scala.Option in interface eu.cdevreeze.tqa.extension.formula.model.Resource is inherited by class VariableSetAssertion in current version.
  filter with: ProblemFilters.exclude[InheritedNewAbstractMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.Resource.idOption")
* the type hierarchy of object eu.cdevreeze.tqa.extension.formula.model.RelativeFilter is different in current version. Missing types {scala.runtime.AbstractFunction1}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.formula.model.RelativeFilter$")
* method apply(eu.cdevreeze.yaidom.core.EName)eu.cdevreeze.tqa.extension.formula.model.RelativeFilter in object eu.cdevreeze.tqa.extension.formula.model.RelativeFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.RelativeFilter.apply")
* the type hierarchy of object eu.cdevreeze.tqa.extension.formula.model.MatchNonXDTSegmentFilter is different in current version. Missing types {scala.runtime.AbstractFunction2}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchNonXDTSegmentFilter$")
* method apply(eu.cdevreeze.yaidom.core.EName,Boolean)eu.cdevreeze.tqa.extension.formula.model.MatchNonXDTSegmentFilter in object eu.cdevreeze.tqa.extension.formula.model.MatchNonXDTSegmentFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchNonXDTSegmentFilter.apply")
* the type hierarchy of object eu.cdevreeze.tqa.extension.formula.model.ConceptSubstitutionGroupFilter is different in current version. Missing types {scala.runtime.AbstractFunction2}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.formula.model.ConceptSubstitutionGroupFilter$")
* method apply(eu.cdevreeze.tqa.ENameValueOrExpr,Boolean)eu.cdevreeze.tqa.extension.formula.model.ConceptSubstitutionGroupFilter in object eu.cdevreeze.tqa.extension.formula.model.ConceptSubstitutionGroupFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.ConceptSubstitutionGroupFilter.apply")
* abstract method idOption()scala.Option in interface eu.cdevreeze.tqa.extension.formula.model.Resource is inherited by class Variable in current version.
  filter with: ProblemFilters.exclude[InheritedNewAbstractMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.Resource.idOption")
* the type hierarchy of object eu.cdevreeze.tqa.extension.formula.model.PrecisionFilter is different in current version. Missing types {scala.runtime.AbstractFunction1}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.formula.model.PrecisionFilter$")
* method apply(eu.cdevreeze.tqa.ScopedXPathString)eu.cdevreeze.tqa.extension.formula.model.PrecisionFilter in object eu.cdevreeze.tqa.extension.formula.model.PrecisionFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.PrecisionFilter.apply")
* synthetic method copy$default$2()Boolean in class eu.cdevreeze.tqa.extension.formula.model.ConceptDataTypeFilter has a different result type in current version, where it is eu.cdevreeze.tqa.ENameValueOrExpr rather than Boolean
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.ConceptDataTypeFilter.copy$default$2")
* method copy(eu.cdevreeze.tqa.ENameValueOrExpr,Boolean)eu.cdevreeze.tqa.extension.formula.model.ConceptDataTypeFilter in class eu.cdevreeze.tqa.extension.formula.model.ConceptDataTypeFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.ConceptDataTypeFilter.copy")
* synthetic method copy$default$1()eu.cdevreeze.tqa.ENameValueOrExpr in class eu.cdevreeze.tqa.extension.formula.model.ConceptDataTypeFilter has a different result type in current version, where it is scala.Option rather than eu.cdevreeze.tqa.ENameValueOrExpr
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.ConceptDataTypeFilter.copy$default$1")
* method this(eu.cdevreeze.tqa.ENameValueOrExpr,Boolean)Unit in class eu.cdevreeze.tqa.extension.formula.model.ConceptDataTypeFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.ConceptDataTypeFilter.this")
* synthetic method copy$default$6()eu.cdevreeze.tqa.extension.formula.common.ConceptRelationFilters#Axis in class eu.cdevreeze.tqa.extension.formula.model.ConceptRelationFilter has a different result type in current version, where it is scala.Option rather than eu.cdevreeze.tqa.extension.formula.common.ConceptRelationFilters#Axis
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.ConceptRelationFilter.copy$default$6")
* synthetic method copy$default$2()eu.cdevreeze.tqa.StringValueOrExpr in class eu.cdevreeze.tqa.extension.formula.model.ConceptRelationFilter has a different result type in current version, where it is eu.cdevreeze.tqa.ENameValueOrExpr rather than eu.cdevreeze.tqa.StringValueOrExpr
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.ConceptRelationFilter.copy$default$2")
* synthetic method copy$default$5()scala.Option in class eu.cdevreeze.tqa.extension.formula.model.ConceptRelationFilter has a different result type in current version, where it is eu.cdevreeze.tqa.StringValueOrExpr rather than scala.Option
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.ConceptRelationFilter.copy$default$5")
* method copy(eu.cdevreeze.tqa.ENameValueOrExpr,eu.cdevreeze.tqa.StringValueOrExpr,scala.Option,eu.cdevreeze.tqa.StringValueOrExpr,scala.Option,eu.cdevreeze.tqa.extension.formula.common.ConceptRelationFilters#Axis,scala.Option,scala.Option)eu.cdevreeze.tqa.extension.formula.model.ConceptRelationFilter in class eu.cdevreeze.tqa.extension.formula.model.ConceptRelationFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.ConceptRelationFilter.copy")
* synthetic method copy$default$7()scala.Option in class eu.cdevreeze.tqa.extension.formula.model.ConceptRelationFilter has a different result type in current version, where it is eu.cdevreeze.tqa.extension.formula.common.ConceptRelationFilters#Axis rather than scala.Option
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.ConceptRelationFilter.copy$default$7")
* synthetic method copy$default$1()eu.cdevreeze.tqa.ENameValueOrExpr in class eu.cdevreeze.tqa.extension.formula.model.ConceptRelationFilter has a different result type in current version, where it is scala.Option rather than eu.cdevreeze.tqa.ENameValueOrExpr
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.ConceptRelationFilter.copy$default$1")
* synthetic method copy$default$4()eu.cdevreeze.tqa.StringValueOrExpr in class eu.cdevreeze.tqa.extension.formula.model.ConceptRelationFilter has a different result type in current version, where it is scala.Option rather than eu.cdevreeze.tqa.StringValueOrExpr
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.ConceptRelationFilter.copy$default$4")
* synthetic method copy$default$3()scala.Option in class eu.cdevreeze.tqa.extension.formula.model.ConceptRelationFilter has a different result type in current version, where it is eu.cdevreeze.tqa.StringValueOrExpr rather than scala.Option
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.ConceptRelationFilter.copy$default$3")
* method this(eu.cdevreeze.tqa.ENameValueOrExpr,eu.cdevreeze.tqa.StringValueOrExpr,scala.Option,eu.cdevreeze.tqa.StringValueOrExpr,scala.Option,eu.cdevreeze.tqa.extension.formula.common.ConceptRelationFilters#Axis,scala.Option,scala.Option)Unit in class eu.cdevreeze.tqa.extension.formula.model.ConceptRelationFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.ConceptRelationFilter.this")
* the type hierarchy of object eu.cdevreeze.tqa.extension.formula.model.OrFilter is different in current version. Missing types {scala.runtime.AbstractFunction1}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.formula.model.OrFilter$")
* method apply(scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.extension.formula.model.OrFilter in object eu.cdevreeze.tqa.extension.formula.model.OrFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.OrFilter.apply")
* synthetic method copy$default$2()scala.Option in class eu.cdevreeze.tqa.extension.formula.model.FactVariable has a different result type in current version, where it is Boolean rather than scala.Option
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.FactVariable.copy$default$2")
* synthetic method copy$default$5()scala.collection.immutable.IndexedSeq in class eu.cdevreeze.tqa.extension.formula.model.FactVariable has a different result type in current version, where it is scala.Option rather than scala.collection.immutable.IndexedSeq
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.FactVariable.copy$default$5")
* method copy(Boolean,scala.Option,scala.Option,scala.Option,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.extension.formula.model.FactVariable in class eu.cdevreeze.tqa.extension.formula.model.FactVariable does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.FactVariable.copy")
* synthetic method copy$default$1()Boolean in class eu.cdevreeze.tqa.extension.formula.model.FactVariable has a different result type in current version, where it is scala.Option rather than Boolean
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.FactVariable.copy$default$1")
* method this(Boolean,scala.Option,scala.Option,scala.Option,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.extension.formula.model.FactVariable does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.FactVariable.this")
* synthetic method copy$default$2()scala.collection.immutable.IndexedSeq in class eu.cdevreeze.tqa.extension.formula.model.ExplicitDimensionFilter has a different result type in current version, where it is eu.cdevreeze.tqa.ENameValueOrExpr rather than scala.collection.immutable.IndexedSeq
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.ExplicitDimensionFilter.copy$default$2")
* method copy(eu.cdevreeze.tqa.ENameValueOrExpr,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.extension.formula.model.ExplicitDimensionFilter in class eu.cdevreeze.tqa.extension.formula.model.ExplicitDimensionFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.ExplicitDimensionFilter.copy")
* synthetic method copy$default$1()eu.cdevreeze.tqa.ENameValueOrExpr in class eu.cdevreeze.tqa.extension.formula.model.ExplicitDimensionFilter has a different result type in current version, where it is scala.Option rather than eu.cdevreeze.tqa.ENameValueOrExpr
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.ExplicitDimensionFilter.copy$default$1")
* method this(eu.cdevreeze.tqa.ENameValueOrExpr,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.extension.formula.model.ExplicitDimensionFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.ExplicitDimensionFilter.this")
* the type hierarchy of object eu.cdevreeze.tqa.extension.formula.model.RegexpSchemeFilter is different in current version. Missing types {scala.runtime.AbstractFunction1}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.formula.model.RegexpSchemeFilter$")
* method apply(java.lang.String)eu.cdevreeze.tqa.extension.formula.model.RegexpSchemeFilter in object eu.cdevreeze.tqa.extension.formula.model.RegexpSchemeFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.RegexpSchemeFilter.apply")
* synthetic method copy$default$2()Boolean in class eu.cdevreeze.tqa.extension.formula.model.MatchLocationFilter has a different result type in current version, where it is eu.cdevreeze.yaidom.core.EName rather than Boolean
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchLocationFilter.copy$default$2")
* method copy(eu.cdevreeze.yaidom.core.EName,Boolean)eu.cdevreeze.tqa.extension.formula.model.MatchLocationFilter in class eu.cdevreeze.tqa.extension.formula.model.MatchLocationFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchLocationFilter.copy")
* synthetic method copy$default$1()eu.cdevreeze.yaidom.core.EName in class eu.cdevreeze.tqa.extension.formula.model.MatchLocationFilter has a different result type in current version, where it is scala.Option rather than eu.cdevreeze.yaidom.core.EName
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchLocationFilter.copy$default$1")
* method this(eu.cdevreeze.yaidom.core.EName,Boolean)Unit in class eu.cdevreeze.tqa.extension.formula.model.MatchLocationFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchLocationFilter.this")
* method copy(java.lang.String)eu.cdevreeze.tqa.extension.formula.model.ConceptPeriodTypeFilter in class eu.cdevreeze.tqa.extension.formula.model.ConceptPeriodTypeFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.ConceptPeriodTypeFilter.copy")
* synthetic method copy$default$1()java.lang.String in class eu.cdevreeze.tqa.extension.formula.model.ConceptPeriodTypeFilter has a different result type in current version, where it is scala.Option rather than java.lang.String
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.ConceptPeriodTypeFilter.copy$default$1")
* method this(java.lang.String)Unit in class eu.cdevreeze.tqa.extension.formula.model.ConceptPeriodTypeFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.ConceptPeriodTypeFilter.this")
* the type hierarchy of object eu.cdevreeze.tqa.extension.formula.model.AncestorFilter is different in current version. Missing types {scala.runtime.AbstractFunction1}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.formula.model.AncestorFilter$")
* method apply(eu.cdevreeze.tqa.ENameValueOrExpr)eu.cdevreeze.tqa.extension.formula.model.AncestorFilter in object eu.cdevreeze.tqa.extension.formula.model.AncestorFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.AncestorFilter.apply")
* method copy(scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.extension.formula.model.OrFilter in class eu.cdevreeze.tqa.extension.formula.model.OrFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.OrFilter.copy")
* synthetic method copy$default$1()scala.collection.immutable.IndexedSeq in class eu.cdevreeze.tqa.extension.formula.model.OrFilter has a different result type in current version, where it is scala.Option rather than scala.collection.immutable.IndexedSeq
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.OrFilter.copy$default$1")
* method this(scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.extension.formula.model.OrFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.OrFilter.this")
* the type hierarchy of object eu.cdevreeze.tqa.extension.formula.model.MatchScenarioFilter is different in current version. Missing types {scala.runtime.AbstractFunction2}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchScenarioFilter$")
* method apply(eu.cdevreeze.yaidom.core.EName,Boolean)eu.cdevreeze.tqa.extension.formula.model.MatchScenarioFilter in object eu.cdevreeze.tqa.extension.formula.model.MatchScenarioFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchScenarioFilter.apply")
* synthetic method copy$default$2()Boolean in class eu.cdevreeze.tqa.extension.formula.model.MatchEntityIdentifierFilter has a different result type in current version, where it is eu.cdevreeze.yaidom.core.EName rather than Boolean
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchEntityIdentifierFilter.copy$default$2")
* method copy(eu.cdevreeze.yaidom.core.EName,Boolean)eu.cdevreeze.tqa.extension.formula.model.MatchEntityIdentifierFilter in class eu.cdevreeze.tqa.extension.formula.model.MatchEntityIdentifierFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchEntityIdentifierFilter.copy")
* synthetic method copy$default$1()eu.cdevreeze.yaidom.core.EName in class eu.cdevreeze.tqa.extension.formula.model.MatchEntityIdentifierFilter has a different result type in current version, where it is scala.Option rather than eu.cdevreeze.yaidom.core.EName
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchEntityIdentifierFilter.copy$default$1")
* method this(eu.cdevreeze.yaidom.core.EName,Boolean)Unit in class eu.cdevreeze.tqa.extension.formula.model.MatchEntityIdentifierFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchEntityIdentifierFilter.this")
* method copy(eu.cdevreeze.yaidom.core.EName)eu.cdevreeze.tqa.extension.formula.model.RelativeFilter in class eu.cdevreeze.tqa.extension.formula.model.RelativeFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.RelativeFilter.copy")
* synthetic method copy$default$1()eu.cdevreeze.yaidom.core.EName in class eu.cdevreeze.tqa.extension.formula.model.RelativeFilter has a different result type in current version, where it is scala.Option rather than eu.cdevreeze.yaidom.core.EName
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.RelativeFilter.copy$default$1")
* method this(eu.cdevreeze.yaidom.core.EName)Unit in class eu.cdevreeze.tqa.extension.formula.model.RelativeFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.RelativeFilter.this")
* synthetic method copy$default$2()scala.collection.immutable.IndexedSeq in class eu.cdevreeze.tqa.extension.formula.model.AspectCoverFilter has a different result type in current version, where it is scala.collection.immutable.Set rather than scala.collection.immutable.IndexedSeq
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.AspectCoverFilter.copy$default$2")
* method copy(scala.collection.immutable.Set,scala.collection.immutable.IndexedSeq,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.extension.formula.model.AspectCoverFilter in class eu.cdevreeze.tqa.extension.formula.model.AspectCoverFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.AspectCoverFilter.copy")
* synthetic method copy$default$1()scala.collection.immutable.Set in class eu.cdevreeze.tqa.extension.formula.model.AspectCoverFilter has a different result type in current version, where it is scala.Option rather than scala.collection.immutable.Set
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.AspectCoverFilter.copy$default$1")
* method this(scala.collection.immutable.Set,scala.collection.immutable.IndexedSeq,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.extension.formula.model.AspectCoverFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.AspectCoverFilter.this")
* synthetic method copy$default$2()scala.Option in class eu.cdevreeze.tqa.extension.formula.model.Parameter has a different result type in current version, where it is eu.cdevreeze.yaidom.core.EName rather than scala.Option
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.Parameter.copy$default$2")
* method copy(eu.cdevreeze.yaidom.core.EName,scala.Option,scala.Option,scala.Option)eu.cdevreeze.tqa.extension.formula.model.Parameter in class eu.cdevreeze.tqa.extension.formula.model.Parameter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.Parameter.copy")
* synthetic method copy$default$1()eu.cdevreeze.yaidom.core.EName in class eu.cdevreeze.tqa.extension.formula.model.Parameter has a different result type in current version, where it is scala.Option rather than eu.cdevreeze.yaidom.core.EName
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.Parameter.copy$default$1")
* method this(eu.cdevreeze.yaidom.core.EName,scala.Option,scala.Option,scala.Option)Unit in class eu.cdevreeze.tqa.extension.formula.model.Parameter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.Parameter.this")
* the type hierarchy of object eu.cdevreeze.tqa.extension.formula.model.MatchEntityIdentifierFilter is different in current version. Missing types {scala.runtime.AbstractFunction2}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchEntityIdentifierFilter$")
* method apply(eu.cdevreeze.yaidom.core.EName,Boolean)eu.cdevreeze.tqa.extension.formula.model.MatchEntityIdentifierFilter in object eu.cdevreeze.tqa.extension.formula.model.MatchEntityIdentifierFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchEntityIdentifierFilter.apply")
* method copy(java.lang.String)eu.cdevreeze.tqa.extension.formula.model.RegexpIdentifierFilter in class eu.cdevreeze.tqa.extension.formula.model.RegexpIdentifierFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.RegexpIdentifierFilter.copy")
* synthetic method copy$default$1()java.lang.String in class eu.cdevreeze.tqa.extension.formula.model.RegexpIdentifierFilter has a different result type in current version, where it is scala.Option rather than java.lang.String
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.RegexpIdentifierFilter.copy$default$1")
* method this(java.lang.String)Unit in class eu.cdevreeze.tqa.extension.formula.model.RegexpIdentifierFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.RegexpIdentifierFilter.this")
* the type hierarchy of object eu.cdevreeze.tqa.extension.formula.model.MatchDimensionFilter is different in current version. Missing types {scala.runtime.AbstractFunction3}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchDimensionFilter$")
* method apply(eu.cdevreeze.yaidom.core.EName,eu.cdevreeze.yaidom.core.EName,Boolean)eu.cdevreeze.tqa.extension.formula.model.MatchDimensionFilter in object eu.cdevreeze.tqa.extension.formula.model.MatchDimensionFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchDimensionFilter.apply")
* synthetic method copy$default$2()Boolean in class eu.cdevreeze.tqa.extension.formula.model.MatchNonXDTScenarioFilter has a different result type in current version, where it is eu.cdevreeze.yaidom.core.EName rather than Boolean
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchNonXDTScenarioFilter.copy$default$2")
* method copy(eu.cdevreeze.yaidom.core.EName,Boolean)eu.cdevreeze.tqa.extension.formula.model.MatchNonXDTScenarioFilter in class eu.cdevreeze.tqa.extension.formula.model.MatchNonXDTScenarioFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchNonXDTScenarioFilter.copy")
* synthetic method copy$default$1()eu.cdevreeze.yaidom.core.EName in class eu.cdevreeze.tqa.extension.formula.model.MatchNonXDTScenarioFilter has a different result type in current version, where it is scala.Option rather than eu.cdevreeze.yaidom.core.EName
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchNonXDTScenarioFilter.copy$default$1")
* method this(eu.cdevreeze.yaidom.core.EName,Boolean)Unit in class eu.cdevreeze.tqa.extension.formula.model.MatchNonXDTScenarioFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchNonXDTScenarioFilter.this")
* the type hierarchy of object eu.cdevreeze.tqa.extension.formula.model.MatchPeriodFilter is different in current version. Missing types {scala.runtime.AbstractFunction2}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchPeriodFilter$")
* method apply(eu.cdevreeze.yaidom.core.EName,Boolean)eu.cdevreeze.tqa.extension.formula.model.MatchPeriodFilter in object eu.cdevreeze.tqa.extension.formula.model.MatchPeriodFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.MatchPeriodFilter.apply")
* the type hierarchy of object eu.cdevreeze.tqa.extension.formula.model.ConceptNameFilter is different in current version. Missing types {scala.runtime.AbstractFunction1}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.formula.model.ConceptNameFilter$")
* method apply(scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.extension.formula.model.ConceptNameFilter in object eu.cdevreeze.tqa.extension.formula.model.ConceptNameFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.ConceptNameFilter.apply")
* method copy(eu.cdevreeze.tqa.ScopedXPathString)eu.cdevreeze.tqa.extension.formula.model.PeriodFilter in class eu.cdevreeze.tqa.extension.formula.model.PeriodFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.PeriodFilter.copy")
* synthetic method copy$default$1()eu.cdevreeze.tqa.ScopedXPathString in class eu.cdevreeze.tqa.extension.formula.model.PeriodFilter has a different result type in current version, where it is scala.Option rather than eu.cdevreeze.tqa.ScopedXPathString
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.PeriodFilter.copy$default$1")
* method this(eu.cdevreeze.tqa.ScopedXPathString)Unit in class eu.cdevreeze.tqa.extension.formula.model.PeriodFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.PeriodFilter.this")
* the type hierarchy of object eu.cdevreeze.tqa.extension.formula.model.PeriodInstantFilter is different in current version. Missing types {scala.runtime.AbstractFunction2}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.formula.model.PeriodInstantFilter$")
* method apply(eu.cdevreeze.tqa.ScopedXPathString,scala.Option)eu.cdevreeze.tqa.extension.formula.model.PeriodInstantFilter in object eu.cdevreeze.tqa.extension.formula.model.PeriodInstantFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.PeriodInstantFilter.apply")
* abstract method idOption()scala.Option in interface eu.cdevreeze.tqa.extension.formula.model.Resource is inherited by class BooleanFilter in current version.
  filter with: ProblemFilters.exclude[InheritedNewAbstractMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.Resource.idOption")
* the type hierarchy of object eu.cdevreeze.tqa.extension.formula.model.PeriodStartFilter is different in current version. Missing types {scala.runtime.AbstractFunction2}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.formula.model.PeriodStartFilter$")
* method apply(eu.cdevreeze.tqa.ScopedXPathString,scala.Option)eu.cdevreeze.tqa.extension.formula.model.PeriodStartFilter in object eu.cdevreeze.tqa.extension.formula.model.PeriodStartFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.PeriodStartFilter.apply")
* the type hierarchy of object eu.cdevreeze.tqa.extension.formula.model.AndFilter is different in current version. Missing types {scala.runtime.AbstractFunction1}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.formula.model.AndFilter$")
* method apply(scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.extension.formula.model.AndFilter in object eu.cdevreeze.tqa.extension.formula.model.AndFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.AndFilter.apply")
* the type hierarchy of object eu.cdevreeze.tqa.extension.formula.model.Formula is different in current version. Missing types {scala.runtime.AbstractFunction10}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.formula.model.Formula$")
* method apply(Boolean,eu.cdevreeze.tqa.aspect.AspectModel,scala.Option,eu.cdevreeze.tqa.ScopedXPathString,scala.collection.immutable.IndexedSeq,scala.Option,scala.Option,scala.collection.immutable.IndexedSeq,scala.collection.immutable.IndexedSeq,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.extension.formula.model.Formula in object eu.cdevreeze.tqa.extension.formula.model.Formula does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.Formula.apply")
* synthetic method copy$default$2()scala.Option in class eu.cdevreeze.tqa.extension.formula.model.ConceptCustomAttributeFilter has a different result type in current version, where it is eu.cdevreeze.tqa.ENameValueOrExpr rather than scala.Option
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.ConceptCustomAttributeFilter.copy$default$2")
* method copy(eu.cdevreeze.tqa.ENameValueOrExpr,scala.Option)eu.cdevreeze.tqa.extension.formula.model.ConceptCustomAttributeFilter in class eu.cdevreeze.tqa.extension.formula.model.ConceptCustomAttributeFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.ConceptCustomAttributeFilter.copy")
* synthetic method copy$default$1()eu.cdevreeze.tqa.ENameValueOrExpr in class eu.cdevreeze.tqa.extension.formula.model.ConceptCustomAttributeFilter has a different result type in current version, where it is scala.Option rather than eu.cdevreeze.tqa.ENameValueOrExpr
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.ConceptCustomAttributeFilter.copy$default$1")
* method this(eu.cdevreeze.tqa.ENameValueOrExpr,scala.Option)Unit in class eu.cdevreeze.tqa.extension.formula.model.ConceptCustomAttributeFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.ConceptCustomAttributeFilter.this")
* method copy(eu.cdevreeze.tqa.ScopedXPathString)eu.cdevreeze.tqa.extension.formula.model.Precondition in class eu.cdevreeze.tqa.extension.formula.model.Precondition does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.Precondition.copy")
* synthetic method copy$default$1()eu.cdevreeze.tqa.ScopedXPathString in class eu.cdevreeze.tqa.extension.formula.model.Precondition has a different result type in current version, where it is scala.Option rather than eu.cdevreeze.tqa.ScopedXPathString
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.Precondition.copy$default$1")
* method this(eu.cdevreeze.tqa.ScopedXPathString)Unit in class eu.cdevreeze.tqa.extension.formula.model.Precondition does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.Precondition.this")
* the type hierarchy of object eu.cdevreeze.tqa.extension.formula.model.NilFilter is different in current version. Missing types {eu.cdevreeze.tqa.extension.formula.model.Filter,scala.Product,eu.cdevreeze.tqa.extension.formula.model.ValueFilter,scala.Equals}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.formula.model.NilFilter$")
* method productElement(Int)java.lang.Object in object eu.cdevreeze.tqa.extension.formula.model.NilFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.NilFilter.productElement")
* method productArity()Int in object eu.cdevreeze.tqa.extension.formula.model.NilFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.NilFilter.productArity")
* method canEqual(java.lang.Object)Boolean in object eu.cdevreeze.tqa.extension.formula.model.NilFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.NilFilter.canEqual")
* method productIterator()scala.collection.Iterator in object eu.cdevreeze.tqa.extension.formula.model.NilFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.NilFilter.productIterator")
* method productPrefix()java.lang.String in object eu.cdevreeze.tqa.extension.formula.model.NilFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.NilFilter.productPrefix")
* method toString()java.lang.String in object eu.cdevreeze.tqa.extension.formula.model.NilFilter is declared final in current version
  filter with: ProblemFilters.exclude[FinalMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.NilFilter.toString")
* synthetic method copy$default$2()scala.Option in class eu.cdevreeze.tqa.extension.formula.model.PeriodInstantFilter has a different result type in current version, where it is eu.cdevreeze.tqa.ScopedXPathString rather than scala.Option
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.PeriodInstantFilter.copy$default$2")
* method copy(eu.cdevreeze.tqa.ScopedXPathString,scala.Option)eu.cdevreeze.tqa.extension.formula.model.PeriodInstantFilter in class eu.cdevreeze.tqa.extension.formula.model.PeriodInstantFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.PeriodInstantFilter.copy")
* synthetic method copy$default$1()eu.cdevreeze.tqa.ScopedXPathString in class eu.cdevreeze.tqa.extension.formula.model.PeriodInstantFilter has a different result type in current version, where it is scala.Option rather than eu.cdevreeze.tqa.ScopedXPathString
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.model.PeriodInstantFilter.copy$default$1")
* method this(eu.cdevreeze.tqa.ScopedXPathString,scala.Option)Unit in class eu.cdevreeze.tqa.extension.formula.model.PeriodInstantFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.PeriodInstantFilter.this")
* the type hierarchy of object eu.cdevreeze.tqa.extension.formula.model.TypedDimensionFilter is different in current version. Missing types {scala.runtime.AbstractFunction2}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.formula.model.TypedDimensionFilter$")
* method apply(eu.cdevreeze.tqa.ENameValueOrExpr,scala.Option)eu.cdevreeze.tqa.extension.formula.model.TypedDimensionFilter in object eu.cdevreeze.tqa.extension.formula.model.TypedDimensionFilter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.TypedDimensionFilter.apply")
* abstract method idOption()scala.Option in interface eu.cdevreeze.tqa.extension.formula.model.Resource is inherited by class Assertion in current version.
  filter with: ProblemFilters.exclude[InheritedNewAbstractMethodProblem]("eu.cdevreeze.tqa.extension.formula.model.Resource.idOption")
* synthetic method copy$default$6()eu.cdevreeze.tqa.StringValueOrExpr in class eu.cdevreeze.tqa.extension.table.model.ConceptRelationshipNode has a different result type in current version, where it is scala.Option rather than eu.cdevreeze.tqa.StringValueOrExpr
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.table.model.ConceptRelationshipNode.copy$default$6")
* synthetic method copy$default$10()scala.collection.immutable.IndexedSeq in class eu.cdevreeze.tqa.extension.table.model.ConceptRelationshipNode has a different result type in current version, where it is scala.Option rather than scala.collection.immutable.IndexedSeq
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.table.model.ConceptRelationshipNode.copy$default$10")
* method copy(scala.Option,scala.Option,scala.collection.immutable.IndexedSeq,scala.Option,scala.Option,eu.cdevreeze.tqa.StringValueOrExpr,scala.Option,scala.Option,scala.Option,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.extension.table.model.ConceptRelationshipNode in class eu.cdevreeze.tqa.extension.table.model.ConceptRelationshipNode does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.table.model.ConceptRelationshipNode.copy")
* synthetic method copy$default$7()scala.Option in class eu.cdevreeze.tqa.extension.table.model.ConceptRelationshipNode has a different result type in current version, where it is eu.cdevreeze.tqa.StringValueOrExpr rather than scala.Option
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.table.model.ConceptRelationshipNode.copy$default$7")
* synthetic method copy$default$4()scala.Option in class eu.cdevreeze.tqa.extension.table.model.ConceptRelationshipNode has a different result type in current version, where it is scala.collection.immutable.IndexedSeq rather than scala.Option
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.table.model.ConceptRelationshipNode.copy$default$4")
* synthetic method copy$default$3()scala.collection.immutable.IndexedSeq in class eu.cdevreeze.tqa.extension.table.model.ConceptRelationshipNode has a different result type in current version, where it is scala.Option rather than scala.collection.immutable.IndexedSeq
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.table.model.ConceptRelationshipNode.copy$default$3")
* method this(scala.Option,scala.Option,scala.collection.immutable.IndexedSeq,scala.Option,scala.Option,eu.cdevreeze.tqa.StringValueOrExpr,scala.Option,scala.Option,scala.Option,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.extension.table.model.ConceptRelationshipNode does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.table.model.ConceptRelationshipNode.this")
* synthetic method copy$default$2()scala.collection.immutable.IndexedSeq in class eu.cdevreeze.tqa.extension.table.model.Breakdown has a different result type in current version, where it is scala.Option rather than scala.collection.immutable.IndexedSeq
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.table.model.Breakdown.copy$default$2")
* method copy(scala.Option,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.extension.table.model.Breakdown in class eu.cdevreeze.tqa.extension.table.model.Breakdown does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.table.model.Breakdown.copy")
* method this(scala.Option,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.extension.table.model.Breakdown does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.table.model.Breakdown.this")
* abstract method idOption()scala.Option in interface eu.cdevreeze.tqa.extension.table.model.Resource is inherited by class RelationshipNode in current version.
  filter with: ProblemFilters.exclude[InheritedNewAbstractMethodProblem]("eu.cdevreeze.tqa.extension.table.model.Resource.idOption")
* the type hierarchy of object eu.cdevreeze.tqa.extension.table.model.DimensionRelationshipNode is different in current version. Missing types {scala.runtime.AbstractFunction8}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.table.model.DimensionRelationshipNode$")
* method apply(scala.Option,scala.Option,eu.cdevreeze.yaidom.core.EName,scala.collection.immutable.IndexedSeq,scala.Option,scala.Option,scala.Option,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.extension.table.model.DimensionRelationshipNode in object eu.cdevreeze.tqa.extension.table.model.DimensionRelationshipNode does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.table.model.DimensionRelationshipNode.apply")
* the type hierarchy of object eu.cdevreeze.tqa.extension.table.model.ConceptRelationshipNode is different in current version. Missing types {scala.runtime.AbstractFunction10}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.table.model.ConceptRelationshipNode$")
* method apply(scala.Option,scala.Option,scala.collection.immutable.IndexedSeq,scala.Option,scala.Option,eu.cdevreeze.tqa.StringValueOrExpr,scala.Option,scala.Option,scala.Option,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.extension.table.model.ConceptRelationshipNode in object eu.cdevreeze.tqa.extension.table.model.ConceptRelationshipNode does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.table.model.ConceptRelationshipNode.apply")
* synthetic method copy$default$5()scala.Option in class eu.cdevreeze.tqa.extension.table.model.DimensionRelationshipNode has a different result type in current version, where it is scala.collection.immutable.IndexedSeq rather than scala.Option
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.table.model.DimensionRelationshipNode.copy$default$5")
* synthetic method copy$default$8()scala.collection.immutable.IndexedSeq in class eu.cdevreeze.tqa.extension.table.model.DimensionRelationshipNode has a different result type in current version, where it is scala.Option rather than scala.collection.immutable.IndexedSeq
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.table.model.DimensionRelationshipNode.copy$default$8")
* method copy(scala.Option,scala.Option,eu.cdevreeze.yaidom.core.EName,scala.collection.immutable.IndexedSeq,scala.Option,scala.Option,scala.Option,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.extension.table.model.DimensionRelationshipNode in class eu.cdevreeze.tqa.extension.table.model.DimensionRelationshipNode does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.table.model.DimensionRelationshipNode.copy")
* synthetic method copy$default$4()scala.collection.immutable.IndexedSeq in class eu.cdevreeze.tqa.extension.table.model.DimensionRelationshipNode has a different result type in current version, where it is eu.cdevreeze.yaidom.core.EName rather than scala.collection.immutable.IndexedSeq
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.table.model.DimensionRelationshipNode.copy$default$4")
* synthetic method copy$default$3()eu.cdevreeze.yaidom.core.EName in class eu.cdevreeze.tqa.extension.table.model.DimensionRelationshipNode has a different result type in current version, where it is scala.Option rather than eu.cdevreeze.yaidom.core.EName
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.table.model.DimensionRelationshipNode.copy$default$3")
* method this(scala.Option,scala.Option,eu.cdevreeze.yaidom.core.EName,scala.collection.immutable.IndexedSeq,scala.Option,scala.Option,scala.Option,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.extension.table.model.DimensionRelationshipNode does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.table.model.DimensionRelationshipNode.this")
* the type hierarchy of object eu.cdevreeze.tqa.extension.table.model.Breakdown is different in current version. Missing types {scala.runtime.AbstractFunction2}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.table.model.Breakdown$")
* method apply(scala.Option,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.extension.table.model.Breakdown in object eu.cdevreeze.tqa.extension.table.model.Breakdown does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.table.model.Breakdown.apply")
* the type hierarchy of object eu.cdevreeze.tqa.extension.table.model.AspectNode is different in current version. Missing types {scala.runtime.AbstractFunction3}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.table.model.AspectNode$")
* method apply(eu.cdevreeze.tqa.extension.table.model.AspectSpec,scala.Option,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.extension.table.model.AspectNode in object eu.cdevreeze.tqa.extension.table.model.AspectNode does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.table.model.AspectNode.apply")
* synthetic method copy$default$2()scala.collection.immutable.IndexedSeq in class eu.cdevreeze.tqa.extension.table.model.Table has a different result type in current version, where it is eu.cdevreeze.tqa.extension.table.common.ParentChildOrder rather than scala.collection.immutable.IndexedSeq
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.table.model.Table.copy$default$2")
* method copy(eu.cdevreeze.tqa.extension.table.common.ParentChildOrder,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.extension.table.model.Table in class eu.cdevreeze.tqa.extension.table.model.Table does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.table.model.Table.copy")
* synthetic method copy$default$1()eu.cdevreeze.tqa.extension.table.common.ParentChildOrder in class eu.cdevreeze.tqa.extension.table.model.Table has a different result type in current version, where it is scala.Option rather than eu.cdevreeze.tqa.extension.table.common.ParentChildOrder
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.table.model.Table.copy$default$1")
* method this(eu.cdevreeze.tqa.extension.table.common.ParentChildOrder,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.extension.table.model.Table does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.table.model.Table.this")
* synthetic method copy$default$5()Boolean in class eu.cdevreeze.tqa.extension.table.model.RuleNode has a different result type in current version, where it is scala.collection.immutable.IndexedSeq rather than Boolean
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.table.model.RuleNode.copy$default$5")
* method copy(scala.Option,scala.Option,scala.collection.immutable.IndexedSeq,scala.collection.immutable.IndexedSeq,Boolean,Boolean,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.extension.table.model.RuleNode in class eu.cdevreeze.tqa.extension.table.model.RuleNode does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.table.model.RuleNode.copy")
* synthetic method copy$default$7()scala.collection.immutable.IndexedSeq in class eu.cdevreeze.tqa.extension.table.model.RuleNode has a different result type in current version, where it is Boolean rather than scala.collection.immutable.IndexedSeq
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.table.model.RuleNode.copy$default$7")
* synthetic method copy$default$3()scala.collection.immutable.IndexedSeq in class eu.cdevreeze.tqa.extension.table.model.RuleNode has a different result type in current version, where it is scala.Option rather than scala.collection.immutable.IndexedSeq
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.table.model.RuleNode.copy$default$3")
* method this(scala.Option,scala.Option,scala.collection.immutable.IndexedSeq,scala.collection.immutable.IndexedSeq,Boolean,Boolean,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.extension.table.model.RuleNode does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.table.model.RuleNode.this")
* abstract method idOption()scala.Option in interface eu.cdevreeze.tqa.extension.table.model.Resource is inherited by class OpenDefinitionNode in current version.
  filter with: ProblemFilters.exclude[InheritedNewAbstractMethodProblem]("eu.cdevreeze.tqa.extension.table.model.Resource.idOption")
* the type hierarchy of object eu.cdevreeze.tqa.extension.table.model.RuleNode is different in current version. Missing types {scala.runtime.AbstractFunction7}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.table.model.RuleNode$")
* method apply(scala.Option,scala.Option,scala.collection.immutable.IndexedSeq,scala.collection.immutable.IndexedSeq,Boolean,Boolean,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.extension.table.model.RuleNode in object eu.cdevreeze.tqa.extension.table.model.RuleNode does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.table.model.RuleNode.apply")
* abstract method idOption()scala.Option in interface eu.cdevreeze.tqa.extension.table.model.Resource is inherited by class ClosedDefinitionNode in current version.
  filter with: ProblemFilters.exclude[InheritedNewAbstractMethodProblem]("eu.cdevreeze.tqa.extension.table.model.Resource.idOption")
* the type hierarchy of object eu.cdevreeze.tqa.extension.table.model.Table is different in current version. Missing types {scala.runtime.AbstractFunction2}
  filter with: ProblemFilters.exclude[MissingTypesProblem]("eu.cdevreeze.tqa.extension.table.model.Table$")
* method apply(eu.cdevreeze.tqa.extension.table.common.ParentChildOrder,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.extension.table.model.Table in object eu.cdevreeze.tqa.extension.table.model.Table does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.table.model.Table.apply")
* synthetic method copy$default$2()scala.Option in class eu.cdevreeze.tqa.extension.table.model.AspectNode has a different result type in current version, where it is eu.cdevreeze.tqa.extension.table.model.AspectSpec rather than scala.Option
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.table.model.AspectNode.copy$default$2")
* method copy(eu.cdevreeze.tqa.extension.table.model.AspectSpec,scala.Option,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.extension.table.model.AspectNode in class eu.cdevreeze.tqa.extension.table.model.AspectNode does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.table.model.AspectNode.copy")
* synthetic method copy$default$1()eu.cdevreeze.tqa.extension.table.model.AspectSpec in class eu.cdevreeze.tqa.extension.table.model.AspectNode has a different result type in current version, where it is scala.Option rather than eu.cdevreeze.tqa.extension.table.model.AspectSpec
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.table.model.AspectNode.copy$default$1")
* synthetic method copy$default$3()scala.collection.immutable.IndexedSeq in class eu.cdevreeze.tqa.extension.table.model.AspectNode has a different result type in current version, where it is scala.Option rather than scala.collection.immutable.IndexedSeq
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.table.model.AspectNode.copy$default$3")
* method this(eu.cdevreeze.tqa.extension.table.model.AspectSpec,scala.Option,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.extension.table.model.AspectNode does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.extension.table.model.AspectNode.this")
* abstract method idOption()scala.Option in interface eu.cdevreeze.tqa.extension.table.model.Resource is inherited by class DefinitionNode in current version.
  filter with: ProblemFilters.exclude[InheritedNewAbstractMethodProblem]("eu.cdevreeze.tqa.extension.table.model.Resource.idOption")


0.8.3
=====

Compared with release 0.8.2, the main changes in this version are:

* DTS discovery no longer expects "taxonomy document" roots to be XML document roots
* The taxonomy package and layout model "yaidom dialects" now also contain non-element nodes, to make (direct) conversion to resolved elements work
* Periods in XBRL instances can now have timezones (for datetime period data)

Breaking changes compared to version 0.8.2 (in SBT, run: tqaJVM/*:mimaReportBinaryIssues):

* method parseStartDate(java.lang.String)java.time.LocalDateTime in object eu.cdevreeze.tqa.instance.Period has a different result type in current version, where it is java.time.temporal.Temporal rather than java.time.LocalDateTime
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.instance.Period.parseStartDate")
* method parseInstantOrEndDate(java.lang.String)java.time.LocalDateTime in object eu.cdevreeze.tqa.instance.Period has a different result type in current version, where it is java.time.temporal.Temporal rather than java.time.LocalDateTime
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.instance.Period.parseInstantOrEndDate")
* method instantDateTime()java.time.LocalDateTime in class eu.cdevreeze.tqa.instance.InstantPeriod has a different result type in current version, where it is java.time.temporal.Temporal rather than java.time.LocalDateTime
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.instance.InstantPeriod.instantDateTime")
* method dateTime()java.time.LocalDateTime in class eu.cdevreeze.tqa.instance.StartDate has a different result type in current version, where it is java.time.temporal.Temporal rather than java.time.LocalDateTime
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.instance.StartDate.dateTime")
* method endDateTime()java.time.LocalDateTime in class eu.cdevreeze.tqa.instance.StartEndDatePeriod has a different result type in current version, where it is java.time.temporal.Temporal rather than java.time.LocalDateTime
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.instance.StartEndDatePeriod.endDateTime")
* method startDateTime()java.time.LocalDateTime in class eu.cdevreeze.tqa.instance.StartEndDatePeriod has a different result type in current version, where it is java.time.temporal.Temporal rather than java.time.LocalDateTime
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.instance.StartEndDatePeriod.startDateTime")
* method dateTime()java.time.LocalDateTime in class eu.cdevreeze.tqa.instance.Instant has a different result type in current version, where it is java.time.temporal.Temporal rather than java.time.LocalDateTime
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.instance.Instant.dateTime")
* method dateTime()java.time.LocalDateTime in class eu.cdevreeze.tqa.instance.EndDate has a different result type in current version, where it is java.time.temporal.Temporal rather than java.time.LocalDateTime
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.instance.EndDate.dateTime")


0.8.2
=====

Compared with release 0.8.1, the main changes in this version are:

* Added a yaidom dialect for table layout models, with rather rich query support
* Enhanced the aspect support in the Aspect type, at the expense of AspectModel methods that have been removed
* "Hardened" the lenient creation APIs for the different yaidom dialects (for taxonomies, instances, taxonomy packages and layout models)
* These yaidom dialects now also support nesting the expected root elements in other elements

Breaking changes compared to version 0.8.1 (in SBT, run: tqaJVM/*:mimaReportBinaryIssues):

* abstract method wellKnownAspects()scala.collection.immutable.Set in interface eu.cdevreeze.tqa.aspect.AspectModel does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.aspect.AspectModel.wellKnownAspects")
* abstract method requiredNumericItemAspects()scala.collection.immutable.Set in interface eu.cdevreeze.tqa.aspect.AspectModel does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.aspect.AspectModel.requiredNumericItemAspects")
* abstract method requiredItemAspects()scala.collection.immutable.Set in interface eu.cdevreeze.tqa.aspect.AspectModel does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.aspect.AspectModel.requiredItemAspects")
* method wellKnownAspects()scala.collection.immutable.Set in object eu.cdevreeze.tqa.aspect.AspectModel#DimensionalAspectModel does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.aspect.AspectModel#DimensionalAspectModel.wellKnownAspects")
* method requiredNumericItemAspects()scala.collection.immutable.Set in object eu.cdevreeze.tqa.aspect.AspectModel#DimensionalAspectModel does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.aspect.AspectModel#DimensionalAspectModel.requiredNumericItemAspects")
* method requiredItemAspects()scala.collection.immutable.Set in object eu.cdevreeze.tqa.aspect.AspectModel#DimensionalAspectModel does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.aspect.AspectModel#DimensionalAspectModel.requiredItemAspects")
* abstract method isIncludedInDimensionalAspectModel()Boolean in interface eu.cdevreeze.tqa.aspect.Aspect is present only in current version
  filter with: ProblemFilters.exclude[ReversedMissingMethodProblem]("eu.cdevreeze.tqa.aspect.Aspect.isIncludedInDimensionalAspectModel")
* abstract method appliesToNonNumericItems()Boolean in interface eu.cdevreeze.tqa.aspect.Aspect is present only in current version
  filter with: ProblemFilters.exclude[ReversedMissingMethodProblem]("eu.cdevreeze.tqa.aspect.Aspect.appliesToNonNumericItems")
* abstract method isIncludedInNonDimensionalAspectModel()Boolean in interface eu.cdevreeze.tqa.aspect.Aspect is present only in current version
  filter with: ProblemFilters.exclude[ReversedMissingMethodProblem]("eu.cdevreeze.tqa.aspect.Aspect.isIncludedInNonDimensionalAspectModel")
* abstract method appliesToTuples()Boolean in interface eu.cdevreeze.tqa.aspect.Aspect is present only in current version
  filter with: ProblemFilters.exclude[ReversedMissingMethodProblem]("eu.cdevreeze.tqa.aspect.Aspect.appliesToTuples")
* method wellKnownAspects()scala.collection.immutable.Set in object eu.cdevreeze.tqa.aspect.AspectModel#NonDimensionalAspectModel does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.aspect.AspectModel#NonDimensionalAspectModel.wellKnownAspects")
* method requiredNumericItemAspects()scala.collection.immutable.Set in object eu.cdevreeze.tqa.aspect.AspectModel#NonDimensionalAspectModel does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.aspect.AspectModel#NonDimensionalAspectModel.requiredNumericItemAspects")
* method requiredItemAspects()scala.collection.immutable.Set in object eu.cdevreeze.tqa.aspect.AspectModel#NonDimensionalAspectModel does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.aspect.AspectModel#NonDimensionalAspectModel.requiredItemAspects")
* method apply(eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem)eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyPackage in object eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyPackage does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyPackage.apply")
* method apply(eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem)eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyPackageElem in object eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyPackageElem does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyPackageElem.apply")
* class eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyPackageElem was concrete; is declared abstract in current version
  filter with: ProblemFilters.exclude[AbstractClassProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyPackageElem")
* method isFactPath(eu.cdevreeze.yaidom.core.Path)Boolean in object eu.cdevreeze.tqa.instance.Fact does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.instance.Fact.isFactPath")
* class eu.cdevreeze.tqa.instance.XbrliElem was concrete; is declared abstract in current version
  filter with: ProblemFilters.exclude[AbstractClassProblem]("eu.cdevreeze.tqa.instance.XbrliElem")


0.8.1
=====

Compared with release 0.8.0, the main changes in this version are:

* Completely reworked (and tested) ``ConceptRelationshipNodeData`` and ``DimensionRelationshipNodeData``
* Added some methods to ``DimensionalRelationshipContainerApi``
* Bug fix: it is no longer required that a ``TaxonomyDocument`` holds a schema or linkbase

There are many breaking changes (only) in the "xpathaware" namespace, but this part of TQA has rarely been used so far. The most important change
in this respect is that XPath evaluation no longer needs an implicit Scope.

Breaking changes compared to version 0.8.0 (in SBT, run: tqaJVM/*:mimaReportBinaryIssues):

* method evaluate(eu.cdevreeze.tqa.StringValueOrExpr,eu.cdevreeze.yaidom.xpath.XPathEvaluator,eu.cdevreeze.yaidom.core.Scope)java.lang.String in object eu.cdevreeze.tqa.xpathaware.StringValueOrExprEvaluator does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.xpathaware.StringValueOrExprEvaluator.evaluate")
* method evaluate(eu.cdevreeze.tqa.ENameValueOrExpr,eu.cdevreeze.yaidom.xpath.XPathEvaluator,eu.cdevreeze.yaidom.core.Scope)eu.cdevreeze.yaidom.core.EName in object eu.cdevreeze.tqa.xpathaware.ENameValueOrExprEvaluator does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.xpathaware.ENameValueOrExprEvaluator.evaluate")
* method evaluate(eu.cdevreeze.tqa.BigDecimalValueOrExpr,eu.cdevreeze.yaidom.xpath.XPathEvaluator,eu.cdevreeze.yaidom.core.Scope)scala.math.BigDecimal in object eu.cdevreeze.tqa.xpathaware.BigDecimalValueOrExprEvaluator does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.xpathaware.BigDecimalValueOrExprEvaluator.evaluate")
* abstract method evaluate(eu.cdevreeze.tqa.ValueOrExpr,eu.cdevreeze.yaidom.xpath.XPathEvaluator,eu.cdevreeze.yaidom.core.Scope)java.lang.Object in interface eu.cdevreeze.tqa.xpathaware.ValueOrExprEvaluator does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.xpathaware.ValueOrExprEvaluator.evaluate")
* abstract method evaluate(eu.cdevreeze.tqa.ValueOrExpr,eu.cdevreeze.yaidom.xpath.XPathEvaluator)java.lang.Object in interface eu.cdevreeze.tqa.xpathaware.ValueOrExprEvaluator is present only in current version
  filter with: ProblemFilters.exclude[ReversedMissingMethodProblem]("eu.cdevreeze.tqa.xpathaware.ValueOrExprEvaluator.evaluate")
* method memberOption(eu.cdevreeze.yaidom.xpath.XPathEvaluator,eu.cdevreeze.yaidom.core.Scope)scala.Option in class eu.cdevreeze.tqa.xpathaware.extension.formula.ExplicitDimensionAspectData does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.xpathaware.extension.formula.ExplicitDimensionAspectData.memberOption")
* method qnameValueOption(eu.cdevreeze.yaidom.xpath.XPathEvaluator,eu.cdevreeze.yaidom.core.Scope)scala.Option in class eu.cdevreeze.tqa.xpathaware.extension.formula.ConceptAspectData does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.xpathaware.extension.formula.ConceptAspectData.qnameValueOption")
* method relationshipSources(eu.cdevreeze.yaidom.xpath.XPathEvaluator,eu.cdevreeze.yaidom.core.Scope)scala.collection.immutable.IndexedSeq in class eu.cdevreeze.tqa.xpathaware.extension.table.ConceptRelationshipNodeData does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.xpathaware.extension.table.ConceptRelationshipNodeData.relationshipSources")
* method linknameOption(eu.cdevreeze.yaidom.xpath.XPathEvaluator,eu.cdevreeze.yaidom.core.Scope)scala.Option in class eu.cdevreeze.tqa.xpathaware.extension.table.ConceptRelationshipNodeData does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.xpathaware.extension.table.ConceptRelationshipNodeData.linknameOption")
* method generations(eu.cdevreeze.yaidom.xpath.XPathEvaluator,eu.cdevreeze.yaidom.core.Scope)Int in class eu.cdevreeze.tqa.xpathaware.extension.table.ConceptRelationshipNodeData does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.xpathaware.extension.table.ConceptRelationshipNodeData.generations")
* method arcrole(eu.cdevreeze.yaidom.xpath.XPathEvaluator,eu.cdevreeze.yaidom.core.Scope)java.lang.String in class eu.cdevreeze.tqa.xpathaware.extension.table.ConceptRelationshipNodeData does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.xpathaware.extension.table.ConceptRelationshipNodeData.arcrole")
* method arcnameOption(eu.cdevreeze.yaidom.xpath.XPathEvaluator,eu.cdevreeze.yaidom.core.Scope)scala.Option in class eu.cdevreeze.tqa.xpathaware.extension.table.ConceptRelationshipNodeData does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.xpathaware.extension.table.ConceptRelationshipNodeData.arcnameOption")
* method formulaAxis(eu.cdevreeze.yaidom.xpath.XPathEvaluator,eu.cdevreeze.yaidom.core.Scope)eu.cdevreeze.tqa.extension.table.common.ConceptRelationshipNodes#FormulaAxis in class eu.cdevreeze.tqa.xpathaware.extension.table.ConceptRelationshipNodeData does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.xpathaware.extension.table.ConceptRelationshipNodeData.formulaAxis")
* method linkroleOption(eu.cdevreeze.yaidom.xpath.XPathEvaluator,eu.cdevreeze.yaidom.core.Scope)scala.Option in class eu.cdevreeze.tqa.xpathaware.extension.table.ConceptRelationshipNodeData does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.xpathaware.extension.table.ConceptRelationshipNodeData.linkroleOption")
* object eu.cdevreeze.tqa.xpathaware.extension.table.ConceptRelationshipNodeData#ConceptTreeWalkSpec does not have a correspondent in current version
  filter with: ProblemFilters.exclude[MissingClassProblem]("eu.cdevreeze.tqa.xpathaware.extension.table.ConceptRelationshipNodeData$ConceptTreeWalkSpec$")
* class eu.cdevreeze.tqa.xpathaware.extension.table.DimensionRelationshipNodeData#DimensionMemberTreeWalkSpec#MemberSource does not have a correspondent in current version
  filter with: ProblemFilters.exclude[MissingClassProblem]("eu.cdevreeze.tqa.xpathaware.extension.table.DimensionRelationshipNodeData$DimensionMemberTreeWalkSpec$MemberSource")
* object eu.cdevreeze.tqa.xpathaware.extension.table.DimensionRelationshipNodeData#DimensionMemberTreeWalkSpec does not have a correspondent in current version
  filter with: ProblemFilters.exclude[MissingClassProblem]("eu.cdevreeze.tqa.xpathaware.extension.table.DimensionRelationshipNodeData$DimensionMemberTreeWalkSpec$")
* class eu.cdevreeze.tqa.xpathaware.extension.table.DimensionRelationshipNodeData#DimensionMemberTreeWalkSpec#DimensionDomainSource does not have a correspondent in current version
  filter with: ProblemFilters.exclude[MissingClassProblem]("eu.cdevreeze.tqa.xpathaware.extension.table.DimensionRelationshipNodeData$DimensionMemberTreeWalkSpec$DimensionDomainSource")
* interface eu.cdevreeze.tqa.xpathaware.extension.table.DimensionRelationshipNodeData#DimensionMemberTreeWalkSpec#StartMember does not have a correspondent in current version
  filter with: ProblemFilters.exclude[MissingClassProblem]("eu.cdevreeze.tqa.xpathaware.extension.table.DimensionRelationshipNodeData$DimensionMemberTreeWalkSpec$StartMember")
* method findAllMembersInDimensionRelationshipNode(eu.cdevreeze.tqa.extension.table.dom.DimensionRelationshipNode,eu.cdevreeze.tqa.extension.table.taxonomy.BasicTableTaxonomy,eu.cdevreeze.yaidom.xpath.XPathEvaluator,eu.cdevreeze.yaidom.core.Scope)scala.collection.immutable.Set in object eu.cdevreeze.tqa.xpathaware.extension.table.DimensionRelationshipNodeData does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.xpathaware.extension.table.DimensionRelationshipNodeData.findAllMembersInDimensionRelationshipNode")
* method filterDescendantOrSelfMembers(eu.cdevreeze.tqa.xpathaware.extension.table.DimensionRelationshipNodeData#DimensionMemberTreeWalkSpec,eu.cdevreeze.tqa.extension.table.taxonomy.BasicTableTaxonomy)scala.collection.immutable.Set in object eu.cdevreeze.tqa.xpathaware.extension.table.DimensionRelationshipNodeData does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.xpathaware.extension.table.DimensionRelationshipNodeData.filterDescendantOrSelfMembers")
* class eu.cdevreeze.tqa.xpathaware.extension.table.ConceptRelationshipNodeData#ConceptTreeWalkSpec does not have a correspondent in current version
  filter with: ProblemFilters.exclude[MissingClassProblem]("eu.cdevreeze.tqa.xpathaware.extension.table.ConceptRelationshipNodeData$ConceptTreeWalkSpec")
* class eu.cdevreeze.tqa.xpathaware.extension.table.DimensionRelationshipNodeData#DimensionMemberTreeWalkSpec does not have a correspondent in current version
  filter with: ProblemFilters.exclude[MissingClassProblem]("eu.cdevreeze.tqa.xpathaware.extension.table.DimensionRelationshipNodeData$DimensionMemberTreeWalkSpec")
* method relationshipSources(eu.cdevreeze.yaidom.xpath.XPathEvaluator,eu.cdevreeze.yaidom.core.Scope)scala.collection.immutable.IndexedSeq in class eu.cdevreeze.tqa.xpathaware.extension.table.DimensionRelationshipNodeData does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.xpathaware.extension.table.DimensionRelationshipNodeData.relationshipSources")
* method generations(eu.cdevreeze.yaidom.xpath.XPathEvaluator,eu.cdevreeze.yaidom.core.Scope)Int in class eu.cdevreeze.tqa.xpathaware.extension.table.DimensionRelationshipNodeData does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.xpathaware.extension.table.DimensionRelationshipNodeData.generations")
* method formulaAxis(eu.cdevreeze.yaidom.xpath.XPathEvaluator,eu.cdevreeze.yaidom.core.Scope)eu.cdevreeze.tqa.extension.table.common.DimensionRelationshipNodes#FormulaAxis in class eu.cdevreeze.tqa.xpathaware.extension.table.DimensionRelationshipNodeData does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.xpathaware.extension.table.DimensionRelationshipNodeData.formulaAxis")
* method linkroleOption(eu.cdevreeze.yaidom.xpath.XPathEvaluator,eu.cdevreeze.yaidom.core.Scope)scala.Option in class eu.cdevreeze.tqa.xpathaware.extension.table.DimensionRelationshipNodeData does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.xpathaware.extension.table.DimensionRelationshipNodeData.linkroleOption")
* method findAllConceptsInConceptRelationshipNode(eu.cdevreeze.tqa.extension.table.dom.ConceptRelationshipNode,eu.cdevreeze.tqa.extension.table.taxonomy.BasicTableTaxonomy,eu.cdevreeze.yaidom.xpath.XPathEvaluator,eu.cdevreeze.yaidom.core.Scope)scala.collection.immutable.Set in object eu.cdevreeze.tqa.xpathaware.extension.table.ConceptRelationshipNodeData does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.xpathaware.extension.table.ConceptRelationshipNodeData.findAllConceptsInConceptRelationshipNode")
* method filterDescendantOrSelfConcepts(eu.cdevreeze.tqa.xpathaware.extension.table.ConceptRelationshipNodeData#ConceptTreeWalkSpec,eu.cdevreeze.tqa.extension.table.taxonomy.BasicTableTaxonomy)scala.collection.immutable.Set in object eu.cdevreeze.tqa.xpathaware.extension.table.ConceptRelationshipNodeData does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.xpathaware.extension.table.ConceptRelationshipNodeData.filterDescendantOrSelfConcepts")
* abstract method filterIncomingHypercubeDimensionRelationships(eu.cdevreeze.yaidom.core.EName,scala.Function1)scala.collection.immutable.IndexedSeq in interface eu.cdevreeze.tqa.base.queryapi.DimensionalRelationshipContainerApi is present only in current version
  filter with: ProblemFilters.exclude[ReversedMissingMethodProblem]("eu.cdevreeze.tqa.base.queryapi.DimensionalRelationshipContainerApi.filterIncomingHypercubeDimensionRelationships")
* abstract method filterIncomingHasHypercubeRelationships(eu.cdevreeze.yaidom.core.EName,scala.Function1)scala.collection.immutable.IndexedSeq in interface eu.cdevreeze.tqa.base.queryapi.DimensionalRelationshipContainerApi is present only in current version
  filter with: ProblemFilters.exclude[ReversedMissingMethodProblem]("eu.cdevreeze.tqa.base.queryapi.DimensionalRelationshipContainerApi.filterIncomingHasHypercubeRelationships")
* abstract method findAllIncomingHasHypercubeRelationships(eu.cdevreeze.yaidom.core.EName)scala.collection.immutable.IndexedSeq in interface eu.cdevreeze.tqa.base.queryapi.DimensionalRelationshipContainerApi is present only in current version
  filter with: ProblemFilters.exclude[ReversedMissingMethodProblem]("eu.cdevreeze.tqa.base.queryapi.DimensionalRelationshipContainerApi.findAllIncomingHasHypercubeRelationships")
* abstract method findAllIncomingHypercubeDimensionRelationships(eu.cdevreeze.yaidom.core.EName)scala.collection.immutable.IndexedSeq in interface eu.cdevreeze.tqa.base.queryapi.DimensionalRelationshipContainerApi is present only in current version
  filter with: ProblemFilters.exclude[ReversedMissingMethodProblem]("eu.cdevreeze.tqa.base.queryapi.DimensionalRelationshipContainerApi.findAllIncomingHypercubeDimensionRelationships")


0.8.0
=====

Compared with release 0.7.1, the main changes in this version are:

* Dependency on yaidom 1.8.0

  * Therefore depending on yaidom's Saxon wrapper elements and yaidom's XPath support
  * The Saxon wrapper elements and XPath support therefore no longer live in the TQA project
  * Yaidom 1.8.0 is also leveraged by using its ``ScopedNodes.Elem`` trait for the taxonomy and instance "yaidom dialects"
  * These type-safe DOM elements contain ``BackingNodes.Elem`` backing elements, thus abstracting over the backing element implementation

* More taxonomy query API methods and traits, like added support for element-label relationship querying
* Some refactoring, like moving document builders (for Saxon and native yaidom indexed documents) to another package

Breaking changes compared to version 0.7.1 (in SBT, run: tqaJVM/*:mimaReportBinaryIssues):

* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.XmlFragmentKey#XmlFragmentKeyAware has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.XmlFragmentKey#XmlFragmentKeyAware.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi)Unit in class eu.cdevreeze.tqa.XmlFragmentKey#XmlFragmentKeyAware's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.XmlFragmentKey#XmlFragmentKeyAware.this")
* method XmlFragmentKeyAware(eu.cdevreeze.yaidom.queryapi.BackingElemApi)eu.cdevreeze.tqa.XmlFragmentKey#XmlFragmentKeyAware in object eu.cdevreeze.tqa.XmlFragmentKey's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem)eu.cdevreeze.tqa.XmlFragmentKey#XmlFragmentKeyAware instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi)eu.cdevreeze.tqa.XmlFragmentKey#XmlFragmentKeyAware
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.XmlFragmentKey.XmlFragmentKeyAware")
* method underlyingParentElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in interface eu.cdevreeze.tqa.xlink.ChildXLink has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.xlink.ChildXLink.underlyingParentElem")
* abstract method underlyingParentElem()eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem in interface eu.cdevreeze.tqa.xlink.ChildXLink is present only in current version
  filter with: ProblemFilters.exclude[ReversedMissingMethodProblem]("eu.cdevreeze.tqa.xlink.ChildXLink.underlyingParentElem")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in interface eu.cdevreeze.tqa.xlink.XLinkElem has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.xlink.XLinkElem.backingElem")
* abstract method backingElem()eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem in interface eu.cdevreeze.tqa.xlink.XLinkElem is present only in current version
  filter with: ProblemFilters.exclude[ReversedMissingMethodProblem]("eu.cdevreeze.tqa.xlink.XLinkElem.backingElem")
* method fromElem(eu.cdevreeze.yaidom.queryapi.BackingElemApi)eu.cdevreeze.tqa.docbuilder.SimpleCatalog in object eu.cdevreeze.tqa.docbuilder.SimpleCatalog's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem)eu.cdevreeze.tqa.docbuilder.SimpleCatalog instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi)eu.cdevreeze.tqa.docbuilder.SimpleCatalog
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.docbuilder.SimpleCatalog.fromElem")
* method fromElem(eu.cdevreeze.yaidom.queryapi.BackingElemApi)eu.cdevreeze.tqa.docbuilder.SimpleCatalog#UriRewrite in object eu.cdevreeze.tqa.docbuilder.SimpleCatalog#UriRewrite's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem)eu.cdevreeze.tqa.docbuilder.SimpleCatalog#UriRewrite instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi)eu.cdevreeze.tqa.docbuilder.SimpleCatalog#UriRewrite
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.docbuilder.SimpleCatalog#UriRewrite.fromElem")
* method evaluate(eu.cdevreeze.tqa.StringValueOrExpr,eu.cdevreeze.tqa.xpath.XPathEvaluator)java.lang.String in object eu.cdevreeze.tqa.xpathaware.StringValueOrExprEvaluator does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.xpathaware.StringValueOrExprEvaluator.evaluate")
* method evaluate(eu.cdevreeze.tqa.ENameValueOrExpr,eu.cdevreeze.tqa.xpath.XPathEvaluator)eu.cdevreeze.yaidom.core.EName in object eu.cdevreeze.tqa.xpathaware.ENameValueOrExprEvaluator does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.xpathaware.ENameValueOrExprEvaluator.evaluate")
* method evaluate(eu.cdevreeze.tqa.BigDecimalValueOrExpr,eu.cdevreeze.tqa.xpath.XPathEvaluator)scala.math.BigDecimal in object eu.cdevreeze.tqa.xpathaware.BigDecimalValueOrExprEvaluator does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.xpathaware.BigDecimalValueOrExprEvaluator.evaluate")
* abstract method evaluate(eu.cdevreeze.tqa.ValueOrExpr,eu.cdevreeze.tqa.xpath.XPathEvaluator)java.lang.Object in interface eu.cdevreeze.tqa.xpathaware.ValueOrExprEvaluator does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.xpathaware.ValueOrExprEvaluator.evaluate")
* abstract method evaluate(eu.cdevreeze.tqa.ValueOrExpr,eu.cdevreeze.yaidom.xpath.XPathEvaluator,eu.cdevreeze.yaidom.core.Scope)java.lang.Object in interface eu.cdevreeze.tqa.xpathaware.ValueOrExprEvaluator is present only in current version
  filter with: ProblemFilters.exclude[ReversedMissingMethodProblem]("eu.cdevreeze.tqa.xpathaware.ValueOrExprEvaluator.evaluate")
* method memberOption(eu.cdevreeze.tqa.xpath.XPathEvaluator)scala.Option in class eu.cdevreeze.tqa.xpathaware.extension.formula.ExplicitDimensionAspectData does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.xpathaware.extension.formula.ExplicitDimensionAspectData.memberOption")
* method qnameValueOption(eu.cdevreeze.tqa.xpath.XPathEvaluator)scala.Option in class eu.cdevreeze.tqa.xpathaware.extension.formula.ConceptAspectData does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.xpathaware.extension.formula.ConceptAspectData.qnameValueOption")
* method valueOption(eu.cdevreeze.tqa.xpath.XPathEvaluator)scala.Option in class eu.cdevreeze.tqa.xpathaware.extension.formula.TypedDimensionAspectData's type is different in current version, where it is (eu.cdevreeze.yaidom.xpath.XPathEvaluator)scala.Option instead of (eu.cdevreeze.tqa.xpath.XPathEvaluator)scala.Option
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.xpathaware.extension.formula.TypedDimensionAspectData.valueOption")
* method relationshipSources(eu.cdevreeze.tqa.xpath.XPathEvaluator)scala.collection.immutable.IndexedSeq in class eu.cdevreeze.tqa.xpathaware.extension.table.ConceptRelationshipNodeData does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.xpathaware.extension.table.ConceptRelationshipNodeData.relationshipSources")
* method linknameOption(eu.cdevreeze.tqa.xpath.XPathEvaluator)scala.Option in class eu.cdevreeze.tqa.xpathaware.extension.table.ConceptRelationshipNodeData does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.xpathaware.extension.table.ConceptRelationshipNodeData.linknameOption")
* method generations(eu.cdevreeze.tqa.xpath.XPathEvaluator)Int in class eu.cdevreeze.tqa.xpathaware.extension.table.ConceptRelationshipNodeData does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.xpathaware.extension.table.ConceptRelationshipNodeData.generations")
* method arcrole(eu.cdevreeze.tqa.xpath.XPathEvaluator)java.lang.String in class eu.cdevreeze.tqa.xpathaware.extension.table.ConceptRelationshipNodeData does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.xpathaware.extension.table.ConceptRelationshipNodeData.arcrole")
* method arcnameOption(eu.cdevreeze.tqa.xpath.XPathEvaluator)scala.Option in class eu.cdevreeze.tqa.xpathaware.extension.table.ConceptRelationshipNodeData does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.xpathaware.extension.table.ConceptRelationshipNodeData.arcnameOption")
* method formulaAxis(eu.cdevreeze.tqa.xpath.XPathEvaluator)eu.cdevreeze.tqa.extension.table.common.ConceptRelationshipNodes#FormulaAxis in class eu.cdevreeze.tqa.xpathaware.extension.table.ConceptRelationshipNodeData does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.xpathaware.extension.table.ConceptRelationshipNodeData.formulaAxis")
* method linkroleOption(eu.cdevreeze.tqa.xpath.XPathEvaluator)scala.Option in class eu.cdevreeze.tqa.xpathaware.extension.table.ConceptRelationshipNodeData does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.xpathaware.extension.table.ConceptRelationshipNodeData.linkroleOption")
* method findAllMembersInDimensionRelationshipNode(eu.cdevreeze.tqa.extension.table.dom.DimensionRelationshipNode,eu.cdevreeze.tqa.extension.table.taxonomy.BasicTableTaxonomy,eu.cdevreeze.tqa.xpath.XPathEvaluator)scala.collection.immutable.Set in object eu.cdevreeze.tqa.xpathaware.extension.table.DimensionRelationshipNodeData does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.xpathaware.extension.table.DimensionRelationshipNodeData.findAllMembersInDimensionRelationshipNode")
* method relationshipSources(eu.cdevreeze.tqa.xpath.XPathEvaluator)scala.collection.immutable.IndexedSeq in class eu.cdevreeze.tqa.xpathaware.extension.table.DimensionRelationshipNodeData does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.xpathaware.extension.table.DimensionRelationshipNodeData.relationshipSources")
* method generations(eu.cdevreeze.tqa.xpath.XPathEvaluator)Int in class eu.cdevreeze.tqa.xpathaware.extension.table.DimensionRelationshipNodeData does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.xpathaware.extension.table.DimensionRelationshipNodeData.generations")
* method formulaAxis(eu.cdevreeze.tqa.xpath.XPathEvaluator)eu.cdevreeze.tqa.extension.table.common.DimensionRelationshipNodes#FormulaAxis in class eu.cdevreeze.tqa.xpathaware.extension.table.DimensionRelationshipNodeData does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.xpathaware.extension.table.DimensionRelationshipNodeData.formulaAxis")
* method linkroleOption(eu.cdevreeze.tqa.xpath.XPathEvaluator)scala.Option in class eu.cdevreeze.tqa.xpathaware.extension.table.DimensionRelationshipNodeData does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.xpathaware.extension.table.DimensionRelationshipNodeData.linkroleOption")
* method findAllConceptsInConceptRelationshipNode(eu.cdevreeze.tqa.extension.table.dom.ConceptRelationshipNode,eu.cdevreeze.tqa.extension.table.taxonomy.BasicTableTaxonomy,eu.cdevreeze.tqa.xpath.XPathEvaluator)scala.collection.immutable.Set in object eu.cdevreeze.tqa.xpathaware.extension.table.ConceptRelationshipNodeData does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.xpathaware.extension.table.ConceptRelationshipNodeData.findAllConceptsInConceptRelationshipNode")
* method filterDocumentUris(scala.collection.immutable.Set)eu.cdevreeze.tqa.base.taxonomy.BasicTaxonomy in class eu.cdevreeze.tqa.base.taxonomy.BasicTaxonomy does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.base.taxonomy.BasicTaxonomy.filterDocumentUris")
* method filterIncomingInterConceptRelationshipPaths(eu.cdevreeze.yaidom.core.EName,scala.reflect.ClassTag,scala.Function1)scala.collection.immutable.IndexedSeq in class eu.cdevreeze.tqa.base.taxonomy.BasicTaxonomy does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.base.taxonomy.BasicTaxonomy.filterIncomingInterConceptRelationshipPaths")
* method filterRelationships(scala.Function1)eu.cdevreeze.tqa.base.taxonomy.BasicTaxonomy in class eu.cdevreeze.tqa.base.taxonomy.BasicTaxonomy does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.base.taxonomy.BasicTaxonomy.filterRelationships")
* method filterOutgoingInterConceptRelationshipPaths(eu.cdevreeze.yaidom.core.EName,scala.reflect.ClassTag,scala.Function1)scala.collection.immutable.IndexedSeq in class eu.cdevreeze.tqa.base.taxonomy.BasicTaxonomy does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.base.taxonomy.BasicTaxonomy.filterOutgoingInterConceptRelationshipPaths")
* abstract method filterOutgoingElementLabelRelationships(eu.cdevreeze.tqa.XmlFragmentKey,scala.Function1)scala.collection.immutable.IndexedSeq in interface eu.cdevreeze.tqa.base.queryapi.ElementLabelRelationshipContainerApi is inherited by class TaxonomyApi in current version.
  filter with: ProblemFilters.exclude[InheritedNewAbstractMethodProblem]("eu.cdevreeze.tqa.base.queryapi.ElementLabelRelationshipContainerApi.filterOutgoingElementLabelRelationships")
* abstract method findAllOutgoingElementLabelRelationships(eu.cdevreeze.tqa.XmlFragmentKey)scala.collection.immutable.IndexedSeq in interface eu.cdevreeze.tqa.base.queryapi.ElementLabelRelationshipContainerApi is inherited by class TaxonomyApi in current version.
  filter with: ProblemFilters.exclude[InheritedNewAbstractMethodProblem]("eu.cdevreeze.tqa.base.queryapi.ElementLabelRelationshipContainerApi.findAllOutgoingElementLabelRelationships")
* abstract method findAllElementLabelRelationships()scala.collection.immutable.IndexedSeq in interface eu.cdevreeze.tqa.base.queryapi.ElementLabelRelationshipContainerApi is inherited by class TaxonomyApi in current version.
  filter with: ProblemFilters.exclude[InheritedNewAbstractMethodProblem]("eu.cdevreeze.tqa.base.queryapi.ElementLabelRelationshipContainerApi.findAllElementLabelRelationships")
* abstract method filterElementLabelRelationships(scala.Function1)scala.collection.immutable.IndexedSeq in interface eu.cdevreeze.tqa.base.queryapi.ElementLabelRelationshipContainerApi is inherited by class TaxonomyApi in current version.
  filter with: ProblemFilters.exclude[InheritedNewAbstractMethodProblem]("eu.cdevreeze.tqa.base.queryapi.ElementLabelRelationshipContainerApi.filterElementLabelRelationships")
* abstract method filterElementReferenceRelationships(scala.Function1)scala.collection.immutable.IndexedSeq in interface eu.cdevreeze.tqa.base.queryapi.ElementReferenceRelationshipContainerApi is inherited by class TaxonomyApi in current version.
  filter with: ProblemFilters.exclude[InheritedNewAbstractMethodProblem]("eu.cdevreeze.tqa.base.queryapi.ElementReferenceRelationshipContainerApi.filterElementReferenceRelationships")
* abstract method findAllOutgoingElementReferenceRelationships(eu.cdevreeze.tqa.XmlFragmentKey)scala.collection.immutable.IndexedSeq in interface eu.cdevreeze.tqa.base.queryapi.ElementReferenceRelationshipContainerApi is inherited by class TaxonomyApi in current version.
  filter with: ProblemFilters.exclude[InheritedNewAbstractMethodProblem]("eu.cdevreeze.tqa.base.queryapi.ElementReferenceRelationshipContainerApi.findAllOutgoingElementReferenceRelationships")
* abstract method findAllElementReferenceRelationships()scala.collection.immutable.IndexedSeq in interface eu.cdevreeze.tqa.base.queryapi.ElementReferenceRelationshipContainerApi is inherited by class TaxonomyApi in current version.
  filter with: ProblemFilters.exclude[InheritedNewAbstractMethodProblem]("eu.cdevreeze.tqa.base.queryapi.ElementReferenceRelationshipContainerApi.findAllElementReferenceRelationships")
* abstract method filterOutgoingElementReferenceRelationships(eu.cdevreeze.tqa.XmlFragmentKey,scala.Function1)scala.collection.immutable.IndexedSeq in interface eu.cdevreeze.tqa.base.queryapi.ElementReferenceRelationshipContainerApi is inherited by class TaxonomyApi in current version.
  filter with: ProblemFilters.exclude[InheritedNewAbstractMethodProblem]("eu.cdevreeze.tqa.base.queryapi.ElementReferenceRelationshipContainerApi.filterOutgoingElementReferenceRelationships")
* method filterIncomingInterConceptRelationshipPaths(eu.cdevreeze.yaidom.core.EName,scala.reflect.ClassTag,scala.Function1)scala.collection.immutable.IndexedSeq in interface eu.cdevreeze.tqa.base.queryapi.InterConceptRelationshipContainerLike does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.base.queryapi.InterConceptRelationshipContainerLike.filterIncomingInterConceptRelationshipPaths")
* method filterOutgoingInterConceptRelationshipPaths(eu.cdevreeze.yaidom.core.EName,scala.reflect.ClassTag,scala.Function1)scala.collection.immutable.IndexedSeq in interface eu.cdevreeze.tqa.base.queryapi.InterConceptRelationshipContainerLike does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.base.queryapi.InterConceptRelationshipContainerLike.filterOutgoingInterConceptRelationshipPaths")
* abstract method filterOutgoingNonStandardRelationships(eu.cdevreeze.tqa.XmlFragmentKey,scala.Function1)scala.collection.immutable.IndexedSeq in interface eu.cdevreeze.tqa.base.queryapi.NonStandardRelationshipContainerApi is present only in current version
  filter with: ProblemFilters.exclude[ReversedMissingMethodProblem]("eu.cdevreeze.tqa.base.queryapi.NonStandardRelationshipContainerApi.filterOutgoingNonStandardRelationships")
* abstract method findAllOutgoingNonStandardRelationships(eu.cdevreeze.tqa.XmlFragmentKey)scala.collection.immutable.IndexedSeq in interface eu.cdevreeze.tqa.base.queryapi.NonStandardRelationshipContainerApi is present only in current version
  filter with: ProblemFilters.exclude[ReversedMissingMethodProblem]("eu.cdevreeze.tqa.base.queryapi.NonStandardRelationshipContainerApi.findAllOutgoingNonStandardRelationships")
* abstract method filterIncomingInterConceptRelationshipPaths(eu.cdevreeze.yaidom.core.EName,scala.reflect.ClassTag,scala.Function1)scala.collection.immutable.IndexedSeq in interface eu.cdevreeze.tqa.base.queryapi.InterConceptRelationshipContainerApi does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.base.queryapi.InterConceptRelationshipContainerApi.filterIncomingInterConceptRelationshipPaths")
* abstract method filterOutgoingInterConceptRelationshipPaths(eu.cdevreeze.yaidom.core.EName,scala.reflect.ClassTag,scala.Function1)scala.collection.immutable.IndexedSeq in interface eu.cdevreeze.tqa.base.queryapi.InterConceptRelationshipContainerApi does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.base.queryapi.InterConceptRelationshipContainerApi.filterOutgoingInterConceptRelationshipPaths")
* abstract method filterIncomingInterConceptRelationships(eu.cdevreeze.yaidom.core.EName,scala.Function1)scala.collection.immutable.IndexedSeq in interface eu.cdevreeze.tqa.base.queryapi.InterConceptRelationshipContainerApi is present only in current version
  filter with: ProblemFilters.exclude[ReversedMissingMethodProblem]("eu.cdevreeze.tqa.base.queryapi.InterConceptRelationshipContainerApi.filterIncomingInterConceptRelationships")
* abstract method filterOutgoingInterConceptRelationships(eu.cdevreeze.yaidom.core.EName,scala.Function1)scala.collection.immutable.IndexedSeq in interface eu.cdevreeze.tqa.base.queryapi.InterConceptRelationshipContainerApi is present only in current version
  filter with: ProblemFilters.exclude[ReversedMissingMethodProblem]("eu.cdevreeze.tqa.base.queryapi.InterConceptRelationshipContainerApi.filterOutgoingInterConceptRelationships")
* abstract method findAllIncomingInterConceptRelationships(eu.cdevreeze.yaidom.core.EName)scala.collection.immutable.IndexedSeq in interface eu.cdevreeze.tqa.base.queryapi.InterConceptRelationshipContainerApi is present only in current version
  filter with: ProblemFilters.exclude[ReversedMissingMethodProblem]("eu.cdevreeze.tqa.base.queryapi.InterConceptRelationshipContainerApi.findAllIncomingInterConceptRelationships")
* abstract method findAllOutgoingInterConceptRelationships(eu.cdevreeze.yaidom.core.EName)scala.collection.immutable.IndexedSeq in interface eu.cdevreeze.tqa.base.queryapi.InterConceptRelationshipContainerApi is present only in current version
  filter with: ProblemFilters.exclude[ReversedMissingMethodProblem]("eu.cdevreeze.tqa.base.queryapi.InterConceptRelationshipContainerApi.findAllOutgoingInterConceptRelationships")
* abstract method filterOutgoingUnrestrictedInterConceptRelationshipPaths(eu.cdevreeze.yaidom.core.EName,scala.reflect.ClassTag,scala.Function1)scala.collection.immutable.IndexedSeq in interface eu.cdevreeze.tqa.base.queryapi.InterConceptRelationshipContainerApi is present only in current version
  filter with: ProblemFilters.exclude[ReversedMissingMethodProblem]("eu.cdevreeze.tqa.base.queryapi.InterConceptRelationshipContainerApi.filterOutgoingUnrestrictedInterConceptRelationshipPaths")
* abstract method filterIncomingUnrestrictedInterConceptRelationshipPaths(eu.cdevreeze.yaidom.core.EName,scala.reflect.ClassTag,scala.Function1)scala.collection.immutable.IndexedSeq in interface eu.cdevreeze.tqa.base.queryapi.InterConceptRelationshipContainerApi is present only in current version
  filter with: ProblemFilters.exclude[ReversedMissingMethodProblem]("eu.cdevreeze.tqa.base.queryapi.InterConceptRelationshipContainerApi.filterIncomingUnrestrictedInterConceptRelationshipPaths")
* abstract method filterOutgoingStandardRelationships(eu.cdevreeze.yaidom.core.EName,scala.Function1)scala.collection.immutable.IndexedSeq in interface eu.cdevreeze.tqa.base.queryapi.StandardRelationshipContainerApi is present only in current version
  filter with: ProblemFilters.exclude[ReversedMissingMethodProblem]("eu.cdevreeze.tqa.base.queryapi.StandardRelationshipContainerApi.filterOutgoingStandardRelationships")
* abstract method findAllOutgoingStandardRelationships(eu.cdevreeze.yaidom.core.EName)scala.collection.immutable.IndexedSeq in interface eu.cdevreeze.tqa.base.queryapi.StandardRelationshipContainerApi is present only in current version
  filter with: ProblemFilters.exclude[ReversedMissingMethodProblem]("eu.cdevreeze.tqa.base.queryapi.StandardRelationshipContainerApi.findAllOutgoingStandardRelationships")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.base.taxonomybuilder.VersioningReport has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.VersioningReport.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.taxonomybuilder.VersioningReport's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.VersioningReport.this")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.base.taxonomybuilder.PublisherCountry has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.PublisherCountry.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.taxonomybuilder.PublisherCountry's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.PublisherCountry.this")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.base.taxonomybuilder.Language has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.Language.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.taxonomybuilder.Language's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.Language.this")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.base.taxonomybuilder.LanguagesElem has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.LanguagesElem.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.taxonomybuilder.LanguagesElem's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.LanguagesElem.this")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.base.taxonomybuilder.Version has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.Version.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.taxonomybuilder.Version's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.Version.this")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.base.taxonomybuilder.PublicationDate has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.PublicationDate.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.taxonomybuilder.PublicationDate's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.PublicationDate.this")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.base.taxonomybuilder.PublisherUrl has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.PublisherUrl.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.taxonomybuilder.PublisherUrl's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.PublisherUrl.this")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.base.taxonomybuilder.License has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.License.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.taxonomybuilder.License's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.License.this")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.base.taxonomybuilder.Identifier has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.Identifier.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.taxonomybuilder.Identifier's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.Identifier.this")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.base.taxonomybuilder.EntryPoint has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.EntryPoint.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.taxonomybuilder.EntryPoint's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.EntryPoint.this")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.base.taxonomybuilder.EntryPointsElem has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.EntryPointsElem.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.taxonomybuilder.EntryPointsElem's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.EntryPointsElem.this")
* method apply(eu.cdevreeze.yaidom.queryapi.BackingElemApi)eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyPackage in object eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyPackage's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem)eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyPackage instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi)eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyPackage
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyPackage.apply")
* method apply(eu.cdevreeze.yaidom.queryapi.BackingElemApi)eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyPackageElem in object eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyPackageElem's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem)eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyPackageElem instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi)eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyPackageElem
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyPackageElem.apply")
* method apply(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyPackageElem in object eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyPackageElem's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyPackageElem instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyPackageElem
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyPackageElem.apply")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.base.taxonomybuilder.SupersededTaxonomyPackagesElem has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.SupersededTaxonomyPackagesElem.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.taxonomybuilder.SupersededTaxonomyPackagesElem's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.SupersededTaxonomyPackagesElem.this")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyPackageRef has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyPackageRef.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyPackageRef's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyPackageRef.this")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.base.taxonomybuilder.EntryPointDocument has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.EntryPointDocument.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.taxonomybuilder.EntryPointDocument's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.EntryPointDocument.this")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyPackageElem has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyPackageElem.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyPackageElem's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyPackageElem.this")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.base.taxonomybuilder.VersioningReportsElem has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.VersioningReportsElem.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.taxonomybuilder.VersioningReportsElem's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.VersioningReportsElem.this")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.base.taxonomybuilder.Description has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.Description.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.taxonomybuilder.Description's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.Description.this")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.base.taxonomybuilder.Name has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.Name.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.taxonomybuilder.Name's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.Name.this")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.base.taxonomybuilder.Publisher has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.Publisher.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.taxonomybuilder.Publisher's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.Publisher.this")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyPackage has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyPackage.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyPackage's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyPackage.this")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.Appinfo's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.Appinfo.this")
* method underlyingParentElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.base.dom.StandardLoc has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.base.dom.StandardLoc.underlyingParentElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.StandardLoc's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.StandardLoc.this")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.ReferenceLink's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.ReferenceLink.this")
* method buildOptionally(eu.cdevreeze.yaidom.queryapi.BackingElemApi)scala.Option in object eu.cdevreeze.tqa.base.dom.TaxonomyRootElem's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem)scala.Option instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi)scala.Option
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.TaxonomyRootElem.buildOptionally")
* method build(eu.cdevreeze.yaidom.queryapi.BackingElemApi)eu.cdevreeze.tqa.base.dom.TaxonomyRootElem in object eu.cdevreeze.tqa.base.dom.TaxonomyRootElem's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem)eu.cdevreeze.tqa.base.dom.TaxonomyRootElem instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi)eu.cdevreeze.tqa.base.dom.TaxonomyRootElem
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.TaxonomyRootElem.build")
* method opt(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)scala.Option in object eu.cdevreeze.tqa.base.dom.AttributeDeclarationOrReference's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)scala.Option instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)scala.Option
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.AttributeDeclarationOrReference.opt")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.AttributeGroupReference's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.AttributeGroupReference.this")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.LocalElementDeclaration's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.LocalElementDeclaration.this")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.DefinitionArc's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.DefinitionArc.this")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.LocalAttributeDeclaration's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.LocalAttributeDeclaration.this")
* method build(eu.cdevreeze.yaidom.queryapi.BackingElemApi)eu.cdevreeze.tqa.base.dom.XsdSchema in object eu.cdevreeze.tqa.base.dom.XsdSchema's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem)eu.cdevreeze.tqa.base.dom.XsdSchema instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi)eu.cdevreeze.tqa.base.dom.XsdSchema
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.XsdSchema.build")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.LinkbaseRef's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.LinkbaseRef.this")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.AnonymousComplexTypeDefinition's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.AnonymousComplexTypeDefinition.this")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.Extension's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.Extension.this")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.OtherXsdElem's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.OtherXsdElem.this")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.AnonymousSimpleTypeDefinition's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.AnonymousSimpleTypeDefinition.this")
* method opt(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)scala.Option in object eu.cdevreeze.tqa.base.dom.ComplexTypeDefinition's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)scala.Option instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)scala.Option
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.ComplexTypeDefinition.opt")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.base.dom.ConceptDeclaration has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.base.dom.ConceptDeclaration.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.ModelGroupReference's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.ModelGroupReference.this")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.OtherNonXLinkElem's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.OtherNonXLinkElem.this")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.SequenceModelGroup's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.SequenceModelGroup.this")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.ConceptLabelResource's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.ConceptLabelResource.this")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.Annotation's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.Annotation.this")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.AttributeGroupDefinition's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.AttributeGroupDefinition.this")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.Definition's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.Definition.this")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.LabelArc's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.LabelArc.this")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.SchemaRef's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.SchemaRef.this")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.RoleType's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.RoleType.this")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.base.dom.TaxonomyElem has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.base.dom.TaxonomyElem.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.TaxonomyElem's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.TaxonomyElem.this")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.CalculationArc's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.CalculationArc.this")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.GlobalElementDeclaration's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.GlobalElementDeclaration.this")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.NamedSimpleTypeDefinition's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.NamedSimpleTypeDefinition.this")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.Linkbase's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.Linkbase.this")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.Restriction's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.Restriction.this")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.StandardExtendedLink's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.StandardExtendedLink.this")
* method build(eu.cdevreeze.yaidom.queryapi.BackingElemApi)eu.cdevreeze.tqa.base.dom.Linkbase in object eu.cdevreeze.tqa.base.dom.Linkbase's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem)eu.cdevreeze.tqa.base.dom.Linkbase instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi)eu.cdevreeze.tqa.base.dom.Linkbase
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.Linkbase.build")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.CalculationLink's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.CalculationLink.this")
* method apply(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.base.dom.XsdElem in object eu.cdevreeze.tqa.base.dom.XsdElem's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.base.dom.XsdElem instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.base.dom.XsdElem
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.XsdElem.apply")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.LabelLink's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.LabelLink.this")
* method opt(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)scala.Option in object eu.cdevreeze.tqa.base.dom.ElementDeclarationOrReference's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)scala.Option instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)scala.Option
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.ElementDeclarationOrReference.opt")
* method underlyingParentElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.base.dom.NonStandardLocator has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.base.dom.NonStandardLocator.underlyingParentElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.NonStandardLocator's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.NonStandardLocator.this")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.NonStandardSimpleLink's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.NonStandardSimpleLink.this")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.AllModelGroup's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.AllModelGroup.this")
* method underlyingParentElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.base.dom.StandardResource has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.base.dom.StandardResource.underlyingParentElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.StandardResource's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.StandardResource.this")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.ChoiceModelGroup's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.ChoiceModelGroup.this")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.Import's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.Import.this")
* method underlyingParentElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.base.dom.StandardArc has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.base.dom.StandardArc.underlyingParentElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.StandardArc's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.StandardArc.this")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.ArcroleType's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.ArcroleType.this")
* method apply(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.base.dom.TaxonomyElem in object eu.cdevreeze.tqa.base.dom.TaxonomyElem's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.base.dom.TaxonomyElem instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.base.dom.TaxonomyElem
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.TaxonomyElem.apply")
* method build(eu.cdevreeze.yaidom.queryapi.BackingElemApi)eu.cdevreeze.tqa.base.dom.TaxonomyElem in object eu.cdevreeze.tqa.base.dom.TaxonomyElem's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem)eu.cdevreeze.tqa.base.dom.TaxonomyElem instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi)eu.cdevreeze.tqa.base.dom.TaxonomyElem
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.TaxonomyElem.build")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.AttributeReference's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.AttributeReference.this")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.NonStandardExtendedLink's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.NonStandardExtendedLink.this")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.PresentationArc's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.PresentationArc.this")
* method opt(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)scala.Option in object eu.cdevreeze.tqa.base.dom.ModelGroupDefinitionOrReference's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)scala.Option instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)scala.Option
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.ModelGroupDefinitionOrReference.opt")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.ConceptReferenceResource's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.ConceptReferenceResource.this")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.ModelGroupDefinition's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.ModelGroupDefinition.this")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.ArcroleRef's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.ArcroleRef.this")
* method underlyingParentElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in interface eu.cdevreeze.tqa.base.dom.ChildXLink has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.base.dom.ChildXLink.underlyingParentElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.OtherLinkElem's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.OtherLinkElem.this")
* method apply(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.base.dom.LinkElem in object eu.cdevreeze.tqa.base.dom.LinkElem's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.base.dom.LinkElem instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.base.dom.LinkElem
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.LinkElem.apply")
* method opt(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)scala.Option in object eu.cdevreeze.tqa.base.dom.SimpleTypeDefinition's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)scala.Option instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)scala.Option
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.SimpleTypeDefinition.opt")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.DefinitionLink's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.DefinitionLink.this")
* method filterDocumentUris(scala.collection.immutable.Set)eu.cdevreeze.tqa.base.dom.TaxonomyBase in class eu.cdevreeze.tqa.base.dom.TaxonomyBase does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.base.dom.TaxonomyBase.filterDocumentUris")
* method underlyingParentElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.base.dom.NonStandardResource has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.base.dom.NonStandardResource.underlyingParentElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.NonStandardResource's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.NonStandardResource.this")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.XsdSchema's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.XsdSchema.this")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.ComplexContent's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.ComplexContent.this")
* method underlyingParentElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.base.dom.NonStandardArc has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.base.dom.NonStandardArc.underlyingParentElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.NonStandardArc's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.NonStandardArc.this")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.NamedComplexTypeDefinition's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.NamedComplexTypeDefinition.this")
* method opt(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)scala.Option in object eu.cdevreeze.tqa.base.dom.AttributeGroupDefinitionOrReference's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)scala.Option instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)scala.Option
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.AttributeGroupDefinitionOrReference.opt")
* method withXmlDeclarationOption(scala.Option)eu.cdevreeze.tqa.base.dom.TaxonomyDocument in class eu.cdevreeze.tqa.base.dom.TaxonomyDocument does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.base.dom.TaxonomyDocument.withXmlDeclarationOption")
* method this(scala.Option,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.TaxonomyDocument's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingDocumentApi,eu.cdevreeze.tqa.base.dom.TaxonomyElem)Unit instead of (scala.Option,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.TaxonomyDocument.this")
* method apply(scala.Option,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.base.dom.TaxonomyDocument in object eu.cdevreeze.tqa.base.dom.TaxonomyDocument does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.base.dom.TaxonomyDocument.apply")
* method apply(scala.Option,eu.cdevreeze.tqa.base.dom.TaxonomyElem)eu.cdevreeze.tqa.base.dom.TaxonomyDocument in object eu.cdevreeze.tqa.base.dom.TaxonomyDocument does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.base.dom.TaxonomyDocument.apply")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.PresentationLink's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.PresentationLink.this")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.Include's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.Include.this")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.GlobalAttributeDeclaration's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.GlobalAttributeDeclaration.this")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.ReferenceArc's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.ReferenceArc.this")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.RoleRef's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.RoleRef.this")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.ElementReference's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.ElementReference.this")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.SimpleContent's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.SimpleContent.this")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.base.dom.UsedOn's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.dom.UsedOn.this")
* object eu.cdevreeze.tqa.backingelem.nodeinfo.SaxonNode does not have a correspondent in current version
  filter with: ProblemFilters.exclude[MissingClassProblem]("eu.cdevreeze.tqa.backingelem.nodeinfo.SaxonNode$")
* class eu.cdevreeze.tqa.backingelem.nodeinfo.SaxonText does not have a correspondent in current version
  filter with: ProblemFilters.exclude[MissingClassProblem]("eu.cdevreeze.tqa.backingelem.nodeinfo.SaxonText")
* object eu.cdevreeze.tqa.backingelem.nodeinfo.SaxonDocument does not have a correspondent in current version
  filter with: ProblemFilters.exclude[MissingClassProblem]("eu.cdevreeze.tqa.backingelem.nodeinfo.SaxonDocument$")
* class eu.cdevreeze.tqa.backingelem.nodeinfo.SaxonDocument does not have a correspondent in current version
  filter with: ProblemFilters.exclude[MissingClassProblem]("eu.cdevreeze.tqa.backingelem.nodeinfo.SaxonDocument")
* object eu.cdevreeze.tqa.backingelem.nodeinfo.YaidomSaxonToSimpleElemConverter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[MissingClassProblem]("eu.cdevreeze.tqa.backingelem.nodeinfo.YaidomSaxonToSimpleElemConverter$")
* class eu.cdevreeze.tqa.backingelem.nodeinfo.SaxonNode does not have a correspondent in current version
  filter with: ProblemFilters.exclude[MissingClassProblem]("eu.cdevreeze.tqa.backingelem.nodeinfo.SaxonNode")
* class eu.cdevreeze.tqa.backingelem.nodeinfo.YaidomSaxonToSimpleElemConverter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[MissingClassProblem]("eu.cdevreeze.tqa.backingelem.nodeinfo.YaidomSaxonToSimpleElemConverter")
* object eu.cdevreeze.tqa.backingelem.nodeinfo.package does not have a correspondent in current version
  filter with: ProblemFilters.exclude[MissingClassProblem]("eu.cdevreeze.tqa.backingelem.nodeinfo.package$")
* class eu.cdevreeze.tqa.backingelem.nodeinfo.SaxonComment does not have a correspondent in current version
  filter with: ProblemFilters.exclude[MissingClassProblem]("eu.cdevreeze.tqa.backingelem.nodeinfo.SaxonComment")
* class eu.cdevreeze.tqa.backingelem.nodeinfo.SaxonElem does not have a correspondent in current version
  filter with: ProblemFilters.exclude[MissingClassProblem]("eu.cdevreeze.tqa.backingelem.nodeinfo.SaxonElem")
* class eu.cdevreeze.tqa.backingelem.nodeinfo.SaxonProcessingInstruction does not have a correspondent in current version
  filter with: ProblemFilters.exclude[MissingClassProblem]("eu.cdevreeze.tqa.backingelem.nodeinfo.SaxonProcessingInstruction")
* class eu.cdevreeze.tqa.backingelem.nodeinfo.package does not have a correspondent in current version
  filter with: ProblemFilters.exclude[MissingClassProblem]("eu.cdevreeze.tqa.backingelem.nodeinfo.package")
* class eu.cdevreeze.tqa.backingelem.nodeinfo.YaidomSimpleToSaxonElemConverter does not have a correspondent in current version
  filter with: ProblemFilters.exclude[MissingClassProblem]("eu.cdevreeze.tqa.backingelem.nodeinfo.YaidomSimpleToSaxonElemConverter")
* interface eu.cdevreeze.tqa.backingelem.nodeinfo.SaxonCanBeDocumentChild does not have a correspondent in current version
  filter with: ProblemFilters.exclude[MissingClassProblem]("eu.cdevreeze.tqa.backingelem.nodeinfo.SaxonCanBeDocumentChild")
* class eu.cdevreeze.tqa.backingelem.nodeinfo.docbuilder.SaxonDocumentBuilder does not have a correspondent in current version
  filter with: ProblemFilters.exclude[MissingClassProblem]("eu.cdevreeze.tqa.backingelem.nodeinfo.docbuilder.SaxonDocumentBuilder")
* object eu.cdevreeze.tqa.backingelem.nodeinfo.docbuilder.SaxonDocumentBuilder does not have a correspondent in current version
  filter with: ProblemFilters.exclude[MissingClassProblem]("eu.cdevreeze.tqa.backingelem.nodeinfo.docbuilder.SaxonDocumentBuilder$")
* object eu.cdevreeze.tqa.backingelem.nodeinfo.docbuilder.package does not have a correspondent in current version
  filter with: ProblemFilters.exclude[MissingClassProblem]("eu.cdevreeze.tqa.backingelem.nodeinfo.docbuilder.package$")
* class eu.cdevreeze.tqa.backingelem.nodeinfo.docbuilder.package does not have a correspondent in current version
  filter with: ProblemFilters.exclude[MissingClassProblem]("eu.cdevreeze.tqa.backingelem.nodeinfo.docbuilder.package")
* object eu.cdevreeze.tqa.backingelem.indexed.docbuilder.IndexedDocumentBuilder does not have a correspondent in current version
  filter with: ProblemFilters.exclude[MissingClassProblem]("eu.cdevreeze.tqa.backingelem.indexed.docbuilder.IndexedDocumentBuilder$")
* object eu.cdevreeze.tqa.backingelem.indexed.docbuilder.package does not have a correspondent in current version
  filter with: ProblemFilters.exclude[MissingClassProblem]("eu.cdevreeze.tqa.backingelem.indexed.docbuilder.package$")
* class eu.cdevreeze.tqa.backingelem.indexed.docbuilder.package does not have a correspondent in current version
  filter with: ProblemFilters.exclude[MissingClassProblem]("eu.cdevreeze.tqa.backingelem.indexed.docbuilder.package")
* class eu.cdevreeze.tqa.backingelem.indexed.docbuilder.IndexedDocumentBuilder does not have a correspondent in current version
  filter with: ProblemFilters.exclude[MissingClassProblem]("eu.cdevreeze.tqa.backingelem.indexed.docbuilder.IndexedDocumentBuilder")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.instancevalidation.TypedDimensionMember has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.instancevalidation.TypedDimensionMember.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi)Unit in class eu.cdevreeze.tqa.instancevalidation.TypedDimensionMember's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.instancevalidation.TypedDimensionMember.this")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.instance.Identifier has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.instance.Identifier.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.instance.Identifier's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.instance.Identifier.this")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.instance.Segment has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.instance.Segment.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.instance.Segment's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.instance.Segment.this")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.instance.SchemaRef has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.instance.SchemaRef.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.instance.SchemaRef's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.instance.SchemaRef.this")
* method accepts(eu.cdevreeze.yaidom.queryapi.BackingElemApi)Boolean in object eu.cdevreeze.tqa.instance.Period's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem)Boolean instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi)Boolean
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.instance.Period.accepts")
* method apply(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.instance.Period in object eu.cdevreeze.tqa.instance.Period's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.instance.Period instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.instance.Period
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.instance.Period.apply")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.instance.InstantPeriod has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.instance.InstantPeriod.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.instance.InstantPeriod's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.instance.InstantPeriod.this")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.instance.ArcroleRef has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.instance.ArcroleRef.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.instance.ArcroleRef's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.instance.ArcroleRef.this")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.instance.NonNumericItemFact has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.instance.NonNumericItemFact.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.instance.NonNumericItemFact's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.instance.NonNumericItemFact.this")
* method withXmlDeclarationOption(scala.Option)eu.cdevreeze.tqa.instance.XbrlInstanceDocument in class eu.cdevreeze.tqa.instance.XbrlInstanceDocument does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.instance.XbrlInstanceDocument.withXmlDeclarationOption")
* method this(scala.Option,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.instance.XbrlInstanceDocument's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingDocumentApi,eu.cdevreeze.tqa.instance.XbrlInstance)Unit instead of (scala.Option,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.instance.XbrlInstanceDocument.this")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.instance.Scenario has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.instance.Scenario.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.instance.Scenario's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.instance.Scenario.this")
* method underlyingParentElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.instance.StandardLoc has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.instance.StandardLoc.underlyingParentElem")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.instance.StandardLoc has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.instance.StandardLoc.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.instance.StandardLoc's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.instance.StandardLoc.this")
* method accepts(eu.cdevreeze.yaidom.queryapi.BackingElemApi)Boolean in object eu.cdevreeze.tqa.instance.Fact's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem)Boolean instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi)Boolean
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.instance.Fact.accepts")
* method apply(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.instance.Fact in object eu.cdevreeze.tqa.instance.Fact's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.instance.Fact instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.instance.Fact
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.instance.Fact.apply")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.instance.Fact has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.instance.Fact.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.instance.Fact's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.instance.Fact.this")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.instance.TypedMember has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.instance.TypedMember.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.instance.TypedMember's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.instance.TypedMember.this")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.instance.StartDate has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.instance.StartDate.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.instance.StartDate's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.instance.StartDate.this")
* method applyForLinkNamespace(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.instance.XbrliElem in object eu.cdevreeze.tqa.instance.XbrliElem's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.instance.XbrliElem instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.instance.XbrliElem
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.instance.XbrliElem.applyForLinkNamespace")
* method applyForOtherNamespace(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.instance.XbrliElem in object eu.cdevreeze.tqa.instance.XbrliElem's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.instance.XbrliElem instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.instance.XbrliElem
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.instance.XbrliElem.applyForOtherNamespace")
* method applyForXbrldiNamespace(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.instance.XbrliElem in object eu.cdevreeze.tqa.instance.XbrliElem's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.instance.XbrliElem instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.instance.XbrliElem
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.instance.XbrliElem.applyForXbrldiNamespace")
* method apply(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.instance.XbrliElem in object eu.cdevreeze.tqa.instance.XbrliElem's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.instance.XbrliElem instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.instance.XbrliElem
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.instance.XbrliElem.apply")
* method build(eu.cdevreeze.yaidom.queryapi.BackingElemApi)eu.cdevreeze.tqa.instance.XbrliElem in object eu.cdevreeze.tqa.instance.XbrliElem's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem)eu.cdevreeze.tqa.instance.XbrliElem instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi)eu.cdevreeze.tqa.instance.XbrliElem
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.instance.XbrliElem.build")
* method applyForXbrliNamespace(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.instance.XbrliElem in object eu.cdevreeze.tqa.instance.XbrliElem's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.instance.XbrliElem instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.instance.XbrliElem
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.instance.XbrliElem.applyForXbrliNamespace")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.instance.FootnoteLink has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.instance.FootnoteLink.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.instance.FootnoteLink's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.instance.FootnoteLink.this")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.instance.LinkbaseRef has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.instance.LinkbaseRef.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.instance.LinkbaseRef's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.instance.LinkbaseRef.this")
* method underlyingParentElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.instance.FootnoteArc has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.instance.FootnoteArc.underlyingParentElem")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.instance.FootnoteArc has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.instance.FootnoteArc.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.instance.FootnoteArc's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.instance.FootnoteArc.this")
* method apply(scala.Option,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.instance.XbrlInstanceDocument in object eu.cdevreeze.tqa.instance.XbrlInstanceDocument does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.instance.XbrlInstanceDocument.apply")
* method apply(scala.Option,eu.cdevreeze.tqa.instance.XbrlInstance)eu.cdevreeze.tqa.instance.XbrlInstanceDocument in object eu.cdevreeze.tqa.instance.XbrlInstanceDocument does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.instance.XbrlInstanceDocument.apply")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.instance.ItemFact has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.instance.ItemFact.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.instance.ItemFact's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.instance.ItemFact.this")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.instance.NilNumericItemFact has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.instance.NilNumericItemFact.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.instance.NilNumericItemFact's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.instance.NilNumericItemFact.this")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.instance.StartEndDatePeriod has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.instance.StartEndDatePeriod.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.instance.StartEndDatePeriod's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.instance.StartEndDatePeriod.this")
* method underlyingParentElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in interface eu.cdevreeze.tqa.instance.ChildXLink has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.instance.ChildXLink.underlyingParentElem")
* method build(eu.cdevreeze.yaidom.queryapi.BackingElemApi)eu.cdevreeze.tqa.instance.XbrlInstance in object eu.cdevreeze.tqa.instance.XbrlInstance's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem)eu.cdevreeze.tqa.instance.XbrlInstance instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi)eu.cdevreeze.tqa.instance.XbrlInstance
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.instance.XbrlInstance.build")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.instance.XbrliContext has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.instance.XbrliContext.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.instance.XbrliContext's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.instance.XbrliContext.this")
* method underlyingParentElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.instance.Footnote has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.instance.Footnote.underlyingParentElem")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.instance.Footnote has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.instance.Footnote.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.instance.Footnote's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.instance.Footnote.this")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.instance.Period has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.instance.Period.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.instance.Period's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.instance.Period.this")
* method accepts(eu.cdevreeze.yaidom.queryapi.BackingElemApi)Boolean in object eu.cdevreeze.tqa.instance.TupleFact's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem)Boolean instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi)Boolean
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.instance.TupleFact.accepts")
* method apply(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.instance.TupleFact in object eu.cdevreeze.tqa.instance.TupleFact's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.instance.TupleFact instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.instance.TupleFact
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.instance.TupleFact.apply")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.instance.XbrlInstance has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.instance.XbrlInstance.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.instance.XbrlInstance's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.instance.XbrlInstance.this")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.instance.Forever has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.instance.Forever.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.instance.Forever's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.instance.Forever.this")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.instance.Divide has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.instance.Divide.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.instance.Divide's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.instance.Divide.this")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.instance.Entity has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.instance.Entity.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.instance.Entity's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.instance.Entity.this")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.instance.NumericItemFact has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.instance.NumericItemFact.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.instance.NumericItemFact's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.instance.NumericItemFact.this")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.instance.NonNilNonFractionNumericItemFact has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.instance.NonNilNonFractionNumericItemFact.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.instance.NonNilNonFractionNumericItemFact's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.instance.NonNilNonFractionNumericItemFact.this")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.instance.ExplicitMember has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.instance.ExplicitMember.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.instance.ExplicitMember's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.instance.ExplicitMember.this")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.instance.RoleRef has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.instance.RoleRef.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.instance.RoleRef's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.instance.RoleRef.this")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.instance.ForeverPeriod has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.instance.ForeverPeriod.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.instance.ForeverPeriod's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.instance.ForeverPeriod.this")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.instance.XbrliElem has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.instance.XbrliElem.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.instance.XbrliElem's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.instance.XbrliElem.this")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.instance.TupleFact has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.instance.TupleFact.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.instance.TupleFact's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.instance.TupleFact.this")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.instance.NonNilFractionItemFact has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.instance.NonNilFractionItemFact.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.instance.NonNilFractionItemFact's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.instance.NonNilFractionItemFact.this")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.instance.Instant has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.instance.Instant.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.instance.Instant's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.instance.Instant.this")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.instance.XbrliUnit has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.instance.XbrliUnit.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.instance.XbrliUnit's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.instance.XbrliUnit.this")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.instance.EndDate has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.instance.EndDate.backingElem")
* method this(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit in class eu.cdevreeze.tqa.instance.EndDate's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)Unit instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)Unit
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.instance.EndDate.this")
* method accepts(eu.cdevreeze.yaidom.queryapi.BackingElemApi)Boolean in object eu.cdevreeze.tqa.instance.ItemFact's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem)Boolean instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi)Boolean
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.instance.ItemFact.accepts")
* method apply(eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.instance.ItemFact in object eu.cdevreeze.tqa.instance.ItemFact's type is different in current version, where it is (eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.instance.ItemFact instead of (eu.cdevreeze.yaidom.queryapi.BackingElemApi,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.instance.ItemFact
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.instance.ItemFact.apply")
* object eu.cdevreeze.tqa.xpath.package does not have a correspondent in current version
  filter with: ProblemFilters.exclude[MissingClassProblem]("eu.cdevreeze.tqa.xpath.package$")
* object eu.cdevreeze.tqa.xpath.XPathEvaluatorFactory does not have a correspondent in current version
  filter with: ProblemFilters.exclude[MissingClassProblem]("eu.cdevreeze.tqa.xpath.XPathEvaluatorFactory$")
* interface eu.cdevreeze.tqa.xpath.XPathEvaluator does not have a correspondent in current version
  filter with: ProblemFilters.exclude[MissingClassProblem]("eu.cdevreeze.tqa.xpath.XPathEvaluator")
* object eu.cdevreeze.tqa.xpath.XPathEvaluator does not have a correspondent in current version
  filter with: ProblemFilters.exclude[MissingClassProblem]("eu.cdevreeze.tqa.xpath.XPathEvaluator$")
* class eu.cdevreeze.tqa.xpath.package does not have a correspondent in current version
  filter with: ProblemFilters.exclude[MissingClassProblem]("eu.cdevreeze.tqa.xpath.package")
* interface eu.cdevreeze.tqa.xpath.XPathEvaluatorFactory does not have a correspondent in current version
  filter with: ProblemFilters.exclude[MissingClassProblem]("eu.cdevreeze.tqa.xpath.XPathEvaluatorFactory")
* object eu.cdevreeze.tqa.xpath.jaxp.saxon.JaxpXPathEvaluatorUsingSaxon does not have a correspondent in current version
  filter with: ProblemFilters.exclude[MissingClassProblem]("eu.cdevreeze.tqa.xpath.jaxp.saxon.JaxpXPathEvaluatorUsingSaxon$")
* class eu.cdevreeze.tqa.xpath.jaxp.saxon.JaxpXPathEvaluatorUsingSaxon does not have a correspondent in current version
  filter with: ProblemFilters.exclude[MissingClassProblem]("eu.cdevreeze.tqa.xpath.jaxp.saxon.JaxpXPathEvaluatorUsingSaxon")
* class eu.cdevreeze.tqa.xpath.jaxp.saxon.SimpleUriResolver does not have a correspondent in current version
  filter with: ProblemFilters.exclude[MissingClassProblem]("eu.cdevreeze.tqa.xpath.jaxp.saxon.SimpleUriResolver")
* class eu.cdevreeze.tqa.xpath.jaxp.saxon.JaxpXPathEvaluatorFactoryUsingSaxon does not have a correspondent in current version
  filter with: ProblemFilters.exclude[MissingClassProblem]("eu.cdevreeze.tqa.xpath.jaxp.saxon.JaxpXPathEvaluatorFactoryUsingSaxon")
* object eu.cdevreeze.tqa.xpath.jaxp.saxon.JaxpXPathEvaluatorFactoryUsingSaxon does not have a correspondent in current version
  filter with: ProblemFilters.exclude[MissingClassProblem]("eu.cdevreeze.tqa.xpath.jaxp.saxon.JaxpXPathEvaluatorFactoryUsingSaxon$")
* method underlyingParentElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in interface eu.cdevreeze.tqa.extension.formula.dom.FormulaResource has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.dom.FormulaResource.underlyingParentElem")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in interface eu.cdevreeze.tqa.extension.formula.dom.FormulaResource has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.dom.FormulaResource.backingElem")
* method underlyingParentElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.extension.formula.dom.Severity has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.dom.Severity.underlyingParentElem")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.extension.formula.dom.Severity has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.dom.Severity.backingElem")
* method underlyingParentElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.extension.formula.dom.EqualityDefinition has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.dom.EqualityDefinition.underlyingParentElem")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.extension.formula.dom.EqualityDefinition has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.dom.EqualityDefinition.backingElem")
* method underlyingParentElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in interface eu.cdevreeze.tqa.extension.formula.dom.FormulaArc has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.dom.FormulaArc.underlyingParentElem")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in interface eu.cdevreeze.tqa.extension.formula.dom.FormulaArc has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.dom.FormulaArc.backingElem")
* method underlyingParentElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.extension.formula.dom.VariableFilterArc has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.dom.VariableFilterArc.underlyingParentElem")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.extension.formula.dom.VariableFilterArc has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.dom.VariableFilterArc.backingElem")
* method underlyingParentElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.extension.formula.dom.OtherFormulaArc has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.dom.OtherFormulaArc.underlyingParentElem")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.extension.formula.dom.OtherFormulaArc has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.dom.OtherFormulaArc.backingElem")
* method underlyingParentElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.extension.formula.dom.Message has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.dom.Message.underlyingParentElem")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.extension.formula.dom.Message has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.dom.Message.backingElem")
* method underlyingParentElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.extension.formula.dom.Precondition has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.dom.Precondition.underlyingParentElem")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.extension.formula.dom.Precondition has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.dom.Precondition.backingElem")
* method underlyingParentElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.extension.formula.dom.FunctionImplementation has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.dom.FunctionImplementation.underlyingParentElem")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.extension.formula.dom.FunctionImplementation has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.dom.FunctionImplementation.backingElem")
* method underlyingParentElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.extension.formula.dom.Function has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.dom.Function.underlyingParentElem")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.extension.formula.dom.Function has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.dom.Function.backingElem")
* method underlyingParentElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.extension.formula.dom.VariableArc has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.dom.VariableArc.underlyingParentElem")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.extension.formula.dom.VariableArc has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.dom.VariableArc.backingElem")
* method underlyingParentElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.extension.formula.dom.AssertionSet has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.dom.AssertionSet.underlyingParentElem")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.extension.formula.dom.AssertionSet has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.dom.AssertionSet.backingElem")
* method underlyingParentElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.extension.formula.dom.VariableOrParameter has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.dom.VariableOrParameter.underlyingParentElem")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.extension.formula.dom.VariableOrParameter has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.dom.VariableOrParameter.backingElem")
* method underlyingParentElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.extension.formula.dom.ConsistencyAssertion has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.dom.ConsistencyAssertion.underlyingParentElem")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.extension.formula.dom.ConsistencyAssertion has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.dom.ConsistencyAssertion.backingElem")
* method underlyingParentElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.extension.formula.dom.Filter has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.dom.Filter.underlyingParentElem")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.extension.formula.dom.Filter has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.dom.Filter.backingElem")
* method underlyingParentElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.extension.formula.dom.VariableSet has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.dom.VariableSet.underlyingParentElem")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.extension.formula.dom.VariableSet has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.dom.VariableSet.backingElem")
* method underlyingParentElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.extension.formula.dom.VariableSetFilterArc has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.dom.VariableSetFilterArc.underlyingParentElem")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.extension.formula.dom.VariableSetFilterArc has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.formula.dom.VariableSetFilterArc.backingElem")
* method underlyingParentElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.extension.table.dom.TableParameterArc has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.table.dom.TableParameterArc.underlyingParentElem")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.extension.table.dom.TableParameterArc has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.table.dom.TableParameterArc.backingElem")
* method underlyingParentElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.extension.table.dom.TableFilterArc has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.table.dom.TableFilterArc.underlyingParentElem")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.extension.table.dom.TableFilterArc has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.table.dom.TableFilterArc.backingElem")
* method underlyingParentElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.extension.table.dom.TableBreakdown has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.table.dom.TableBreakdown.underlyingParentElem")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.extension.table.dom.TableBreakdown has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.table.dom.TableBreakdown.backingElem")
* method underlyingParentElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in interface eu.cdevreeze.tqa.extension.table.dom.TableResource has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.table.dom.TableResource.underlyingParentElem")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in interface eu.cdevreeze.tqa.extension.table.dom.TableResource has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.table.dom.TableResource.backingElem")
* method underlyingParentElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.extension.table.dom.AspectNodeFilterArc has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.table.dom.AspectNodeFilterArc.underlyingParentElem")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.extension.table.dom.AspectNodeFilterArc has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.table.dom.AspectNodeFilterArc.backingElem")
* method underlyingParentElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in interface eu.cdevreeze.tqa.extension.table.dom.TableArc has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.table.dom.TableArc.underlyingParentElem")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in interface eu.cdevreeze.tqa.extension.table.dom.TableArc has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.table.dom.TableArc.backingElem")
* method underlyingParentElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.extension.table.dom.DefinitionNodeSubtreeArc has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.table.dom.DefinitionNodeSubtreeArc.underlyingParentElem")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.extension.table.dom.DefinitionNodeSubtreeArc has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.table.dom.DefinitionNodeSubtreeArc.backingElem")
* method underlyingParentElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.extension.table.dom.Table has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.table.dom.Table.underlyingParentElem")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.extension.table.dom.Table has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.table.dom.Table.backingElem")
* method underlyingParentElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.extension.table.dom.DefinitionNode has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.table.dom.DefinitionNode.underlyingParentElem")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.extension.table.dom.DefinitionNode has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.table.dom.DefinitionNode.backingElem")
* method underlyingParentElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.extension.table.dom.TableBreakdownArc has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.table.dom.TableBreakdownArc.underlyingParentElem")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.extension.table.dom.TableBreakdownArc has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.table.dom.TableBreakdownArc.backingElem")
* method underlyingParentElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.extension.table.dom.BreakdownTreeArc has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.table.dom.BreakdownTreeArc.underlyingParentElem")
* method backingElem()eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.extension.table.dom.BreakdownTreeArc has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingNodes#Elem rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.extension.table.dom.BreakdownTreeArc.backingElem")


0.8.0-M2
========

The main changes in this version are:

* Dependency on yaidom 1.8.0-M4
* Therefore leveraging the new yaidom ``BackingNodes.Elem`` query API trait
* Improved the taxonomy query API, by adding some "missing" methods and query API traits (for element label querying etc.)
* Started (experimental) work on programmatic taxonomy creation


0.8.0-M1
========

The main changes in this version are:

* Dependency on yaidom 1.8.0-M3
* The yaidom Saxon wrapper elements have been removed, and now live in yaidom 1.8.0-M3
* The XPath evaluation support (API and implementation for Saxon) has been removed, and now lives in yaidom 1.8.0-M3 (and has been improved)
* Class ``TaxonomyDocument`` now holds both the ``TaxonomyElem`` document element and the entire backing document
* Moved indexed document and Saxon document builders to another package

MiMa reports too many breaking changes to list them. They are mostly of a trivial nature, but it does mean
that upgrading TQA in client code from 0.7.1 to 0.8.0-M1 does require some work on the part of the programmer.


0.7.1
=====

This version is almost the same as version 0.7.0.

This version added the following to the previous version:

* More query methods for querying dimensional tree inheritance, filtering on has-hypercube relationships
* Constants for standard label and reference roles

Breaking changes (in SBT, run: tqaJVM/*:mimaReportBinaryIssues):

* abstract method computeFilteredHasHypercubeInheritanceOrSelf(scala.Function1)scala.collection.immutable.Map in interface eu.cdevreeze.tqa.base.queryapi.DimensionalRelationshipContainerApi is present only in current version
  filter with: ProblemFilters.exclude[ReversedMissingMethodProblem]("eu.cdevreeze.tqa.base.queryapi.DimensionalRelationshipContainerApi.computeFilteredHasHypercubeInheritanceOrSelf")
* abstract method computeHasHypercubeInheritanceForElr(java.lang.String)scala.collection.immutable.Map in interface eu.cdevreeze.tqa.base.queryapi.DimensionalRelationshipContainerApi is present only in current version
  filter with: ProblemFilters.exclude[ReversedMissingMethodProblem]("eu.cdevreeze.tqa.base.queryapi.DimensionalRelationshipContainerApi.computeHasHypercubeInheritanceForElr")
* abstract method computeHasHypercubeInheritanceForElrReturningPrimaries(java.lang.String)scala.collection.immutable.Map in interface eu.cdevreeze.tqa.base.queryapi.DimensionalRelationshipContainerApi is present only in current version
  filter with: ProblemFilters.exclude[ReversedMissingMethodProblem]("eu.cdevreeze.tqa.base.queryapi.DimensionalRelationshipContainerApi.computeHasHypercubeInheritanceForElrReturningPrimaries")
* abstract method computeHasHypercubeInheritanceOrSelfForElrReturningPrimaries(java.lang.String)scala.collection.immutable.Map in interface eu.cdevreeze.tqa.base.queryapi.DimensionalRelationshipContainerApi is present only in current version
  filter with: ProblemFilters.exclude[ReversedMissingMethodProblem]("eu.cdevreeze.tqa.base.queryapi.DimensionalRelationshipContainerApi.computeHasHypercubeInheritanceOrSelfForElrReturningPrimaries")
* abstract method computeHasHypercubeInheritanceOrSelfForElr(java.lang.String)scala.collection.immutable.Map in interface eu.cdevreeze.tqa.base.queryapi.DimensionalRelationshipContainerApi is present only in current version
  filter with: ProblemFilters.exclude[ReversedMissingMethodProblem]("eu.cdevreeze.tqa.base.queryapi.DimensionalRelationshipContainerApi.computeHasHypercubeInheritanceOrSelfForElr")
* abstract method computeFilteredHasHypercubeInheritance(scala.Function1)scala.collection.immutable.Map in interface eu.cdevreeze.tqa.base.queryapi.DimensionalRelationshipContainerApi is present only in current version
  filter with: ProblemFilters.exclude[ReversedMissingMethodProblem]("eu.cdevreeze.tqa.base.queryapi.DimensionalRelationshipContainerApi.computeFilteredHasHypercubeInheritance")
* method apply(scala.Option,eu.cdevreeze.tqa.instance.XbrliElem)eu.cdevreeze.tqa.instance.XbrlInstanceDocument in object eu.cdevreeze.tqa.instance.XbrlInstanceDocument in current version does not have a correspondent with same parameter signature among (scala.Option,scala.collection.immutable.IndexedSeq)eu.cdevreeze.tqa.instance.XbrlInstanceDocument, (scala.Option,eu.cdevreeze.tqa.instance.XbrlInstance)eu.cdevreeze.tqa.instance.XbrlInstanceDocument
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.instance.XbrlInstanceDocument.apply")


0.7.0
=====

This version adds taxonomy documents and XBRL instance documents (that can contain top-level comments
and processing instructions besides the root element).

The major changes are:

* Using yaidom 1.7.1 ``BackingDocumentApi`` in API of ``SaxonDocument``
* Now ``DocumentBuilder`` returns documents instead of (root) elements
* Added ``TaxonomyDocument`` and using it in taxonomy classes (and document collectors) instead of (root) elements
* Added ``XbrlInstanceDocument``
* Renamed XbrlInstance ``apply`` method to ``build``

Breaking changes (in SBT, run: tqaJVM/*:mimaReportBinaryIssues):

* method build(java.net.URI)eu.cdevreeze.yaidom.queryapi.BackingElemApi in interface eu.cdevreeze.tqa.docbuilder.DocumentBuilder has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingDocumentApi rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.docbuilder.DocumentBuilder.build")
* abstract method build(java.net.URI)eu.cdevreeze.yaidom.queryapi.BackingDocumentApi in interface eu.cdevreeze.tqa.docbuilder.DocumentBuilder is present only in current version
  filter with: ProblemFilters.exclude[ReversedMissingMethodProblem]("eu.cdevreeze.tqa.docbuilder.DocumentBuilder.build")
* method build(java.net.URI)eu.cdevreeze.yaidom.queryapi.BackingElemApi in class eu.cdevreeze.tqa.docbuilder.jvm.CachingDocumentBuilder has a different result type in current version, where it is eu.cdevreeze.yaidom.queryapi.BackingDocumentApi rather than eu.cdevreeze.yaidom.queryapi.BackingElemApi
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.docbuilder.jvm.CachingDocumentBuilder.build")
* abstract method taxonomyDocs()scala.collection.immutable.IndexedSeq in interface eu.cdevreeze.tqa.base.queryapi.TaxonomyApi is present only in current version
  filter with: ProblemFilters.exclude[ReversedMissingMethodProblem]("eu.cdevreeze.tqa.base.queryapi.TaxonomyApi.taxonomyDocs")
* method collectTaxonomyRootElems(scala.collection.immutable.Set,eu.cdevreeze.tqa.docbuilder.DocumentBuilder)scala.collection.immutable.IndexedSeq in object eu.cdevreeze.tqa.base.taxonomybuilder.TrivialDocumentCollector does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.TrivialDocumentCollector.collectTaxonomyRootElems")
* abstract method collectTaxonomyRootElems(scala.collection.immutable.Set,eu.cdevreeze.tqa.docbuilder.DocumentBuilder)scala.collection.immutable.IndexedSeq in interface eu.cdevreeze.tqa.base.taxonomybuilder.DocumentCollector does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.DocumentCollector.collectTaxonomyRootElems")
* abstract method collectTaxonomyDocuments(scala.collection.immutable.Set,eu.cdevreeze.tqa.docbuilder.DocumentBuilder)scala.collection.immutable.IndexedSeq in interface eu.cdevreeze.tqa.base.taxonomybuilder.DocumentCollector is present only in current version
  filter with: ProblemFilters.exclude[ReversedMissingMethodProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.DocumentCollector.collectTaxonomyDocuments")
* method findAllUsedDocUris(eu.cdevreeze.tqa.base.dom.TaxonomyRootElem)scala.collection.immutable.Set in class eu.cdevreeze.tqa.base.taxonomybuilder.DefaultDtsCollector's type is different in current version, where it is (eu.cdevreeze.tqa.base.dom.TaxonomyDocument)scala.collection.immutable.Set instead of (eu.cdevreeze.tqa.base.dom.TaxonomyRootElem)scala.collection.immutable.Set
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.DefaultDtsCollector.findAllUsedDocUris")
* method collectTaxonomyRootElems(scala.collection.immutable.Set,eu.cdevreeze.tqa.docbuilder.DocumentBuilder)scala.collection.immutable.IndexedSeq in class eu.cdevreeze.tqa.base.taxonomybuilder.AbstractDtsCollector does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.AbstractDtsCollector.collectTaxonomyRootElems")
* method findAllUsedDocUris(eu.cdevreeze.tqa.base.dom.TaxonomyRootElem)scala.collection.immutable.Set in class eu.cdevreeze.tqa.base.taxonomybuilder.AbstractDtsCollector's type is different in current version, where it is (eu.cdevreeze.tqa.base.dom.TaxonomyDocument)scala.collection.immutable.Set instead of (eu.cdevreeze.tqa.base.dom.TaxonomyRootElem)scala.collection.immutable.Set
  filter with: ProblemFilters.exclude[IncompatibleMethTypeProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.AbstractDtsCollector.findAllUsedDocUris")
* abstract method findAllUsedDocUris(eu.cdevreeze.tqa.base.dom.TaxonomyDocument)scala.collection.immutable.Set in class eu.cdevreeze.tqa.base.taxonomybuilder.AbstractDtsCollector is present only in current version
  filter with: ProblemFilters.exclude[ReversedMissingMethodProblem]("eu.cdevreeze.tqa.base.taxonomybuilder.AbstractDtsCollector.findAllUsedDocUris")
* method build(java.net.URI)eu.cdevreeze.tqa.backingelem.nodeinfo.SaxonElem in class eu.cdevreeze.tqa.backingelem.nodeinfo.docbuilder.SaxonDocumentBuilder has a different result type in current version, where it is eu.cdevreeze.tqa.backingelem.nodeinfo.SaxonDocument rather than eu.cdevreeze.tqa.backingelem.nodeinfo.SaxonElem
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.backingelem.nodeinfo.docbuilder.SaxonDocumentBuilder.build")
* method build(java.net.URI)eu.cdevreeze.yaidom.indexed.IndexedScopedNode#Elem in class eu.cdevreeze.tqa.backingelem.indexed.docbuilder.IndexedDocumentBuilder has a different result type in current version, where it is eu.cdevreeze.yaidom.indexed.Document rather than eu.cdevreeze.yaidom.indexed.IndexedScopedNode#Elem
  filter with: ProblemFilters.exclude[IncompatibleResultTypeProblem]("eu.cdevreeze.tqa.backingelem.indexed.docbuilder.IndexedDocumentBuilder.build")
* method apply(eu.cdevreeze.yaidom.queryapi.BackingElemApi)eu.cdevreeze.tqa.instance.XbrliElem in object eu.cdevreeze.tqa.instance.XbrliElem does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.instance.XbrliElem.apply")
* method apply(eu.cdevreeze.yaidom.queryapi.BackingElemApi)eu.cdevreeze.tqa.instance.XbrlInstance in object eu.cdevreeze.tqa.instance.XbrlInstance does not have a correspondent in current version
  filter with: ProblemFilters.exclude[DirectMissingMethodProblem]("eu.cdevreeze.tqa.instance.XbrlInstance.apply")


0.6.1
=====

Exactly the same as release 0.6.0. At first, deployment of release 0.6.0 to Maven Central seemed unsuccessful,
hence release 0.6.1. Both releases are now in Maven Central, and both releases can be used. They are
interchangeable.


0.6.0
=====

Version 0.6.0 of TQA redesigned DocumentBuilders and TaxonomyBuilders. It also added an XBRL instance
viewer (created with Scala.js). The redesign of DocumentBuilders and TaxonomyBuilders causes some breaking changes.

The major changes are:

* Redesign of ``DocumentBuilder`` implementations on the JVM

  * Document builders targeting the JVM now take a URI resolver as extra argument, turning a URI into a SAX InputSource
  * Such a URI resolver can parse documents in ZIP files, like the ones in a Taxonomy Package
  * URI resolvers can also take a URI converter (function from URI to URI)
  * An XML catalog (as restricted by Taxonomy Packages) can be offered as such a URI converter
  * URI converters can be combined, and so can URI resolvers
  * A large portion of the test code now also uses ZIP files as test taxonomy document input

* Redesign of ``TaxonomyBuilder``, in particular of ``DocumentCollector``

  * Document collectors no longer contain the (entry point) document URIs as state
  * Instead, the taxonomy builder ``build`` method now takes the (entry point) document URIs
  * This makes taxonomy builders re-usable across entry points
  * Taxonomy package XML files can now be parsed, and provide entry point URIs via an entry point name, for example

Breaking changes (in SBT, run: tqaJVM/*:mimaReportBinaryIssues):

  * method uriToLocalUri(java.net.URI,java.io.File)java.net.URI in object eu.cdevreeze.tqa.docbuilder.jvm.UriConverters does not have a correspondent in current version
  * abstract method collectTaxonomyRootElems(eu.cdevreeze.tqa.docbuilder.DocumentBuilder)scala.collection.immutable.IndexedSeq in interface eu.cdevreeze.tqa.base.taxonomybuilder.DocumentCollector does not have a correspondent in current version
  * abstract method collectTaxonomyRootElems(scala.collection.immutable.Set,eu.cdevreeze.tqa.docbuilder.DocumentBuilder)scala.collection.immutable.IndexedSeq in interface eu.cdevreeze.tqa.base.taxonomybuilder.DocumentCollector is present only in current version
  * method withDocumentCollector(eu.cdevreeze.tqa.base.taxonomybuilder.DocumentCollector)eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyBuilder in class eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyBuilder does not have a correspondent in current version
  * method build()eu.cdevreeze.tqa.base.taxonomy.BasicTaxonomy in class eu.cdevreeze.tqa.base.taxonomybuilder.TaxonomyBuilder does not have a correspondent in current version
  * method this(scala.collection.immutable.Set)Unit in class eu.cdevreeze.tqa.base.taxonomybuilder.DefaultDtsCollector does not have a correspondent in current version
  * method apply(scala.collection.immutable.Set)eu.cdevreeze.tqa.base.taxonomybuilder.DefaultDtsCollector in object eu.cdevreeze.tqa.base.taxonomybuilder.DefaultDtsCollector does not have a correspondent in current version
  * method collectTaxonomyRootElems(eu.cdevreeze.tqa.docbuilder.DocumentBuilder)scala.collection.immutable.IndexedSeq in class eu.cdevreeze.tqa.base.taxonomybuilder.AbstractDtsCollector does not have a correspondent in current version
  * method entrypointUris()scala.collection.immutable.Set in class eu.cdevreeze.tqa.base.taxonomybuilder.AbstractDtsCollector does not have a correspondent in current version
  * method this(scala.collection.immutable.Set)Unit in class eu.cdevreeze.tqa.base.taxonomybuilder.AbstractDtsCollector does not have a correspondent in current version
  * method uriConverter()scala.Function1 in class eu.cdevreeze.tqa.backingelem.nodeinfo.docbuilder.SaxonDocumentBuilder does not have a correspondent in current version
  * method uriConverter()scala.Function1 in class eu.cdevreeze.tqa.backingelem.indexed.docbuilder.IndexedDocumentBuilder does not have a correspondent in current version


0.5.0
=====

Version 0.5.0 of TQA supports Scala.js as second target platform. The API should start to feel more stable than in
previous releases.


0.5.0-M2
========

This milestone release is a step towards release 0.5.0, which supports Scala.js and which should make the API more
stable than was the case for previous releases.

