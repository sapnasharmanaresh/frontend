package controllers

import play.api.mvc._
import common.ExecutionContexts
import services.{IdentityRequest, IdRequestParser, IdentityUrlBuilder}
import com.google.inject.{Inject, Singleton}
import utils.SafeLogging
import model.{Cached, NoCache, IdentityPage}
import idapiclient.IdApiClient
import actions.AuthActionWithUser
import play.filters.csrf.{CSRFCheck, CSRFAddToken}
import form._
import scala.concurrent._
import scala.concurrent.Future
import com.gu.identity.model.User
import discussion.DiscussionApi
import play.api.Plugin
import conf.Configuration
import client.Response

@Singleton
class PublicProfileController @Inject()(idUrlBuilder: IdentityUrlBuilder,
                                        authActionWithUser: AuthActionWithUser,
                                        identityApiClient: IdApiClient,
                                        idRequestParser: IdRequestParser
                                      )
  extends Controller
  with ExecutionContexts
  with SafeLogging{

  val discussionApi = new DiscussionApi {
    protected val apiRoot = Configuration.discussion.apiRoot
    override protected val clientHeaderValue: String = Configuration.discussion.apiClientHeader
  }

  val page = IdentityPage("/profile/public", "Public profile", "public profile")

  def displayPublicProfileForm = displayForm(isPublicFormActive = true)
  def displayAccountForm = displayForm(isPublicFormActive = false)

  protected def displayForm(isPublicFormActive: Boolean) = CSRFAddToken {
    authActionWithUser.apply { implicit request =>
      val idRequest = idRequestParser(request)
      val forms = ProfileForms(request.user, isPublicFormActive)
      NoCache(Ok(views.html.profileForms(page.tracking(idRequest), request.user, forms, idRequest, idUrlBuilder)))
    }
  }

  def submitPublicProfileForm() = submitForm(isProfileForm = true)
  def submitAccountForm() = submitForm(isProfileForm = false)

  def submitForm(isProfileForm: Boolean) = CSRFCheck {
    authActionWithUser.async {
      implicit request =>
        val idRequest = idRequestParser(request)
        val forms = ProfileForms(request.user, isProfileForm).bindFromRequest(request)
        val futureFormOpt = forms.activeForm.value map {
          data: UserFormData =>
            identityApiClient.saveUser(request.user.id, data.toUserUpdate(request.user), request.auth) map {
              case Left(errors) =>
                forms.withErrors(errors)

              case Right(user) => forms.bindForms(user)
            }
        }

        val futureForms = futureFormOpt getOrElse Future.successful(forms)
        futureForms map {
          forms =>
            NoCache(Ok(views.html.profileForms(page.accountEdited(idRequest), request.user, forms, idRequest,idUrlBuilder)))
        }
    }
  }

  def page(url: String) = IdentityPage(url, "Public profile", "public profile")

  def renderProfileFromVanityUrl(vanityUrl: String) = renderPublicProfilePage(
    "/user/" + vanityUrl,
    identityApiClient.userFromVanityUrl(vanityUrl)
  )

  def renderProfileFromId(id: String) = renderPublicProfilePage("/user/id/"+id, identityApiClient.user(id))

  def renderPublicProfilePage(url: String, futureUser: => Future[Response[User]]) = Action.async {
    implicit request =>
//      futureUser flatMap {
//        case Left(errors) =>
//          logger.info(s"public profile page returned errors ${errors.toString()}")
//          NotFound(views.html.errors._404())
//
//        case Right(user) =>
//          val idRequest = idRequestParser(request)
//
//          for {
//            comments <- discussionApi.commentsForUser(user.getId())
//          } yield {
//            Cached(60)(Ok(views.html.public_profile_page(page, idRequest, idUrlBuilder, user, comments)))
//          }
//
//          //Cached(60)(Ok(views.html.public_profile_page(page, idRequest, idUrlBuilder, user)))
//          Cached(60)(Ok(views.html.public_profile_page(page(url), idRequest, idUrlBuilder, user)))

      futureUser flatMap {
        case Right(user: User) => {
          val comments = discussionApi.commentsForUser(user.getId())
          val idRequest = idRequestParser(request)
          comments map { commentList =>
            Cached(60)(Ok(views.html.public_profile_page(page, idRequest, idUrlBuilder, user, commentList)))

          }
        }
        case Left(errors) => {
          logger.info(s"public profile page returned errors ${errors.toString()}")
          Future.successful(NotFound(views.html.errors._404()))
        }
      }
  }

}
