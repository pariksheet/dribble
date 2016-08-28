package models.authorizations

import com.mohiva.play.silhouette.api.Authorization
import models.User
import play.api.i18n._
import play.api.mvc.RequestHeader
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import play.api.mvc.Request
import scala.concurrent.Future

/**
 * Check for authorization
 */
case class WithRole(role: Role) extends Authorization[User,JWTAuthenticator] {
  override def isAuthorized[B](user: User, authenticator: JWTAuthenticator)(implicit request: Request[B]): Future[Boolean] = user.roles match {
    case list: Set[Role] => Future.successful(list.contains(role))
    case _               => Future.successful(false)
  }
}
/**
 * Trait for all roles
 */
trait Role {
  def name: String
}

/**
 * Companion object
 */
object Role {

  def apply(role: String): Role = role match {
    case SuperAdmin.name        => SuperAdmin
    case Admin.name      => Admin
    case SimpleUser.name => SimpleUser
    case _               => Unknown
  }

  def unapply(role: Role): Option[String] = Some(role.name)

}

/**
 * Administration role
 */
object SuperAdmin extends Role {
  val name = "god"
}

/**
 * Administration role
 */
object Admin extends Role {
  val name = "admin"
}

/**
 * Normal user role
 */
object SimpleUser extends Role {
  val name = "user"
}

/**
 * The generic unknown role
 */
object Unknown extends Role {
  val name = "-"
}