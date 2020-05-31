package utils

import play.api.mvc.Call

object CommonCalls {
  def home: Call = controllers.routes.HomeController.index()
  def signin: Call = controllers.routes.HomeController.index()
  def webHome:String = "http://localhost:8081"
}
