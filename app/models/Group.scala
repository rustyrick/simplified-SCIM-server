package models

import anorm._
import play.api.db.DB
import play.api.Play.current
import play.api.libs.functional.syntax._
import java.util.Calendar
import java.text.SimpleDateFormat

import play.api.libs.functional.syntax.unlift
import play.api.libs.json.{Format, __}

case class Group(id:String, externalId:Option[String], name:String, members:Seq[Member], meta:(String, String, String))

object Group {
  implicit val groupFormat: Format[Group] = (
    (__ \ "id").format[String] and
      (__ \ "externalId").format[Option[String]] and
      (__ \ "displayName").format[String] and
      (__ \ "members").format[Seq[Member]] and
      (__ \ "meta").format(
        (__ \ "resourceType").format[String] and
        (__ \ "created").format[String] and
        (__ \ "lastModified").format[String]
        tupled
      )
    ) (Group.apply, unlift(Group.unapply))

  def getGroups(count:Option[String], startIndex:Option[String]): List[Group] = {
    DB.withConnection { implicit c =>
      val startFrom: Long = if (startIndex.isDefined) startIndex.getOrElse("0").toInt else "0".toInt
      val maxResult: Long = if (count.isDefined) count.getOrElse("2147483647").toInt else "2147483647".toInt

      val rowParser:RowParser[Group] = (
          SqlParser.int("idGroup") ~
          SqlParser.get[Option[String]]("externalId") ~
          SqlParser.str("displayName") ~
          SqlParser.getAliased[String]("membersinfo") ~
          SqlParser.date("created") ~
          SqlParser.date("lastModified")
        ) map {
        case idGroup ~ externalId ~ displayName ~ membersinfo ~ created ~ lastModified =>
          val creationCal = Calendar.getInstance()
          val updateCal = Calendar.getInstance()
          creationCal.setTime(created)
          updateCal.setTime(lastModified)
          val sf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
          Group(Integer.toString(idGroup), externalId, displayName, Member.parseMembers(membersinfo), ("Group", sf.format(creationCal.getTime()), sf.format(updateCal.getTime())))
      }
      val rsParser:ResultSetParser[List[Group]] = rowParser.*
      val query:String = "SELECT G.*, GROUP_CONCAT(CONCAT_WS('#', U.idUser, U.givenName, U.familyName) SEPARATOR '/') AS `membersinfo` " +
        "FROM `Groups` G " +
        "LEFT JOIN `UserAssoc` A ON A.groupId = G.idGroup " +
        "LEFT JOIN `Users` U ON A.userId = U.idUser " +
        "GROUP BY idGroup " +
        "LIMIT {startFrom},{maxResult};"

      System.err.println(query)

      SQL(query).on(
        "startFrom" -> startFrom,
        "maxResult" -> maxResult
      ).as(rsParser)
    }
  }

  def loadGroup(groupid:String): Option[Group] = {
    DB.withConnection { implicit c =>

      val rowParser:RowParser[Group] = (
        SqlParser.int("idGroup") ~
          SqlParser.get[Option[String]]("externalId") ~
          SqlParser.str("displayName") ~
          SqlParser.getAliased[String]("membersinfo") ~
          SqlParser.date("created") ~
          SqlParser.date("lastModified")
        ) map {
        case idGroup ~ externalId ~ displayName ~ membersinfo ~ created ~ lastModified =>
          val creationCal = Calendar.getInstance()
          val updateCal = Calendar.getInstance()
          creationCal.setTime(created)
          updateCal.setTime(lastModified)
          val sf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
          Group(Integer.toString(idGroup), externalId, displayName, Member.parseMembers(membersinfo), ("Group", sf.format(creationCal.getTime()), sf.format(updateCal.getTime())))
      }
      val rsParser:ResultSetParser[Option[Group]] = rowParser.singleOpt
      val query:String = "SELECT G.*, GROUP_CONCAT(CONCAT_WS('#', U.idUser, U.givenName, U.familyName) SEPARATOR '/') AS `membersinfo` " +
        "FROM `Groups` G " +
        "LEFT JOIN `UserAssoc` A ON A.groupId = G.idGroup " +
        "LEFT JOIN `Users` U ON A.userId = U.idUser " +
        "WHERE idGroup = {groupid}" +
        "GROUP BY idGroup;"

      System.err.println(query)

      SQL(query).on("groupid" -> groupid).as(rsParser)
    }
  }
}
