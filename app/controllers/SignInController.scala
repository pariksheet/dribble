package controllers

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

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
import forms.SignInForm
import javax.inject.Inject
import utils.auth.JWTEnv
import net.ceedubs.ficus.Ficus.finiteDurationReader
import net.ceedubs.ficus.Ficus.optionValueReader
import net.ceedubs.ficus.Ficus.toFicusConfig
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.i18n.Messages
import play.api.i18n.MessagesApi
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.libs.json.__
import play.api.mvc.Action
import play.api.mvc.Controller
import services.{UserService,UserTokenService}
import models.{Profile,User,UserToken,Email,Password}
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordHasher
import utils.Mailer
import java.util.UUID
import play.api.i18n.{I18nSupport,MessagesApi,Messages}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.i18n.MessagesApi
import play.api.data.validation.Constraints._


//object SignInController {
//  
//    val resetPasswordForm = Form(
//    mapping(
//      "password1" -> text,
//      "password2" -> text
//    )(ResetPassword.apply)(ResetPassword.unapply)
//
//)
//}
/**
 * The `Sign In` controller.
 *
 * @param messagesApi The Play messages API.
 * @param silhouette The Silhouette stack.
 * @param userService The user service implementation.
 * @param authInfoRepository The auth info repository implementation.
 * @param credentialsProvider The credentials provider.
 * @param socialProviderRegistry The social provider registry.
 * @param configuration The Play configuration.
 * @param clock The clock instance.
 */
class SignInController @Inject() (
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

  /**
   * Converts the JSON into a `SignInForm.Data` object.
   */
  implicit val dataReads = (
    (__ \ 'email).read[String] and
    (__ \ 'password).read[String] and
    (__ \ 'rememberMe).read[Boolean]
  )(SignInForm.Data.apply _)

  /**
   * Handles the submitted JSON data.
   *
   * @return The result to display.
   */
//  import SignInController._
  def submit = Action.async(parse.json) { implicit request =>
    request.body.validate[SignInForm.Data].map { data =>
      credentialsProvider.authenticate(Credentials(data.email, data.password)).flatMap { loginInfo =>
        silhouette.env.identityService.retrieve(loginInfo).flatMap {
          case Some(user) => silhouette.env.authenticatorService.create(loginInfo).map {
            case authenticator if data.rememberMe =>
              val c = configuration.underlying
              authenticator.copy(
                expirationDateTime = clock.now + c.as[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorExpiry"),
                idleTimeout = c.getAs[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorIdleTimeout")
              )
            case authenticator => authenticator
          }.flatMap { authenticator =>
            silhouette.env.eventBus.publish(LoginEvent(user, request))
            silhouette.env.authenticatorService.init(authenticator).map { token =>
              Ok(Json.obj("token" -> token))
            }
          }
          case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
        }
      }.recover {
        case e: ProviderException =>
          Unauthorized(Json.obj("message" -> Messages("invalid.credentials")))
      }
    }.recoverTotal {
      case error =>
        Future.successful(Unauthorized(Json.obj("message" -> Messages("invalid.credentials"))))
    }
  }
  
    
    
   def changePassword = silhouette.SecuredAction.async(parse.json) { implicit request =>
    import models.User.passwordJsonFormat
    request.identity match {
      case user =>
                request.body.validate[Password].map { 
          data => {
            val authInfo = passwordHasher.hash(data.password)
            val loginInfo = LoginInfo(CredentialsProvider.ID, user.profiles(0).email.get)
            silhouette.env.identityService.retrieve(loginInfo).flatMap {
            case None => Future.successful(Ok("error.noUser"))
            case Some(user) => for {
              authInfo <- authInfoRepository.update(loginInfo, authInfo)
            } yield {
              Ok(Json.obj("message" -> Messages("password.updated")))
            }
          }
        }
        }.recoverTotal {
      case error =>
        Future.successful(Unauthorized(Json.obj("message" -> Messages("invalid.data"))))
    }
    }
  }    
}
