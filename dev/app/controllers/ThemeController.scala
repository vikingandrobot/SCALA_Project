package controllers

import dao.ThemeDAO
import javax.inject.{Inject, Singleton}
import models.{SignUpForm, Theme}
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class ThemeController @Inject()(cc: ControllerComponents, themeDAO: ThemeDAO) extends AbstractController(cc) with play.api.i18n.I18nSupport {

  implicit val themeToJson: Writes[Theme] = (
    (JsPath \ "id").write[Option[Long]] and
      (JsPath \ "name").write[String]
    )(unlift(Theme.unapply))

  implicit val jsonToTheme: Reads[Theme] = (
    (JsPath \ "id").readNullable[Long] and
      (JsPath \ "name").read[String]
  )(Theme.apply _)


  // Form post action to set
 // private val postJoinUrl = routes.UserController.join()
  //private val postLoginUrl = routes.UserController.login()


  def validateJson[A: Reads] = parse.json.validate(
    _.validate[A].asEither.left.map(e => BadRequest(JsError.toJson(e)))
  )

  /**
    * Get the list of all existing users, then return it.
    * The Action.async is used because the request is asynchronous.
    */
  def getThemes = Action.async {
    val themesList = themeDAO.list()
    themesList map (s => Ok(Json.toJson(s)))
  }


  def getTheme(themeId: Long) = Action.async {
    val optionalTheme = themeDAO.findById(themeId)

    optionalTheme.map {
      case Some(c) => Ok(Json.toJson(c))
      case None =>
        NotFound(Json.obj(
          "status" -> "Not Found",
          "message" -> ("Theme #" + themeId + " not found.")
        ))
    }
  }

}
