package controllers

import dao.{InterestDAO, ThemeDAO, UserDAO}
import javax.inject.{Inject, Singleton}
import models._
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class InterestController @Inject()(cc: ControllerComponents, interestDAO: InterestDAO, themeDAO:ThemeDAO, userDAO:UserDAO)
  extends AbstractController(cc) with play.api.i18n.I18nSupport {
  /*
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
*/
  /** TEST  */

  // Form post action to set
  private val postNewInterestUrl = routes.InterestController.interestNew()
//  private val postDelInterestUrl = routes.InterestController.delete()

  def interestNewPage = Action.async { implicit request =>
    // Get the session
    request.session.get("connected") match {

      // If the session exists
      case Some(s) =>
        for {
          u <- userDAO.findByUsername(s)
          t <-
            if (u.isDefined) themeDAO.list()
            else Future.successful(Seq.empty)
        } yield {

          if (u.isEmpty) Unauthorized("Oops, you are not connected")
          else Ok(views.html.interestNew(NewInterestForm.form, postNewInterestUrl, t))
        }

      // If the session does not exist
      case None => Future { Unauthorized("Oops, you are not connected") }
    }
  }

  def interestNew = Action.async { implicit request =>
    // Get the connected user
    val user: Future[Option[User]] = {

      // Get the session
      val session = request.session.get("connected")

      if(session.isEmpty)
        Future.failed(new RuntimeException("user not found"))
      else {
        val user = userDAO.findByUsername(session.get)
        for {
          u <- user
          if u.isDefined
        } yield u
      }
    }

    user flatMap {
      case Some(u)=>
        NewInterestForm.form.bindFromRequest.fold(
          formWithErrors => {
            Future{
              BadRequest(views.html.interestNew(formWithErrors, postNewInterestUrl,Seq.empty))
            }
          },
          formData =>{
            Future{
              val newInterest = Interest(None,u.id.get, formData.themeId)
              val interest = interestDAO.insert(newInterest)
             // todo attention si signature change
              Redirect(routes.UserController.profile)
            }
          }

        )
      case None => Future.successful(Unauthorized("Oops, you are not connected"))
    }
  }

 /* def deleteInterestPage = Action { implicit request =>

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
  }*/
}

