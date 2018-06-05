package controllers

import dao.{InterestDAO, ThemeDAO, UserDAO}
import javax.inject.{Inject, Singleton}
import models._
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class InterestController @Inject()(cc: ControllerComponents, interestDAO: InterestDAO, themeDAO: ThemeDAO, userDAO: UserDAO)
  extends AbstractController(cc) with play.api.i18n.I18nSupport {

  // Form post action to set
  private val postNewInterestUrl = routes.InterestController.interestNew()

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
      case None => Future {
        Unauthorized("Oops, you are not connected")
      }
    }
  }

  def interestNew = Action.async { implicit request =>
    // Get the connected user
    val user: Future[Option[User]] = {

      // Get the session
      val session = request.session.get("connected")

      if (session.isEmpty)
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
      case Some(u) =>
        NewInterestForm.form.bindFromRequest.fold(
          formWithErrors => {
            Future {
              BadRequest(views.html.interestNew(formWithErrors, postNewInterestUrl, Seq.empty))
            }
          },
          formData => {
            Future {
              val newInterest = Interest(None, u.id.get, formData.themeId)
              val interest = interestDAO.insert(newInterest)
              // todo attention si signature change
              Redirect(routes.UserController.profile)
            }
          }

        )
      case None => Future.successful(Unauthorized("Oops, you are not connected"))
    }
  }

  def interestDelete(id: Long) = Action.async { implicit request =>
    // Get the connected user
    val user: Future[Option[User]] = {

      // Get the session
      val session = request.session.get("connected")

      if (session.isEmpty)
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
      case Some(u) =>
        Future {
          for (
            i <- interestDAO.findById(id)
            if (i.isDefined)
          ) yield {
            if (i.get.userId.equals(u.id.get)) interestDAO.delete(id)
            else Future.successful(Unauthorized("Oops, an error has occured"))
          }
          Redirect(routes.UserController.profile)
        }
      case None => Future.successful(Unauthorized("Oops, you are not connected"))
    }
  }

}

