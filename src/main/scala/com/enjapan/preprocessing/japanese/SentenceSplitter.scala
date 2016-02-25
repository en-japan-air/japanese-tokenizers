package com.enjapan.preprocessing.japanese

import mariten.kanatools.KanaConverter

/**
  * Created by Ugo Bataillard on 2/5/16.
  */
class SentenceSplitter {

}

object SentenceSplitter {
  val LINE_SPLITTER_REGEX = """(?:[^「『!?！？。\.\"]*(?:[「『\"][^」』\"]*?[」』\"])?[^!?！？。\.]*)*[!?！？。\.]+""".r

  val PARENTHESIS_REGEX = """[（\(][^）\)]+[）\)]""".r
  val QUOTES_REGEX = """[「『].*[!?。！？]+.*[」』]""".r
  val QUOTES_WITH_MATCHING_GROUPS_REGEX = """([「『].*)[!?。！？]+(.*[」』])""".r
  val BLANK_REGEX = """\s+""".r

  def normalize(s:String):String = {
    // TODO check if '.' should be replaced with '。'
    val conv_op_flags = KanaConverter.OP_HAN_KATA_TO_ZEN_KATA | KanaConverter.OP_HAN_LETTER_TO_ZEN_LETTER
    KanaConverter.convertKana(s, conv_op_flags)
  }

  def replaceLineBreaks(s:String):String = {
    // TODO improve with 。！？
    s.replace("\n", if (s.contains('。')) "" else "。")
  }

  def replaceQuestionWithCommas(s:String):String = {

    val sp = s.split("""(\?|？)""")

    val r = sp.sliding(2).map { case Array(w1,w2) =>
      val w2s = w2.length
      if (w2s == 0) {
        w1
      } else if (w2s <= 4) {
        w1 + "、"
      } else w1 + "？"
    }.mkString

    r + (if(s.last == '?'){
      sp.last.dropRight(1) + "？"
    } else sp.last)
  }

  def removeParenthesis(s:String):String = {
    PARENTHESIS_REGEX.replaceAllIn(s, "")
  }

  def removeEosInQuotes(s:String):String = {
    var result = s
    while (QUOTES_REGEX.findFirstIn(result).isDefined) {
      result = QUOTES_WITH_MATCHING_GROUPS_REGEX.replaceAllIn(result, "$1$2")
    }
    result
  }

  def removeBlanks(s:String) = {
    BLANK_REGEX.replaceAllIn(s, "")
  }

  def splitColloquial(s:String): List[String] = {
    SentenceSplitter.LINE_SPLITTER_REGEX.findAllIn(s).filterNot(_.isEmpty).toList
  }
}
