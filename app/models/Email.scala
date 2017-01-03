package models

import scala.collection._
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.json.Reads._

object Email {
  implicit val emailReads:Reads[Email] = (
    (__ \ "value").readNullable[String](email) and
    (__ \ "type").readNullable[String] and
    (__ \ "primary").readNullable[Boolean]
  ) (Email.apply _)

  implicit val emailWrites = Json.writes[Email]

  def parseDBresult(iString:String) = {
    val aList = mutable.MutableList[Email]()
    if(iString.nonEmpty) {
      iString.split('/').map(s => {
        val tmp = s.split('#')
        aList += Email(Some(tmp(0)), Some(tmp(1)), Some(if (tmp(2).toInt == 1) true else false))
      })
    }
    aList
  }
}

case class Email(value:Option[String] = None, `type`:Option[String] = None, primary:Option[Boolean] = None)
