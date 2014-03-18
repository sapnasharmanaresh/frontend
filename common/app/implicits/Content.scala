package implicits

import model.Gallery
import com.gu.openplatform.contentapi.model.{ Content => ApiContent }
import model.Content

trait ContentImplicits {

  implicit class MaybeGallery(c: Content) {

    lazy val maybeGallery = c match {
      case g: Gallery => Some(g)
      case _ => None
    }

  }

  implicit class ApiContent2Is(content: ApiContent) {
    lazy val isLiveBlog: Boolean = content.tags exists { _.id == "tone/minutebyminute" }
    lazy val isArticle: Boolean = content.tags exists { _.id == "type/article" }
    lazy val isSudoku: Boolean = content.tags exists { _.id == "type/sudoku" }
    lazy val isGallery: Boolean = content.tags exists { _.id == "type/gallery" }
    lazy val isVideo: Boolean = content.tags exists { _.id == "type/video" }
    lazy val isPoll: Boolean = content.tags exists { _.id == "type/poll" }
    lazy val isImageContent: Boolean = content.tags exists { tag => List("type/cartoon", "type/picture", "type/graphic").contains(tag.id) }
  }

  implicit class Content2Is(content: Content) {
    lazy val isLiveBlog: Boolean = content.tags exists { _.id == "tone/minutebyminute" }
    lazy val isArticle: Boolean = content.tags exists { _.id == "type/article" }
    lazy val isSudoku: Boolean = content.tags exists { _.id == "type/sudoku" }
    lazy val isGallery: Boolean = content.tags exists { _.id == "type/gallery" }
    lazy val isVideo: Boolean = content.tags exists { _.id == "type/video" }
    lazy val isPoll: Boolean = content.tags exists { _.id == "type/poll" }
    lazy val isImageContent: Boolean = content.tags exists { tag => List("type/cartoon", "type/picture", "type/graphic").contains(tag.id) }
  }
}
