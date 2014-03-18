package controllers

import play.api.mvc.Controller
import common.{DogpileAction, Logging, ExecutionContexts}
import scala.concurrent.Future


class ContentController extends Controller with ExecutionContexts with Logging {



  def render(path: String) = DogpileAction{ request =>






    Future.successful(Ok(""))
  }


}
