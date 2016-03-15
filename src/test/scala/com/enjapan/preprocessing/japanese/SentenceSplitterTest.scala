package com.enjapan.preprocessing.japanese

import org.scalatest.{FunSuite, Matchers}

/**
  * Created by mateusz on 2/25/16.
  */
class SentenceSplitterTest extends FunSuite with Matchers {

  test("Should find all colloquial sentences.") {
    val s1 = "先日の不満買取センターのキャンペーンで転職サイトについてでていた。そして、それに投稿したら、後日送られてくるアンケートに答えたらポイントが１０００ポイントもらえるものだったので、ちょうど転職サイトを利用したばかりだったので応募したら、今日届いたメールが、名前と電話番号とアドレスを教えて下さい。それを利用します。のようなことがかいてあって、そんなのアンケートじゃないだろうと思って、結局そこを書かないと１０００ポイントもらえないし、エントリーしなければよかったと後悔"
    val split1 = SentenceSplitter.splitColloquial(s1)
    val split2 = SentenceSplitter.splitColloquial("日本語が『チョーーー!!!』大好き！でも下手です。へへへ")
    val split3 = SentenceSplitter.splitColloquial("日本語がチョーーー!!!大好き！でも下手です。ははは")
    val split4 = SentenceSplitter.splitColloquial("日本語が。大好き！でも下手です。へへへ")

    split1 should have size 4
    split1 should contain theSameElementsInOrderAs List("先日の不満買取センターのキャンペーンで転職サイトについてでていた。", "そして、それに投稿したら、後日送られてくるアンケートに答えたらポイントが１０００ポイントもらえるものだったので、ちょうど転職サイトを利用したばかりだったので応募したら、今日届いたメールが、名前と電話番号とアドレスを教えて下さい。","それを利用します。","のようなことがかいてあって、そんなのアンケートじゃないだろうと思って、結局そこを書かないと１０００ポイントもらえないし、エントリーしなければよかったと後悔。")

    split4 should have size 4
    assertResult(split4)(List("日本語が。", "大好き！", "でも下手です。","へへへ。"))

    split2 should have size 3
    assertResult(split2)(List("日本語が『チョーーー!!!』大好き！", "でも下手です。へへへ。"))

    split3 should have size 4
    assertResult(split3)(List("日本語がチョーーー!!!", "大好き！", "でも下手です。","ははは。"))

  }

  test("Should remove blanks") {
    val withoutBlanks = SentenceSplitter.removeBlanks("日本 に  来て ５年間   です！ ")
    assertResult(withoutBlanks)("日本に来て５年間です！")
  }

  test("Should remove EOS in Japanese quotes.") {
    val withoutEOS = SentenceSplitter.removeEosInQuotes("『日本語がうまいです！でもまだまだ勉強しないと！』ってよく言われます。")
    assertResult(withoutEOS)("『日本語がうまいですでもまだまだ勉強しないと』ってよく言われます。")
  }

  test("Should normalize sentence") {
    val res = SentenceSplitter.normalize("日本語が『チョーーー!!!』大好き！abc123? ｶﾀﾅ？.。")
    res shouldBe "日本語が『チョーーー！！！』大好き！ａｂｃ１２３？　カタナ？。。"
  }
}