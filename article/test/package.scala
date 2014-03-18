package test

import conf.Configuration
import org.scalatest.Tag
import java.util.{List => JList}
import collection.JavaConversions._

object ArticleComponents extends Tag("article components")

object `package` {

  object HtmlUnit extends EditionalisedHtmlUnit

  object Fake extends FakeApp

  implicit class ListString2FirstNonEmpty(list: JList[String]) {
    lazy val firstNonEmpty: Option[String] = list find { !_.isEmpty }
  }
}