package controllers

import play.api.mvc.{RequestHeader, Controller}
import common.{DogpileAction, Logging, ExecutionContexts}
import common.`package`._
import conf.Switches
import repositories.ContentRepository
import model.{Gallery, LiveBlog, Trail, Article}
import views.support.RenderOtherStatus
import repositories.ContentWithStoryPackage


object ContentController extends Controller with ContentRepository with ExecutionContexts with implicits.Requests with Logging {

  def render(path: String) = DogpileAction{ implicit request =>
   lookup(path).map{
      case Left(ContentWithStoryPackage(a: LiveBlog, storyPackage)) => renderLiveBlog(LiveBlogPage(a, storyPackage))
      case Left(ContentWithStoryPackage(a: Article, storyPackage)) => renderArticle(ArticlePage(a, storyPackage))
      case Left(ContentWithStoryPackage(a: Gallery, storyPackage)) => renderGallery(GalleryPage(a, storyPackage, request.getIntParameter("index", 1)))
      case Right(other) => RenderOtherStatus(other)
      case _ => Ok("booooooo") // TODO
    }
  }

  private def renderLiveBlog(model: LiveBlogPage)(implicit request: RequestHeader) = {
      val htmlResponse = () => views.html.liveBlog(model)
      val jsonResponse = () => views.html.fragments.liveBlogBody(model)
      renderFormat(htmlResponse, jsonResponse, model.article, Switches.all)
  }

  private def renderArticle(model: ArticlePage)(implicit request: RequestHeader) = {
      val htmlResponse = () => views.html.article(model)
      val jsonResponse = () => views.html.fragments.articleBody(model)
      renderFormat(htmlResponse, jsonResponse, model.article, Switches.all)
  }

  private def renderGallery(model: GalleryPage)(implicit request: RequestHeader) = {
    val htmlResponse = () => views.html.gallery(model.gallery, model.storyPackage, model.index)
    val jsonResponse = () => views.html.fragments.galleryBody(model.gallery, model.storyPackage, model.index)
    renderFormat(htmlResponse, jsonResponse, model.gallery, Switches.all)
  }
}

trait ArticleWithStoryPackage {
  def article: Article
  def storyPackage: Seq[Trail]
}

case class ArticlePage(article: Article, storyPackage: Seq[Trail]) extends ArticleWithStoryPackage
case class LiveBlogPage(article: LiveBlog, storyPackage: Seq[Trail]) extends ArticleWithStoryPackage
case class GalleryPage(gallery: Gallery, storyPackage: Seq[Trail], index: Int)

