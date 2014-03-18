package model

import implicits.ContentImplicits

object SupportedContentFilter extends ContentImplicits {
  def apply(content: Seq[Content]) = content.filter { c =>
      c.isArticle || c.isLiveBlog || c.isGallery || c.isVideo || c.isSudoku
  }.filterNot { c => c.isPoll }
}
