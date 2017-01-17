package controllers

import javax.inject.Inject

import models.{KategoriaRepo, TowarRepo,  UserData}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.{Action, Controller}
import play.twirl.api.{Html}
import views.html
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}

class Application @Inject()(val messagesApi: MessagesApi)( kategoriaRepo: KategoriaRepo,
                            towarRepo: TowarRepo)
                           extends Controller  with I18nSupport {

  def index = Action { implicit rs =>
    val user = UserData("",0)
    val title = "Collector"
    Ok(html.index(title,user))
  }

  def x_user() = Action { implicit rs =>
    //Przesyłanie danych do formularza
    val user = UserData("test",1)
    Ok(html.xuser(Application.contactForm.fill(user)))
  }

  def userPost = Action{ implicit rs =>
    val formValidationResult = Application.contactForm.bindFromRequest
    formValidationResult.fold({ formWithErrors =>
      BadRequest(html.xuser(formWithErrors))
    }, { xuser =>
      // Pobieranie danych z formularza
      Ok(html.index("after user",xuser))
    })
  }

  def listKategorie = Action.async { implicit rs =>
    kategoriaRepo.all
      .map(kategorie => Ok(views.html.kategorie(kategorie)))
  }

  def listT(page: Int, orderBy: Int, filter: String) = Action.async { implicit request =>
    val towary = towarRepo.list(page = page, orderBy = orderBy, filter = ("%" + filter + "%"))
    towary.map(tk => Ok(html.towary(tk,orderBy,filter)))
  }

  def listZ(page: Int, orderBy: Int, filter: String) = Action.async { implicit request =>
    val towary = towarRepo.list(page = page, orderBy = orderBy, filter = ("%" + filter + "%"))
    towary.map(tk => Ok(html.towary(tk,orderBy,filter)))
  }
}

object Application{
  val contactForm = Form(
    mapping(
      "name" -> text,
      "age" -> number
    )(UserData.apply)(UserData.unapply)
  )
}
