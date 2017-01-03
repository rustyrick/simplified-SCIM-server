package models

import scala.collection._
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.json.Reads._

object PatchMember {
  implicit val patchMemberReads:Reads[PatchMember] = (
    (__ \ "value").read[String] and
    (__ \ "display").read[String]
  ) (PatchMember.apply _)

  implicit val patchMemberWrites = Json.writes[PatchMember]
}

case class PatchMember(value:String, display:String)
