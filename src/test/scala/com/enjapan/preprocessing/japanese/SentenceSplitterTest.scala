package com.enjapan.preprocessing.japanese

import org.scalatest.FunSuite

/**
  * Created by mateusz on 2/25/16.
  */
class SentenceSplitterTest extends FunSuite {

  test("Should find all colloquial sentences.") {
    val split2 = SentenceSplitter.splitColloquial("日本語が『チョーーー!!!』大好き！でも下手です。")
    val split3 = SentenceSplitter.splitColloquial("日本語がチョーーー!!!大好き！でも下手です。")
    assert(split2.size == 2)
    assertResult(split2)(List("日本語が『チョーーー!!!』大好き！","でも下手です。"))
    assert(split3.size == 3)
    assertResult(split3)(List("日本語がチョーーー!!!","大好き！","でも下手です。"))
  }

  test("Should remove blanks") {
    val withoutBlanks = SentenceSplitter.removeBlanks("日本 に  来て ５年間   です！ ")
    assertResult(withoutBlanks)("日本に来て５年間です！")
  }

  test("Should remove EOS in Japanese quotes.") {
    val withoutEOS = SentenceSplitter.removeEosInQuotes("『日本語がうまいです！でもまだまだ勉強しないと！』ってよく言われます。")
    assertResult(withoutEOS)("『日本語がうまいですでもまだまだ勉強しないと』ってよく言われます。")
  }
}
