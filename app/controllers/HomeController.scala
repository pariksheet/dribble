package controllers

import scala.concurrent.Future
import com.mohiva.play.silhouette.api.Silhouette

import javax.inject.Inject
import utils.auth.JWTEnv
import play.api.i18n.I18nSupport
import play.api.i18n.MessagesApi
import play.api.libs.json.Json
import play.api.mvc.Controller
import models.authorizations._
import play.api.i18n.Messages

class HomeController @Inject() (
    val messagesApi: MessagesApi,
    val silhouette: Silhouette[JWTEnv])
extends Controller with I18nSupport{
  
  /**
   * Handles the index action.
   *
   * @return The result to display.
   */
  def index = silhouette.UserAwareAction.async { implicit request =>
    request.identity match {
      case Some(user) =>
        Future.successful(Ok(Json.obj("message" -> Messages("user.aware.ack"))))
      case None => 
        Future.successful(BadRequest("Invalid Request"))
    }
  }
  
   def secure = silhouette.SecuredAction.async { implicit request =>
    request.identity match {
      case user =>
        Future.successful(Ok(Json.obj("message" -> Messages("user.secure.ack"))))
    }
  }
   
   def admin = silhouette.SecuredAction(WithRole(Admin)).async { implicit request =>
    request.identity match {
      case user =>
        Future.successful(Ok(Json.obj("message" -> Messages("user.admin.ack"))))
    }
  }
   
}
