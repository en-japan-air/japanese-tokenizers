package com.enjapan.preprocessing.japanese.tokenizers

import com.atilika.kuromoji.ipadic.{Tokenizer, Token}
import scala.collection.JavaConverters._

/**
  * Created by Ugo Bataillard on 2/10/16.
  */
class KuromojiTokenizer(tokenizer: Tokenizer = new Tokenizer(), stopWords: Set[String] = Set
  .empty[String], whiteListPOS: Set[List[String]] = Set.empty[List[String]], stopPOS: Set[List[String]] = Set
  .empty[List[String]]) {

  import KuromojiTokenizer._

  def tokenizeWithMetadata(string: String): Seq[Token] = {
    val tokens: Seq[Token] = tokenizer.tokenize(string).asScala.toSeq
    tokens
      .filterNot(matchWords(stopWords))
      .filterNot(unrecognizedPOS)
      .filter(t => whiteListPOS.isEmpty || matchPOSMasks(whiteListPOS)(t))
      .filterNot(matchPOSMasks(stopPOS))
  }

  def tokenize(string: String): Seq[String] = {
    val tokens: Seq[Token] = tokenizer.tokenize(string).asScala.toSeq
    tokenizeWithMetadata(string).flatMap(extractWord)
  }

  def extractWord(t: Token) = Option(t.getBaseForm).filterNot(_.contains("*")).filterNot(_.trim.isEmpty)
    .orElse(Option(t.getSurface).filterNot(_.trim.isEmpty))
}

object KuromojiTokenizer {

  def matchWords(words: Set[String])(token: Token): Boolean = {
    words.nonEmpty && words.intersect(Set(token.getSurface, token.getBaseForm)).nonEmpty
  }

  def matchPOSMasks(posMasks: Set[List[String]])(token: Token): Boolean = {
    posMasks.nonEmpty && {
      val pos = List(token.getPartOfSpeechLevel1, token.getPartOfSpeechLevel2, token.getPartOfSpeechLevel3, token
        .getPartOfSpeechLevel4)
      val posSubsets = (pos +: (for (i <- 1 to 3) yield pos.take(i))).toSet
      posMasks.intersect(posSubsets).nonEmpty
    }
  }

  def unrecognizedPOS(token: Token): Boolean = {
    token.getPronunciation == "*" &&
      token.getPartOfSpeechLevel1 == "名詞" &&
      token.getPartOfSpeechLevel2 == "サ変接続"
  }
}
