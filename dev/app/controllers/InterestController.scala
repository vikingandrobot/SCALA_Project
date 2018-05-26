package controllers

import dao.InterestDAO
import javax.inject.{Inject, Singleton}
import models.{DeleteInterestForm, FindForm, InsertInterestForm, Interest}
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc.{AbstractController, Action, ControllerComponents}
import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, Future }


import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class InterestController @Inject()(cc: ControllerComponents, interestDAO: InterestDAO)
  extends AbstractController(cc) with play.api.i18n.I18nSupport {

  implicit val themeToJson: Writes[Interest] = (
    (JsPath \ "id").write[Option[Long]] and
      (JsPath \ "userId").write[Long] and
        (JsPath \ "themeId").write[Long]
    )(unlift(Interest.unapply))

  implicit val jsonToTheme: Reads[Interest] = (
    (JsPath \ "id").readNullable[Long] and
      (JsPath \ "userId").read[Long] and
        (JsPath \ "themeId").read[Long]
    )(Interest.apply _)


  def validateJson[A: Reads] = parse.json.validate(
    _.validate[A].asEither.left.map(e => BadRequest(JsError.toJson(e)))
  )

  /**
    * Get the list of all existing users, then return it.
    * The Action.async is used because the request is asynchronous.
    */
  def getInterests = Action.async {
    val interestsList = interestDAO.list()
    interestsList map (s => Ok(Json.toJson(s)))
  }


  def getInterest(interestId: Long) = Action.async {
    val optionalInterest = interestDAO.findById(interestId)

    optionalInterest.map {
      case Some(c) => Ok(Json.toJson(c))
      case None =>
        NotFound(Json.obj(
          "status" -> "Not Found",
          "message" -> ("Interest #" + interestId + " not found.")
        ))
    }
  }

  def findUserByTheme(themeId: Long)= Action.async {
    val usersList = interestDAO.findUserByTheme(themeId)
    usersList map ( s=> Ok(s.toString))
  }

  def findThemeByUser(userId: Long)= Action.async{
    val interestsList = interestDAO.findThemeByUser(userId)
    interestsList map (s => Ok(s.toString))
  }

  /** TEST  */

  // Form post action to set
  private val postInsertUrl = routes.InterestController.insert()
  private val postDeleteUrl = routes.InterestController.delete()
  private val postFindUrl = routes.InterestController.find()

  def insertInterestPage = Action { implicit request =>
    Ok(views.html.interest(InsertInterestForm.form, postInsertUrl))
  }

  def deleteInterestPage = Action { implicit request =>
      Ok(views.html.interestDel(DeleteInterestForm.form, postDeleteUrl))
  }

  def findPage = Action { implicit request =>
    Ok(views.html.interestFind(FindForm.form, postFindUrl))
  }

  def insert = Action { implicit request =>
    InsertInterestForm.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.interest(formWithErrors,postInsertUrl))
      },
      formData => {

      val newInterest = Interest(None,formData.userId, formData.themeId)
      val interest = interestDAO.insert(newInterest)
      Await.ready(interest, Duration.Inf)

      Ok(interest.toString)
      }
    )
  }


  def delete = Action { implicit request =>
    DeleteInterestForm.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.interestDel(formWithErrors, postDeleteUrl))
      },
      formData => {

        val optionalInterest = interestDAO.findById(formData.interestId)
        Await.ready(optionalInterest, Duration.Inf)
        interestDAO.delete(formData.interestId)

        Ok(optionalInterest.toString)
      }
    )
  }


  def find = Action{ implicit request =>
    FindForm.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.interestFind(formWithErrors, postFindUrl))
      },
      formData => {

        if (formData.userId.isDefined){
          val optionalThemes = interestDAO.findThemeByUser(formData.userId.get)
          Await.ready(optionalThemes, Duration.Inf)
          Ok(optionalThemes.toString)
        }else if (formData.themeId.isDefined){
          val optionalUsers = interestDAO.findUserByTheme(formData.themeId.get)
          Await.ready(optionalUsers, Duration.Inf)
          Ok(optionalUsers.toString)
        }else{
          val optionalInterest = interestDAO.findById(formData.id.get)
          Await.ready(optionalInterest, Duration.Inf)
          Ok(optionalInterest.toString)
        }
      }
    )
  }
}

