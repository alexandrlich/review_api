//package com.reviyou.controllers
//
//
//import play.api.mvc._
//import play.api.data._
//import play.api.data.Forms._
//import play.api.Play
//import play.api.Play.current
//
//object AdminPanelController  extends BaseController {
//
//  val loginForm: Form[(String, String)] = Form(
//    tuple(
//      "user_name" -> text,
//      "password" -> text
//    )
//  )
//  val admin_user_name: String =  Play.application.configuration.getString("adminPanel.user_name").get
//  val admin_password: String = Play.application.configuration.getString("adminPanel.password").get
//
//  var isLogin: Boolean = false
//
//  def isAuthorized :Boolean = {
//    isLogin
//  }
//
//  def Authorization (name: String, password: String) = {
//    isLogin = name == admin_user_name && password == admin_password
//  }
//
//  def index = Action {
//    if (isAuthorized)
//      Ok(views.html.index())
//    else
//      Redirect(routes.AdminPanelController.loginPage)
//
//  }
//
//  def login() = Action { implicit request =>
//    val (user_name, password) = loginForm.bindFromRequest.get
//    Authorization(user_name, password)
//
//    if (isAuthorized)
//      Redirect(routes.AdminPanelController.index)
//    else
//      Redirect(routes.AdminPanelController.loginPage)
//  }
//
//  def loginPage = Action {
//    Ok(views.html.login(loginForm))
//  }
//}