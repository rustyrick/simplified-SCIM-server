package models

import java.text.SimpleDateFormat

import anorm._
import play.api.db.DB
import play.api.Play.current
import play.api.libs.json._
import play.api.libs.functional.syntax._
import java.util.Calendar

case class User(id:String, externalId:Option[String], user:String, name:(String, String), emails:Option[Seq[Email]], meta:(String, String, String))

object User {
  implicit val userFormat:Format[User] = (
    (__ \ "id").format[String] and
    (__ \ "externalId").format[Option[String]] and
    (__ \ "userName").format[String] and
    (__ \ "name").format(
      (__ \ "givenName").format[String] and
      (__ \ "familyName").format[String]
      tupled
    ) and
    (__ \ "emails").format[Option[Seq[Email]]] and
    (__ \ "meta").format(
      (__ \ "resourceType").format[String] and
      (__ \ "created").format[String] and
      (__ \ "lastModified").format[String]
      tupled
    )
  ) (User.apply, unlift(User.unapply))

  def getUsers(filter:Option[String], count:Option[String], startIndex:Option[String]): List[User] = {
    DB.withConnection { implicit c =>
      val emailFilter:String = if (filter.isDefined && filter.get.contains(" eq ") && filter.getOrElse("%").split(" eq ")(0) == "email") {
        filter.getOrElse("%").split(" eq ").map(s => System.err.println(s))
        filter.getOrElse("%").split(" eq ")(1).replaceAll("\"", "")
      } else "%"
      val startFrom: Long = if (startIndex.isDefined) startIndex.getOrElse("0").toInt else "0".toInt
      val maxResult: Long = if (count.isDefined) count.getOrElse("2147483647").toInt else "2147483647".toInt

      val rowParser: RowParser[User] = (
          SqlParser.int("idUser") ~
          SqlParser.get[Option[String]]("externalId") ~
          SqlParser.str("userName") ~
          SqlParser.str("givenName") ~
          SqlParser.str("familyName") ~
          SqlParser.getAliased[String]("emailinfo") ~
          SqlParser.date("created") ~
          SqlParser.date("lastModified")
        ) map {
        case idUser ~ externalId ~ userName ~ givenName ~ familyName ~ emailinfo ~ created ~ lastModified =>
          val creationCal = Calendar.getInstance()
          val updateCal = Calendar.getInstance()
          creationCal.setTime(created)
          updateCal.setTime(lastModified)
          val sf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
          User(Integer.toString(idUser), externalId, userName, (givenName, familyName), Some(Email.parseDBresult(emailinfo)), ("User", sf.format(creationCal.getTime()), sf.format(updateCal.getTime())))
      }
      val rsParser:ResultSetParser[List[User]] = rowParser.*
      val query:String = "SELECT U.*, GROUP_CONCAT(CONCAT_WS('#', E.value, E.type, E.primary) SEPARATOR '/') AS `emailinfo` " +
        "FROM `Users` U " +
        "LEFT JOIN `Emails` E ON " +
        "E.userId = U.idUser " +
        "WHERE ({emailFilter} != '%' AND U.idUser IN (SELECT `userId` FROM `Emails` WHERE value LIKE {emailFilter})) OR ({emailFilter} = '%') " +
        "GROUP BY idUser " +
        "LIMIT {startFrom},{maxResult};"

      System.err.println(query)

      SQL(query).on(
        "emailFilter" -> emailFilter,
        "startFrom" -> startFrom,
        "maxResult" -> maxResult
      ).as(rsParser)
    }
  }

  def loadUser(userid:String): Option[User] = {
    DB.withConnection { implicit c =>

      val rowParser: RowParser[User] = (
          SqlParser.int("idUser") ~
          SqlParser.get[Option[String]]("externalId") ~
          SqlParser.str("userName") ~
          SqlParser.str("givenName") ~
          SqlParser.str("familyName") ~
          SqlParser.getAliased[String]("emailinfo") ~
          SqlParser.date("created") ~
          SqlParser.date("lastModified")
        ) map {
        case idUser ~ externalId ~ userName ~ givenName ~ familyName ~ emailinfo ~ created ~ lastModified =>
          val creationCal = Calendar.getInstance()
          val updateCal = Calendar.getInstance()
          creationCal.setTime(created)
          updateCal.setTime(lastModified)
          val sf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
          User(Integer.toString(idUser), externalId, userName, (givenName, familyName), Some(Email.parseDBresult(emailinfo)), ("User", sf.format(creationCal.getTime()), sf.format(updateCal.getTime())))
      }
      val rsParser:ResultSetParser[Option[User]] = rowParser.singleOpt
      val query:String = "SELECT U.*, GROUP_CONCAT(CONCAT_WS('#', E.value, E.type, E.primary) SEPARATOR '/') AS `emailinfo` " +
        "FROM `Users` U " +
        "LEFT JOIN `Emails` E ON " +
        "E.userId = U.idUser " +
        "WHERE `idUser` = {userid} " +
        "GROUP BY idUser;"

      System.err.println(query)
      SQL(query).on("userid" -> userid).as(rsParser)
    }
  }

  def storeUser(userobj:User): String = {
    DB.withConnection { implicit c =>
      val query = "INSERT INTO `Users` (`externalId`, `userName`, `givenName`, `familyName`, `created`, `lastModified`) " +
        "VALUES ({externalId}, {userName}, {givenName}, {familyName}, {created}, {lastModified});"

      System.err.println(query)

      try {
        val newPK: Option[Long] = SQL(query).on(
          "externalId" -> userobj.externalId,
          "userName" -> userobj.user,
          "givenName" -> userobj.name._1,
          "familyName" -> userobj.name._2,
          "created" -> new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis()),
          "lastModified" -> new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis())
        ).executeInsert()
        System.err.println("New user: " + newPK.get.toString)
        newPK.get.toString()
      }
      catch {
        case e: Exception => "0"
      }
    }
  }

  def updateUser(uid:String, userobj:User): Option[User] = {
    val oldUser = User.loadUser(uid)
    if(uid != userobj.id) {
      System.err.println("[WARNING!!!] UserId in the request body different from the REST endpoint!!! (REST endpoint is used)")
    }

    // If no changes, no need to update
    if (oldUser.nonEmpty && ((oldUser.get.externalId.get != userobj.externalId.get) ||
      (oldUser.get.user != userobj.user) || // userName
      (oldUser.get.name._1 != userobj.name._1) || // givenName
      (oldUser.get.name._2 != userobj.name._2) || // familyName
      (oldUser.get.emails.toSet != userobj.emails.toSet) // checking email list
    )) {

      DB.withTransaction { implicit c =>
        SQL("DELETE FROM `Emails` WHERE `userId` = {idUser};").on(
          "idUser" -> uid
        ).executeUpdate()

        userobj.emails.get.map(e => {
          SQL("INSERT INTO `Emails` (`value`, `type`, `primary`, `userId`) VALUES ({emailvalue}, {emailtype}, {emailprimary}, {idUser});").on(
            "emailvalue" -> e.value,
            "emailtype" -> e.`type`,
            "emailprimary" -> e.primary,
            "idUser" -> uid
          ).executeInsert()
        })

        val query = "UPDATE `Users` SET " +
          "`externalId` = {externalId}, " +
          "`userName` = {userName}, " +
          "`givenName` = {givenName}, " +
          "`familyName` = {familyName}, " +
          "`lastModified` = {lastModified} " +
          "WHERE idUser = {idUser}; "

        System.err.println("Something changed!")

        System.err.println(query)

        SQL(query).on(
          "externalId" -> userobj.externalId,
          "userName" -> userobj.user,
          "givenName" -> userobj.name._1,
          "familyName" -> userobj.name._2,
          "lastModified" -> new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis()),
          "idUser" -> uid
        ).executeUpdate()
      }

      User.loadUser(uid)
    }
    else {
      System.err.println("Nothing changed!")
      oldUser
    }
  }

  def deleteUser(userid:String): Int = {
    DB.withConnection { implicit c =>
      // Delete Anything related to that user
      SQL(
        """
          | DELETE FROM `Users`
          | WHERE `idUser`={userId};
        """.stripMargin).on(
        "userId" -> userid).executeUpdate()
    }
  }
}