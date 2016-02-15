package com.enjapan.preprocessing.japanese

import com.atilika.kuromoji.ipadic.Token
import com.enjapan.preprocessing.japanese.tokenizers.KuromojiTokenizer
import org.scalatest.{FunSuite, Matchers}

/**
  * Created by Ugo Bataillard on 2/10/16.
  */
class KuromojiTokenizerTest extends FunSuite with Matchers {

  case class FakeToken(
    surface:String,
    pos1: String = "*",
    pos2: String = "*",
    pos3: String = "*",
    pos4: String = "*",
    baseForm: String = "*"
  ) extends Token(0,
      surface,
      null,
      0,
      null) {
    override def getPartOfSpeechLevel1 = pos1
    override def getPartOfSpeechLevel2 = pos2
    override def getPartOfSpeechLevel3 = pos3
    override def getPartOfSpeechLevel4 = pos4
    override def getBaseForm = baseForm
  }

  test("testTokenizeWithStopPos") {
    val tokenizer = new KuromojiTokenizer(stopPOS = Set(List("助動詞"), List("記号")), stopWords = Set("友達"))
    val tokens = tokenizer.tokenize("今日は,,東京大学の友達に行った。")
    tokens should not contain ("行った", "行っ")
    tokens should not contain ("。", ",,")
    tokens should not contain ("は", "た")
    tokens should contain ("行く")
    tokens should contain ("東京大学")
  }

  test("testTokenizeWithWhiteListPOS") {
    val tokenizer = new KuromojiTokenizer(whiteListPOS = Set(List("名詞","一般")))
    val tokens = tokenizer.tokenize("今日は,,東京大学の友達に行った。")
    tokens should contain only "友達"
  }

  test("testMatchWords") {

    val token = FakeToken("食べて", baseForm = "食べる")
    KuromojiTokenizer.matchWords(Set("食べる"))(token) shouldBe true
    KuromojiTokenizer.matchWords(Set("食べて"))(token) shouldBe true
    KuromojiTokenizer.matchWords(Set("東京","食べて" ))(token) shouldBe true
    KuromojiTokenizer.matchWords(Set("東京","京都" ))(token) shouldBe false

  }

  test("testMatchPOSMasks") {

    val token = FakeToken(surface = "東京", pos1 = "名詞", pos2 = "固有名詞", pos3 = "一般", pos4 = "*")
    KuromojiTokenizer.matchPOSMasks(Set.empty)(token) shouldBe false
    KuromojiTokenizer.matchPOSMasks(Set(List(token.pos1)))(token) shouldBe true
    KuromojiTokenizer.matchPOSMasks(Set(List("test")))(token) shouldBe false
    KuromojiTokenizer.matchPOSMasks(Set(List("test"), List(token.pos1)))(token) shouldBe true
    KuromojiTokenizer.matchPOSMasks(Set(List(token.pos1, token.pos2)))(token) shouldBe true
    KuromojiTokenizer.matchPOSMasks(Set(List(token.pos2, token.pos1)))(token) shouldBe false
    KuromojiTokenizer.matchPOSMasks(Set(List(token.pos1, token.pos2, token.pos3)))(token) shouldBe true
    KuromojiTokenizer.matchPOSMasks(Set(List(token.pos1, token.pos2, token.pos3, token.pos4)))(token) shouldBe true
  }

}
