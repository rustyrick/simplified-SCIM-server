package models

import scala.collection._
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.json.Reads._

object Member {
  implicit val memberReads:Reads[Member] = (
    (__ \ "id").read[String] and
    (__ \ "displayName").read[String]
  ) (Member.apply _)

  implicit val memberWrites = Json.writes[Member]

  def parseMembers(iString:String) = {
    val aList = mutable.MutableList[Member]()
    if(iString.nonEmpty) {
      iString.split('/').map(s => {
        val tmp = s.split('#')
        aList += Member(tmp(0), tmp(1) + " " + tmp(2))
      })
    }
    aList
  }
}

case class Member(id:String, displayName:String)
