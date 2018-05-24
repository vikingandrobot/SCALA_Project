package controllers


import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import dao.InterestDAO
import javax.inject.{Inject, Singleton}
import models.{DeleteInterestForm, InsertInterestForm, Interest}
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc.{AbstractController, Action, ControllerComponents}

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class InterestController @Inject()(cc: ControllerComponents, interestDAO: InterestDAO) extends AbstractController(cc) with play.api.i18n.I18nSupport {

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


  // Form post action to set
  private val postInsertUrl = routes.InterestController.insert()
  private val postDeleteUrl = routes.InterestController.delete()

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

  def getInterestByUser(userId: Long)= Action.async {
    val interestsList = interestDAO.findByUser(userId)
    interestsList map ( s=> Ok(Json.toJson(s)))
  }

  def getInterestByTheme(themeId: Long)= Action.async{
    val interestsList = interestDAO.findByTheme(themeId)
    interestsList map (s => Ok(Json.toJson(s)))
  }


  /** TEST  */
  def insertInterestPage = Action { implicit request =>
    Ok(views.html.interest(InsertInterestForm.form, postInsertUrl))
  }

  def deleteInterestPage = Action { implicit request =>
      Ok(views.html.interestDel(DeleteInterestForm.form, postDeleteUrl))
  }

  def insert = Action { implicit request =>
    InsertInterestForm.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.interest(formWithErrors,postInsertUrl))
      },
      formData => {

      val newInterest = Interest(None,formData.userId, formData.themeId)
      val interest = interestDAO.insert(newInterest)

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
        interestDAO.delete(formData.interestId)

        Ok(optionalInterest.toString)
      }
    )
  }
}

