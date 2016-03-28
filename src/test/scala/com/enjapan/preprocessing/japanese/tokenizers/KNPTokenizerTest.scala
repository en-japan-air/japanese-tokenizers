package com.enjapan.preprocessing.japanese.tokenizers

import cats.data.Xor
import com.enjapan.knp.models.{Pas, PredicateArgumentAnalysis, Tag}
import com.enjapan.preprocessing.exceptions.PredicateArgumentsSizeException
import org.scalatest.{FunSuite, Matchers}

/**
  * Created by mateusz on 2/17/16.
  */
class KNPTokenizerTest extends FunSuite with Matchers {

  val knpTokenizer = new KNPTokenizer()

  test("Should fail on no predicate arguments") {
    val res = knpTokenizer.predicateToToken(new Tag(0, "", "", null, List(), Map(), List(), None))
    res.swap.getOrElse(throw new Exception("Should not fail")) shouldBe a[PredicateArgumentsSizeException]
  }

  test("Should modal predicate containing all modals") {
    val cfid = "cfid"
    val tag = new Tag(0, "", "", null, List(), KNPTokenizer.MODALS.map(value => (value, ""))
      .toMap, List(), Some(new Pas(cfid, Map())))
    val modalPredicate = knpTokenizer.predicateToToken(tag)
    modalPredicate shouldBe Xor.Right(("-" + KNPTokenizer.MODALS.mkString("-")) + cfid)
  }

  test("Should modal predicate containing only cfid") {
    val cfid = "cfid"
    val tag = new Tag(0, "", "", null, List(), Map(), List(), Some(new Pas(cfid, Map())))
    val modalPredicate = knpTokenizer.predicateToToken(tag)
    modalPredicate shouldBe Xor.Right(cfid)
  }

  test("Should fail predicate to PA on no predicate arguments") {
    val res = knpTokenizer.predicateToPATokens(Set())(new Tag(0, "", "", null, List(), Map(), List(), None))
    res.swap.getOrElse(throw new Exception("Should not fail")) shouldBe a[PredicateArgumentsSizeException]
  }

  test("Should return PA tokens for all PA arguments") {
    val cfid = "cfid"
    val tag = new Tag(
      0, "", "", null, List(),
      Map("否定表現" -> ""), List(),
      Some(new Pas(cfid, Map("test" -> new PredicateArgumentAnalysis("relname", "reltype", "argword", 0, "0")))))
    val tokens = knpTokenizer.predicateToPATokens(Set())(tag).getOrElse(throw new Exception("not right"))
    tokens should have size 1
    tokens.head should be("-否定表現cfid_argword_relname")
  }

  test("Should filter arguments by relation surface") {
    val cfid = "cfid"
    val tag = new Tag(
      0, "", "", null, List(),
      Map("否定表現" -> ""), List(),
      Some(new Pas(cfid, Map(
        "test" -> new PredicateArgumentAnalysis("relname", "reltype", "argword", 0, "0"),
        "test2" -> new PredicateArgumentAnalysis("nottobetakenintoaccount", "reltype", "argword", 0, "0")
      )))
    )
    val tokens = knpTokenizer.predicateToPATokens(Set("relname"))(tag).getOrElse(throw new Exception("not right"))
    tokens should have size 1
    tokens.head should be("-否定表現cfid_argword_relname")
  }


  test("Should parse example sentence") {
    val s = "どこかの会社みたいにプレゼントする気ないんじゃないの？"
    val knpOutput =
      """# S-ID:1 KNP:4.2-1255337 DATE:2016/03/14 SCORE:-1048.14979
        |* 1D <BGH:どこ/どこ><文頭><疑問詞><助詞><連体修飾><体言><指示詞><係:ノ格><区切:0-4><正規化代表表記:どこ/どこ><主辞代表表記:どこ/どこ>
        |+ 1D <BGH:どこ/どこ><文頭><疑問詞><助詞><連体修飾><体言><指示詞><係:ノ格><区切:0-4><名詞項候補><係チ:非用言格解析||用言&&文節内:Ｔ解析格-ヲ><正規化代表表記:どこ/どこ><クエリ削除語>
        |どこ どこ どこ 指示詞 7 名詞形態指示詞 1 * 0 * 0 "代表表記:どこ/どこ" <代表表記:どこ/どこ><正規化代表表記:どこ/どこ><疑問詞><かな漢字><ひらがな><文頭><自立><内容語><タグ単位始><文節始><文節主辞>
        |か か か 助詞 9 接続助詞 3 * 0 * 0 NIL <漢字><かな漢字><ひらがな><付属>
        |の の の 助詞 9 接続助詞 3 * 0 * 0 NIL <かな漢字><ひらがな><付属>
        |* 2D <SM-主体><SM-場所><SM-組織><BGH:会社/かいしゃ><体言><用言:判><係:連用><修飾><レベル:A-><並キ:述:&D:1&&用言:形&&係:連用||&D:1&&副詞||&ST:4.0><区切:0-0><ID:〜に><連用要素><状態述語><正規化代表表記:会社/かいしゃ><主辞代表表記:会社/かいしゃ><並列類似度:1.258>
        |+ 2D <SM-主体><SM-場所><SM-組織><BGH:会社/かいしゃ><体言><用言:判><係:連用><修飾><レベル:A-><並キ:述:&D:1&&用言:形&&係:連用||&D:1&&副詞||&ST:4.0><区切:0-0><ID:〜に><連用要素><状態述語><判定詞句><名詞項候補><省略解析なし><正規化代表表記:会社/かいしゃ><用言代表表記:会社/かいしゃ><時制-無時制><格解析結果:会社/かいしゃ:判0:ガ/U/-/-/-/-;ニ/U/-/-/-/-;デ/U/-/-/-/-;カラ/U/-/-/-/-;時間/U/-/-/-/-;ノ/U/-/-/-/-;ガ２/U/-/-/-/-><解析格:修飾>
        |会社 かいしゃ 会社 名詞 6 普通名詞 1 * 0 * 0 "代表表記:会社/かいしゃ カテゴリ:組織・団体;場所-施設 ドメイン:ビジネス" <代表表記:会社/かいしゃ><カテゴリ:組織・団体;場所-施設><ドメイン:ビジネス><正規化代表表記:会社/かいしゃ><漢字><かな漢字><名詞相当語><自立><内容語><タグ単位始><文節始><文節主辞>
        |みたいに みたいに みたいだ 助動詞 5 * 0 ナ形容詞 21 ダ列基本連用形 7 NIL <かな漢字><ひらがな><活用語><付属>
        |* 4D <BGH:プレゼント/ぷれぜんと+する/する><サ変><サ変動詞><連体修飾><用言:動><係:連格><レベル:B><区切:0-5><ID:（動詞連体）><連体節><動態述語><正規化代表表記:プレゼント/ぷれぜんと><主辞代表表記:プレゼント/ぷれぜんと>
        |+ 4D <BGH:プレゼント/ぷれぜんと+する/する><サ変動詞><連体修飾><用言:動><係:連格><レベル:B><区切:0-5><ID:（動詞連体）><連体節><動態述語><サ変><正規化代表表記:プレゼント/ぷれぜんと><用言代表表記:プレゼント/ぷれぜんと><時制-未来><格関係1:修飾:会社><格関係5:外の関係:の><格解析結果:プレゼント/ぷれぜんと:動1:ガ/U/-/-/-/-;ヲ/U/-/-/-/-;ニ/U/-/-/-/-;デ/U/-/-/-/-;カラ/U/-/-/-/-;ヘ/U/-/-/-/-;時間/U/-/-/-/-;外の関係/N/の/5/0/1;修飾/C/会社/1/0/1;ノ/U/-/-/-/-;トスル/U/-/-/-/-;ニツク/U/-/-/-/->
        |プレゼント ぷれぜんと プレゼント 名詞 6 サ変名詞 2 * 0 * 0 "代表表記:プレゼント/ぷれぜんと カテゴリ:人工物-その他;抽象物" <代表表記:プレゼント/ぷれぜんと><カテゴリ:人工物-その他;抽象物><正規化代表表記:プレゼント/ぷれぜんと><記英数カ><カタカナ><名詞相当語><サ変><サ変動詞><自立><内容語><タグ単位始><文節始><固有キー><文節主辞>
        |する する する 動詞 2 * 0 サ変動詞 16 基本形 2 "代表表記:する/する 付属動詞候補（基本） 自他動詞:自:成る/なる" <代表表記:する/する><付属動詞候補（基本）><自他動詞:自:成る/なる><正規化代表表記:する/する><かな漢字><ひらがな><活用語><とタ系連用テ形複合辞><付属>
        |* 4D <BGH:気/き><体言><一文字漢字><係:未格><隣係絶対><用言一部><裸名詞><区切:0-0><格要素><連用要素><正規化代表表記:気/き><主辞代表表記:気/き>
        |+ 4D <BGH:気/き><体言><一文字漢字><係:未格><隣係絶対><用言一部><裸名詞><区切:0-0><格要素><連用要素><名詞項候補><先行詞候補><正規化代表表記:気/き><解析格:ガ>
        |気 き 気 名詞 6 普通名詞 1 * 0 * 0 "代表表記:気/き 漢字読み:音 カテゴリ:抽象物" <代表表記:気/き><漢字読み:音><カテゴリ:抽象物><正規化代表表記:気/き><漢字><かな漢字><名詞相当語><自立><内容語><タグ単位始><文節始><文節主辞>
        |* -1D <BGH:無い/ない><文末><形副名詞><二重否定><〜ない><用言:形><タグ単位受:-1><機能的基本句><レベル:C><区切:5-5><ID:（文末）><係:文末><提題受:30><主節><格要素><連用要素><状態述語><疑問><モダリティ-疑問><正規化代表表記:無い/ない><主辞代表表記:無い/ない>
        |+ 5D <BGH:無い/ない><文節内><係:文節内><二重否定><〜ない><連体修飾><用言:形><状態述語><正規化代表表記:無い/ない><用言代表表記:無い/ない><時制-現在><時制-無時制><格関係3:ガ:気><格解析結果:無い/ない:形3:ガ/N/気/3/0/1>
        |ない ない ない 形容詞 3 * 0 イ形容詞アウオ段 18 基本形 2 "代表表記:無い/ない 反義:動詞:有る/ある" <代表表記:無い/ない><反義:動詞:有る/ある><正規化代表表記:無い/ない><かな漢字><ひらがな><活用語><自立><内容語><タグ単位始><文節始><文節主辞>
        |んじゃ んじゃ んだ 助動詞 5 * 0 ナ形容詞 21 ダ列タ系連用ジャ形 14 NIL <かな漢字><ひらがな><活用語><付属>
        |ない ない ない 接尾辞 14 形容詞性述語接尾辞 5 イ形容詞アウオ段 18 基本形 2 "代表表記:ない/ない" <代表表記:ない/ない><正規化代表表記:ない/ない><かな漢字><ひらがな><活用語><否定><付属>
        |+ -1D <BGH:無い/ない><文末><形副名詞><二重否定><〜ない><タグ単位受:-1><機能的基本句><レベル:C><区切:5-5><ID:（文末）><係:文末><提題受:30><主節><格要素><連用要素><状態述語><疑問><モダリティ-疑問><外の関係><受けNONE><用言:判><体言止><判定詞句><用言代表表記:の/の><時制-無時制><クエリ削除語><解析連格:外の関係>
        |の の の 名詞 6 形式名詞 8 * 0 * 0 "疑似代表表記 代表表記:の/の" <疑似代表表記><代表表記:の/の><正規化代表表記:の/の><かな漢字><ひらがな><名詞相当語><形副名詞><表現文末><付属><特殊非見出語><内容語><タグ単位始>
        |？ ？ ？ 特殊 1 記号 5 * 0 * 0 NIL <記英数カ><英記号><記号><文末><付属>
        |EOS
      """.stripMargin


    val tokens = knpTokenizer.parseKnp(knpOutput.lines.toIterable) map knpTokenizer.tokenizePredicateWithPA(Set.empty)
    tokens should be('right)
  }
}