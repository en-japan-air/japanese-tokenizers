package com.enjapan.preprocessing.japanese.tokenizers

import cats.data.Xor
import com.enjapan.knp.models.{BList, Bunsetsu, Predicate, Tag}
import com.enjapan.knp.{KNP, KNPCli, ParseException}
import com.enjapan.preprocessing.exceptions.{PredicateArgumentsSizeException, TagsException}

/**
  * Created by Ugo Bataillard on 2/4/16.
  * Parse and tokenize Japanese sentences to tokens
  */
class KNPTokenizer(val knp:KNP = new KNPCli()) {

  /**
    * Runs KNP on a sentence
    *
    * @param sentence
    * @return
    */
  def runKnp(sentence: String): Xor[ParseException, BList] = {
    knp(sentence)
  }

  /**
    * Transforms output from KNP to a BList
    *
    * @param knpLines
    * @return
    */
  def parseKnp(knpLines: Iterable[String]): Xor[ParseException, BList] = {
    knp.parser.parse(knpLines)
  }

  /**
    * Tokenize a sentence by only taking its predicates
    *
    * @param bList
    * @return
    */
  def tokenizePredicate(bList: BList): IndexedSeq[Xor[PredicateArgumentsSizeException, String]] = {
    filterPredicateTags(bList).map(predicateToToken)
  }

  /**
    * Tokenize a sentence by taking its predicates and tuples of (predicate, argument)
    *
    * @param relationSurface A set of authorized relations between the predicates and arguments (e.g. Set("が"))
    *                        (cf http://nlp.ist.i.kyoto-u.ac.jp/nl-resource/corpus/KyotoCorpus4.0/doc/rel_guideline.pdf).
    * @param bList
    * @return
    */
  def tokenizePredicateWithPA(relationSurface: Set[String])(bList: BList): IndexedSeq[Xor[PredicateArgumentsSizeException,String]] = {
    val predicates = filterPredicateTags(bList)
    val predicatesToken = predicates map predicateToToken
    val paTokens = predicates flatMap { p =>
      predicateToPATokens(relationSurface)(p) match {
        case Xor.Right(l) => l.map(e => Xor.right(e))
        case Xor.Left(err) => List(Xor.left(err))
      }
    }

    predicatesToken ++ paTokens
  }


  def filterPredicateTags(bList: BList): IndexedSeq[Tag] = {
    bList
      .bunsetsuList.filter(_.paType.isInstanceOf[Predicate])
      .flatMap(bunsetsuPredicateToTagPredicate)
  }

  def bunsetsuPredicateToTagPredicate(bunsetsu: Bunsetsu): Option[Tag] = {
    //__get_head_tab_object
    val ts = bunsetsu.tags.filter(_.paTypes.exists(_.isInstanceOf[Predicate]))
    // FIXME Actually concatenate the tags somehow (see with Mitsu)
    ts.headOption
  }

  def predicateToToken(t: Tag): Xor[PredicateArgumentsSizeException, String] = {
    getModalPredicate(t)
  }

  def predicateToPATokens(relationSurface: Set[String])(t: Tag): Xor[PredicateArgumentsSizeException, List[String]] = {
    //__change_modal_surface
    getModalPredicate(t).flatMap { modalPredicateFormat =>
      val tokens = t.pas.map { pas =>
        for {
          (_, arg) <- pas.arguments.toList if relationSurface.isEmpty || relationSurface.contains(arg.relationName)
        } yield List(modalPredicateFormat, arg.argWord, arg.relationName).mkString("_")
      }
      Xor.fromOption(tokens, new PredicateArgumentsSizeException(s"Tag $t contains no predicate arguments."))
    }
  }

  def getModalPredicate(t: Tag): Xor[PredicateArgumentsSizeException, String] = {
    val modal = t.pas.map { pas =>
      //__get_predicate_name
      val cfid = pas.cfid

      //__change_modal_surface
      val featKeySet = t.features.keySet
      val modals = KNPTokenizer.MODALS.filter(featKeySet.contains)
      (if (modals.isEmpty) "" else "-" + modals.mkString("-")) + cfid
    }
    Xor.fromOption(modal, new PredicateArgumentsSizeException(s"Tag $t contains no predicate arguments."))
  }
}

object KNPTokenizer {

  val MODALS = List("否定表現", "準否定表現", "可能表現")

}