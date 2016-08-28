package models

import java.util.UUID

import play.api.libs.json.Json

import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.impl.providers.OAuth1Info
import models.authorizations._

case class Profile(
  loginInfo:LoginInfo,
  confirmed: Boolean,
  email:Option[String],
  firstName: Option[String], 
  lastName: Option[String],
  fullName: Option[String], 
  passwordInfo:Option[PasswordInfo], 
  oauth1Info: Option[OAuth1Info],
  avatarUrl: Option[String]) {
}

case class User(id: UUID, profiles: List[Profile],roles: Set[Role] = Set(SimpleUser)) extends Identity {
  def profileFor(loginInfo:LoginInfo) = profiles.find(_.loginInfo == loginInfo)
  def fullName(loginInfo:LoginInfo) = profileFor(loginInfo).flatMap(_.fullName)
}

case class Email(email: String)
case class Password(password: String)

object User {
  implicit val roleFormat = Json.format[Role]
  implicit val passwordInfoJsonFormat = Json.format[PasswordInfo]
  implicit val oauth1InfoJsonFormat = Json.format[OAuth1Info]
  implicit val profileJsonFormat = Json.format[Profile]
  implicit val userJsonFormat = Json.format[User]
  implicit val emailJsonFormat = Json.format[Email]
  implicit val passwordJsonFormat = Json.format[Password]
}
