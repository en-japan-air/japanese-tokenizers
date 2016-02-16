# japanese-tokenizers

[![Build Status](https://travis-ci.org/en-japan/japanese-tokenizers.svg?branch=master)](https://travis-ci.org/en-japan/japanese-tokenizers)
[![Coverage Status](https://coveralls.io/repos/github/en-japan/japanese-tokenizers/badge.svg?branch=master)](https://coveralls.io/github/en-japan/japanese-tokenizers?branch=master)

A set of Japanese tokenizers. Currently supported are tokenizers using: [Kuromoji](http://www.atilika.org/) and [KNP](http://nlp.ist.i.kyoto-u.ac.jp/EN/?KNP).

## How to install

In your `build.sbt`:
```
resolvers += "en-japan Maven OSS" at "http://dl.bintray.com/en-japan/maven-oss"

libraryDependencies += "com.enjapan" %% "japanese-tokenizers" % "0.0.1"
```

## How to use

Example:
```scala
import com.enjapan.preprocessing.japanese.tokenizers.KuromojiTokenizer

val document = List("京都大学に行った。","飲み過ぎて二日酔いになりました。")
val tokenizer = new KuromojiTokenizer(stopPOS = Set(List("助詞"), List("助動詞"), List("記号"), List("終助詞")))

val tokenized = document.map(tokenizer.tokenize)
tokenized.foreach(tokens => println(tokens.mkString(",")))
```
