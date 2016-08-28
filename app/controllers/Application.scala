package controllers

import models.ResetPassword
import services.{UserService,UserTokenService}
import models.{Profile,User,UserToken,Email,Password}
import com.mohiva.play.silhouette.api.util.PasswordHasher
import utils.Mailer
import play.api.i18n.Messages
import play.api._
import java.util.UUID
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import scala.concurrent.duration.FiniteDuration
import javax.inject.Inject
import utils.auth.JWTEnv
import play.api.libs.json.__
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import com.mohiva.play.silhouette.api.Authenticator.Implicits.RichDateTime
import com.mohiva.play.silhouette.api.LoginEvent
import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.{Environment,LoginInfo,Silhouette}
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.Clock
import com.mohiva.play.silhouette.api.util.Credentials
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.collection.mutable.ArrayBuffer

class Application @Inject() (
    val messagesApi: MessagesApi,
  silhouette: Silhouette[JWTEnv],
  userService : UserService,
  userTokenService: UserTokenService,
  authInfoRepository: AuthInfoRepository,
  credentialsProvider: CredentialsProvider,
  configuration: Configuration,
  passwordHasher: PasswordHasher,
  mailer: Mailer,
  clock: Clock)
  extends Controller with I18nSupport {

    def resetPassword(tokenId:String) = Action.async { implicit request =>
    val id = UUID.fromString(tokenId)
    userTokenService.find(id).flatMap {
      case None => 
        Future.successful(NotFound(views.html.errors.notFound(request)))
      case Some(token) if !token.isSignUp && !token.isExpired => {
        Future.successful(Ok(views.html.resetPassword(token.id.toString, Application.ResetPasswordForm)))
      }
      case Some(token) => 
        userTokenService.remove(id).map {_ => NotFound(views.html.errors.notFound(request))}
    }
  }
  
    def startResetPassword = Action.async(parse.json) {
      import models.User.emailJsonFormat
      implicit request =>
        request.body.validate[Email].map { 
          data => silhouette.env.identityService.retrieve(LoginInfo(CredentialsProvider.ID, data.email)).flatMap {
            case None => Future.successful(Ok("error.noUser"))
            case Some(user) => for {
              token <- userTokenService.save(UserToken.create(user.id, user.profiles(0).email.get, false))
            } yield {
              mailer.resetPassword(user.profiles(0).email.get, link = routes.Application.resetPassword(token.id.toString).absoluteURL())
              Ok(Json.obj("message" -> Messages("email.sent")))
            }
          }
        }.recoverTotal {
      case error =>
        Future.successful(Unauthorized(Json.obj("message" -> Messages("invalid.data"))))
    }
}
    
    
   def handleResetPassword(tokenId:String) = Action.async { implicit request =>
    Application.ResetPasswordForm.bindFromRequest.fold(
      bogusForm => Future.successful(BadRequest("Bad request")),
      passwords => {
        val id = UUID.fromString(tokenId)
        userTokenService.find(id).flatMap {
          case None => 
            Future.successful(NotFound(views.html.errors.notFound(request)))
          case Some(token) if !token.isSignUp && !token.isExpired =>
            val loginInfo = LoginInfo(CredentialsProvider.ID, token.email)
            for {
              _ <- authInfoRepository.save(loginInfo, passwordHasher.hash(passwords.password1))
              authenticator <- silhouette.env.authenticatorService.create(loginInfo)
              value <- silhouette.env.authenticatorService.init(authenticator)
              _ <- userTokenService.remove(id)
              result <- Future.successful(Ok(views.html.resetPasswordDone()))
            } yield result
        }
      } 
    )
  }

}

object Application {

  /** The form definition for the "create a widget" form.
   *  It specifies the form fields and their types,
   *  as well as how to convert from a Widget to form data and vice versa.
   */
  val ResetPasswordForm = Form(
    mapping(
      "password1" -> text,
      "password2" -> text
    )(ResetPassword.apply)(ResetPassword.unapply)

)

}