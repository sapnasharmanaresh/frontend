package repositories

import scala.concurrent.Future
import conf.SwitchingContentApi
import model.{Trail, Content}
import com.gu.openplatform.contentapi.model.{ItemResponse, Content => ApiContent}
import common._
import play.api.mvc.{SimpleResult, RequestHeader}
import implicits.ContentImplicits



case class ContentWithStoryPackage(content: Content, storyPackage: Seq[Trail])

trait ContentRepository extends ExecutionContexts with ContentImplicits with Logging {


  def lookup(path: String)(implicit request: RequestHeader): Future[Either[ContentWithStoryPackage, SimpleResult]]  = {
    val response: Future[ItemResponse] = SwitchingContentApi().item(path, Edition(request))
      .showExpired(true)
      .showTags("all")
      .showFields("all")
      .response

    val result = response map { response =>
      val storyPackage = response.storyPackage map { Content(_) }

      val content = response.content.filter(isSupported).map(Content(_)).map( content =>
        ContentWithStoryPackage(content, storyPackage.filterNot(_.id == content.id))
      )

      ModelOrResult(content, response)
    }

    result recover convertApiExceptions
  }

  private def isSupported(c: ApiContent) = c.isLiveBlog || c.isArticle || c.isSudoku || c.isGallery ||
    c.isVideo || c.isPoll || c.isImageContent
}


