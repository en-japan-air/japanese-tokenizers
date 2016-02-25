package com.enjapan.preprocessing.japanese.tokenizers

import com.enjapan.knp.models.{PredicateArgumentAnalysis, Pas, Tag}
import com.enjapan.preprocessing.exceptions.PredicateArgumentsSizeException
import org.scalatest.{FunSuite, Matchers}

/**
  * Created by mateusz on 2/17/16.
  */
class KNPTokenizerTest extends FunSuite with Matchers {

  val knpTokenizer = new KNPTokenizer()

  test("Should fail on no predicate arguments") {
    intercept[PredicateArgumentsSizeException] {
      knpTokenizer.predicateToToken(new Tag(0, "", "", null, Seq(), Map(), Seq(), None))
    }
  }

  test("Should modal predicate containing all modals") {
    val cfid = "cfid"
    val tag = new Tag(0, "", "", null, Seq(), KNPTokenizer.MODALS.map(value => (value, ""))
      .toMap, Seq(), Some(new Pas(cfid, Map())))
    val modalPredicate = knpTokenizer.predicateToToken(tag)
    modalPredicate should be(("-" + KNPTokenizer.MODALS.mkString("-")) + cfid)
  }

  test("Should modal predicate containing only cfid") {
    val cfid = "cfid"
    val tag = new Tag(0, "", "", null, Seq(), Map(), Seq(), Some(new Pas(cfid, Map())))
    val modalPredicate = knpTokenizer.predicateToToken(tag)
    modalPredicate should be(cfid)
  }

  test("Should fail predicate to PA on no predicate arguments") {
    intercept[PredicateArgumentsSizeException] {
      knpTokenizer.predicateToPATokens(Set())(new Tag(0, "", "", null, Seq(), Map(), Seq(), None))
    }
  }

  test("Should return PA tokens for all PA arguments") {
    val cfid = "cfid"
    val tag = new Tag(
      0, "", "", null, Seq(),
      Map("否定表現" -> ""), Seq(),
      Some(new Pas(cfid, Map("test" -> new PredicateArgumentAnalysis("relname", "reltype", "argword", 0, "0")))))
    val tokens = knpTokenizer.predicateToPATokens(Set())(tag)
    tokens should have size 1
    tokens.head should be("-否定表現cfid_argword_relname")
  }

  test("Should filter arguments by relation surface") {
    val cfid = "cfid"
    val tag = new Tag(
      0, "", "", null, Seq(),
      Map("否定表現" -> ""), Seq(),
      Some(new Pas(cfid, Map(
        "test" -> new PredicateArgumentAnalysis("relname", "reltype", "argword", 0, "0"),
        "test2" -> new PredicateArgumentAnalysis("nottobetakenintoaccount", "reltype", "argword", 0, "0")
      )))
    )
    val tokens = knpTokenizer.predicateToPATokens(Set("relname"))(tag)
    tokens should have size 1
    tokens.head should be("-否定表現cfid_argword_relname")
  }
}