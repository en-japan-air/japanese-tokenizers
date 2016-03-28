package com.enjapan.preprocessing.japanese

import mariten.kanatools.KanaConverter

import scala.util.matching.Regex

/**
  * Created by Ugo Bataillard on 2/5/16.
  */
class SentenceSplitter {

}

object SentenceSplitter {
  //FIXME NEEDS fixing
  val PONCTUATION = List("？","！","。")
  val PONCTUATION_S = PONCTUATION.mkString
  val PONCTUATION_REGEX = s"[$PONCTUATION_S]"
  val LINE_SPLITTER_REGEX = s"""[^$PONCTUATION_S]+[$PONCTUATION_S]+""".r
  val DOT_SPLITTER_REGEX = s"""[^。]+[。]+""".r

  val PARENTHESIS_REGEX = """[（\(][^）\)]+[）\)]""".r
  val QUOTES_REGEX = """[「『].*[!?。！？]+.*[」』]""".r
  val QUOTES_WITH_MATCHING_GROUPS_REGEX = s"""([「『].*)[$PONCTUATION]+(.*[」』])""".r
  val BLANK_REGEX = """\s+""".r


  def normalize(s:String):String = {
    val conv_op_flags = KanaConverter.OP_HAN_KATA_TO_ZEN_KATA | KanaConverter.OP_HAN_ASCII_TO_ZEN_ASCII
    KanaConverter.convertKana(s.replace(".","。"), conv_op_flags)
  }

  def addLastPunctuation(s:String):String = {
    if (!s.isEmpty && !"。！？".exists( _ == s.last)) s + "。" else s
  }

  def replaceLineBreaks(s:String):String = {
    val res = s.replace("\n", if (PONCTUATION.exists(s.contains)) "" else "。")
    addLastPunctuation(s)
  }

  def replaceQuestionWithCommas(s:String):String = {

    val sp = s.split("？")

    if (sp.size <= 1) {
      s
    } else {
      val r =
        sp.sliding(2).map { case Array(w1, w2) =>
          val w2s = w2.length
          if (w2s == 0) {
            w1
          } else if (w2s <= 4) {
            w1 + "、"
          } else w1 + "？"
        }.mkString

      r + (if (s.last == '?') {
        sp.last.dropRight(1) + "？"
      } else sp.last)
    }
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

  def splitSimple(s:String, splitter: Regex = LINE_SPLITTER_REGEX):List[String] = {
    splitter.findAllIn(addLastPunctuation(s)).toList
  }

  def splitColloquial(s:String): List[String] = {
    val res = replaceQuestionWithCommas(replaceLineBreaks(removeParenthesis(removeBlanks(normalize(s)))))
    if (!List("「","『").exists(res.contains) && List("？","！").exists(res.contains) ) {
      splitSimple(res, DOT_SPLITTER_REGEX)
    } else {
      splitSimple(removeEosInQuotes(res))
    }
  }
}
