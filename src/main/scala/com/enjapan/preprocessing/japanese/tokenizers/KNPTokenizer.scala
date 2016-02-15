package com.enjapan.preprocessing.japanese.tokenizers

import cats.data.Xor
import com.enjapan.knp.models.{BList, Bunsetsu, Predicate, Tag}
import com.enjapan.knp.{KNP, ParseException}
import com.enjapan.preprocessing.exceptions.{PredicateArgumentsSizeException, TagsException}

/**
  * Created by Ugo Bataillard on 2/4/16.
  * Parse and tokenize Japanese sentences to tokens
  */
class KNPTokenizer {

  val knp = new KNP()

  /**
    * Runs KNP on a sentence
    * @param sentence
    * @return
    */
  def runKnp(sentence: String): Xor[ParseException, BList] = {
    knp(sentence)
  }

  /**
    * Transforms output from KNP to a BList
    * @param knpLines
    * @return
    */
  def parseKnp(knpLines: Iterable[String]): Xor[ParseException, BList] = {
    knp.parser.parse(knpLines)
  }

  /**
    * Tokenize a sentence by only taking its predicates
    * @param bList
    * @return
    */
  def tokenizePredicate(bList: BList): IndexedSeq[String] = {
    filterPredicateTags(bList).map(predicateToToken)
  }

  /**
    * Tokenize a sentence by taking its predicates and tuples of (predicate, argument)
    * @param relationSurface A set of authorized relations between the predicates and arguments (e.g. Set("が"))
    *                        (cf http://nlp.ist.i.kyoto-u.ac.jp/nl-resource/corpus/KyotoCorpus4.0/doc/rel_guideline.pdf).
    * @param bList
    * @return
    */
  def tokenizePredicateWithPA(relationSurface: Set[String])(bList: BList): IndexedSeq[String] = {
    val predicates = filterPredicateTags(bList)
    val predicatesToken = predicates map predicateToToken
    val paTokens = predicates flatMap predicateToPATokens(relationSurface)

    predicatesToken ++ paTokens
  }


  def filterPredicateTags(bList: BList): IndexedSeq[Tag] = {
    bList
      .bunsetsuList.filter(_.paType.isInstanceOf[Predicate])
      .map(bunsetsuPredicateToTagPredicate)
  }

  def bunsetsuPredicateToTagPredicate(bunsetsu: Bunsetsu): Tag = {
    //__get_head_tab_object
    val ts = bunsetsu.tags.filter(_.paType.isInstanceOf[Predicate])
    if (ts.size != 1) throw new TagsException(s"Bunsetsu $bunsetsu contains too many predicate tags. Expected only 1.")
    else ts.head
  }

  def predicateToToken(t: Tag): String = {
    getModalPredicate(t)
  }

  def predicateToPATokens(relationSurface: Set[String])(t: Tag): List[String] = {
    //__change_modal_surface
    val modal_predicate_format = getModalPredicate(t)

    val pas = t.pas.getOrElse(throw new PredicateArgumentsSizeException(s"Tag $t contains no predicate arguments."))
    for {
      (_, arg) <- pas.arguments.toList if relationSurface.isEmpty || relationSurface.contains(arg.relationName)
    } yield List(modal_predicate_format, arg.argWord, arg.relationName).mkString("_")
  }

  def getModalPredicate(t: Tag): String = {
    val pas = t.pas.getOrElse(throw new PredicateArgumentsSizeException(s"Tag $t contains no predicate arguments."))
    //__get_predicate_name
    val cfid = pas.cfid

    //__change_modal_surface
    val featKeySet = t.features.keySet
    val modals = KNPTokenizer.MODALS.filter(featKeySet.contains)
    (if (modals.isEmpty) "" else "-" + modals.mkString("-")) + cfid
  }
}

object KNPTokenizer {

  val MODALS = List("否定表現", "準否定表現", "可能表現")

}