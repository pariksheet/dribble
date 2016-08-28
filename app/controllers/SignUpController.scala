package controllers

import java.util.UUID

import scala.concurrent.Future

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.SignUpEvent
import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.PasswordHasher
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider

import forms.SignUpForm
import forms.SignUpForm.Data.jsonFormat
import javax.inject.Inject
import models.Profile
import models.User
import models.UserToken
import utils.auth.JWTEnv
import play.api.i18n.I18nSupport
import play.api.i18n.Messages
import play.api.i18n.MessagesApi
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.mvc.Action
import play.api.mvc.Controller
import services.UserService
import services.UserTokenService
import utils.Mailer
/**
 * The `Sign Up` controller.
 *
 * @param messagesApi The Play messages API.
 * @param silhouette The Silhouette stack.
 * @param userService The user service implementation.
 * @param authInfoRepository The auth info repository implementation.
 * @param avatarService The avatar service implementation.
 * @param passwordHasher The password hasher implementation.
 */
class SignUpController @Inject() (
  val messagesApi: MessagesApi,
  silhouette: Silhouette[JWTEnv],
  authInfoRepository: AuthInfoRepository,
  userService : UserService,
  userTokenService : UserTokenService,
  passwordHasher: PasswordHasher,
  val mailer: Mailer)
  extends Controller with I18nSupport {

  /**
   * Handles the submitted JSON data.
   *
   * @return The result to display.
   */
  def submit = Action.async(parse.json) { implicit request =>
    request.body.validate[SignUpForm.Data].map { data =>
      val loginInfo = LoginInfo(CredentialsProvider.ID, data.email)
      silhouette.env.identityService.retrieve(loginInfo).flatMap {
        case Some(user) =>
          Future.successful(BadRequest(Json.obj("message" -> Messages("user.exists"))))
        case None =>
          val authInfo = passwordHasher.hash(data.password)
          val profile = Profile(
              loginInfo = loginInfo,
              confirmed =  false,
              email     =  Some(data.email),
              firstName =  Some(data.firstName), 
              lastName  =  Some(data.lastName),
              fullName  =  Some(data.firstName + " " + data.lastName), 
              passwordInfo = Some(authInfo), 
              oauth1Info =  None,
              avatarUrl = None)
          val user = User(
            id = UUID.randomUUID(),
            profiles = List(profile)
          )
          for {
            user <- userService.save(user)
            authInfo <- authInfoRepository.add(loginInfo, authInfo)
            signUptoken <- userTokenService.save(UserToken.create(user.id, profile.email.get, true))
          } yield {
            silhouette.env.eventBus.publish(SignUpEvent(user, request))
            mailer.welcome(profile, routes.SignUpController.confirm(signUptoken.id.toString).absoluteURL())
            Ok(Json.obj("message" -> Messages("user.created")))
          }
      }
    }.recoverTotal {
      case error =>
        Future.successful(Unauthorized(Json.obj("message" -> Messages("invalid.data"))))
    }
  }
  
  
  def confirm(tokenId:String) = Action.async { implicit request =>
    val id = UUID.fromString(tokenId)
    userTokenService.find(id).flatMap {
      case None => 
        Future.successful(NotFound(views.html.errors.notFound(request)))
      case Some(token) if token.isSignUp && !token.isExpired => 
        userService.find(token.userId).flatMap {
          case None => Future.failed(new IdentityNotFoundException(Messages("error.noUser")))
          case Some(user) => 
            val loginInfo = LoginInfo(CredentialsProvider.ID, token.email)
            for {
              authenticator <- silhouette.env.authenticatorService.create(loginInfo)
              value <- silhouette.env.authenticatorService.init(authenticator)
              _ <- userService.confirm(loginInfo)
              _ <- userTokenService.remove(id)
              result <- Future.successful(Ok("Thanks for Confirmation"))
            } yield result
        }
      case Some(token) => 
        userTokenService.remove(id).map {_ => NotFound(views.html.errors.notFound(request))}
    }
  }
}
