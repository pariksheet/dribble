package daos

import scala.concurrent.Future
import javax.inject.Inject
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO

import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._

import play.modules.reactivemongo.ReactiveMongoApi
import play.modules.reactivemongo.json._
import play.modules.reactivemongo.json.collection.JSONCollection

import models.User, User._
import com.mohiva.play.silhouette.api.util.PasswordInfo

class PasswordInfoDao @Inject() (val reactiveMongoApi: ReactiveMongoApi) extends DelegableAuthInfoDAO[PasswordInfo] {
  val users = reactiveMongoApi.db.collection[JSONCollection]("users")


   def find(loginInfo:LoginInfo):Future[Option[PasswordInfo]] =  {
    val user = users.find(Json.obj(
      "profiles.loginInfo" -> loginInfo,
      "profiles.confirmed" -> true
    )).one[User]
    user.flatMap{
    case Some(u) => {
      Future.successful(u.profiles.find( _.loginInfo == loginInfo ).flatMap { x => x.passwordInfo })
    }
    case _ => Future.successful(None) 
    }
  }
  
  

  def add(loginInfo:LoginInfo, authInfo:PasswordInfo):Future[PasswordInfo] = 
    users.update(Json.obj(
      "profiles.loginInfo" -> loginInfo
    ), Json.obj(
      "$set" -> Json.obj("profiles.$.passwordInfo" -> authInfo)
    )).map(_ => authInfo)

  def update(loginInfo:LoginInfo, authInfo:PasswordInfo):Future[PasswordInfo] = 
    add(loginInfo, authInfo)

  def save(loginInfo:LoginInfo, authInfo:PasswordInfo):Future[PasswordInfo] = 
    add(loginInfo, authInfo)

  def remove(loginInfo:LoginInfo):Future[Unit] = 
    users.update(Json.obj(
      "profiles.loginInfo" -> loginInfo
    ), Json.obj(
      "$pull" -> Json.obj(
        "profiles" -> Json.obj("loginInfo" -> loginInfo)
      )
    )).map(_ => ())
}
