package models

import anorm._
import org.jboss.netty.handler.codec.rtsp.RtspResponseStatuses
import play.api.db.DB

import scala.collection._
import play.api.Play.current
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.json.Reads._

object Operation {
  val ERROR:Int = -1
  val OP_SUCCESSFUL:Int = 0
  val OP_NOT_SUPPORTED:Int = 1
  val OP_NOTHING_TO_DO:Int = 2

  implicit val operationReads:Reads[Operation] = (
    (__ \ "op").read[String] and
    (__ \ "path").read[String] and
    (__ \ "value").read[Option[Seq[PatchMember]]]
  ) (Operation.apply _)

  implicit val operationWrites = Json.writes[Operation]

  def applyOperations(gid:String, iOp:Option[Seq[Operation]]): Int = {
    var returnCode:Int = OP_NOTHING_TO_DO
    iOp.get.foreach(o => {
      if((o.op != "add" && o.op != "remove") || o.path != "members") {
        return OP_NOT_SUPPORTED
      }
      else {
        if(o.values.nonEmpty) {
          DB.withTransaction { implicit c =>
            c.setAutoCommit(false)

            try {
              o.values.get.foreach(m => {
                if (o.op == "add") {
                  if(SQL("INSERT INTO `UserAssoc` (`userId`, `groupId`) VALUES ({uid}, {gid});").on(
                    "uid" -> m.value,
                    "gid" -> gid
                  ).executeInsert() == null) { returnCode = OP_NOTHING_TO_DO }
                  else { returnCode = OP_SUCCESSFUL }
                }
                else if (o.op == "remove") {
                  if(SQL("DELETE FROM `UserAssoc` WHERE `userId` = {uid} AND `groupId` = {gid};").on(
                    "uid" -> m.value,
                    "gid" -> gid
                  ).executeUpdate() > 0) { returnCode = OP_SUCCESSFUL }
                  else { returnCode = OP_NOTHING_TO_DO }
                }
              })
              c.commit()
            }
            catch {
              case e: Exception => {
                System.err.println(e.getMessage)
                c.rollback()
                return ERROR
              }
            }
          }
        }
        else {
          returnCode = OP_NOTHING_TO_DO
        }
      }
    })
    returnCode
  }
}

case class Operation(op:String, path:String, values:Option[Seq[PatchMember]])
