package controllers

import play.api.mvc.{Action, Controller}
import play.api.libs.json._
import play.api.libs.functional.syntax._

import models._

object SCIM extends Controller {

  //GET /scim/v2/Users  controllers.SCIM.users(filter:Option[String], count:Option[String], startIndex:Option[String])
  def users(filter:Option[String], count:Option[String], startIndex:Option[String]) = Action { request =>
    // Provide logic to get users
    // Support optional filter parameter on exact email match
    // Support optional count parameter to limit responses
    // Support optional startIndex parameter to begin responses on the nth user
    val users = User.getUsers(filter, count, startIndex)
    val jsonObj = Json.obj(
      "schemas" -> Json.arr("urn:ietf:params:scim:schemas:core:2.0:ListResponse"),
      "totalResults" -> users.length,
      "Resources" -> Json.toJson(users)
    )
    System.err.println(Json.prettyPrint(jsonObj))
    Ok(jsonObj)
  }

  //GET /scim/v2/Users/:uid  controllers.SCIM.user(uid:String)
  def user(uid:String) = Action { request =>
    // Provide logic to get a single user, identified by user id
    val user = User.loadUser(uid)
    if(user.nonEmpty) {
      val jsonObj = JsObject(Seq("schemas" -> JsArray(Seq(JsString("urn:ietf:params:scim:schemas:core:2.0:User"))))) ++ Json.toJson(user).as[JsObject]
      System.err.println(Json.prettyPrint(jsonObj))
      Ok(jsonObj)
    }
    else {
      val jsonObj = JsObject(Seq(
        "schemas" -> JsArray(Seq(JsString("urn:ietf:params:scim:api:messages:2.0:Error"))),
        "detail" -> JsString("Resource " + uid + " not found"),
        "status" -> JsString("404")
      ))
      System.err.println(Json.prettyPrint(jsonObj))
      NotFound(jsonObj)
    }
  }

  //POST /scim/v2/Users  controllers.SCIM.createUser()
  //HTTP request must specify the text/json or application/json mime type in its Content-Type header!!!!
  def createUser() = Action(parse.json) { implicit request =>
    implicit val rds = (
      (__ \ "externalId").read[String] and
      (__ \ "userName").read[String] and
      (__ \ "name" \ "givenName").read[String] and
      (__ \ "name" \ "familyName").read[String]
      tupled
    )

    request.body.validate[(String, String, String, String)].map{
        case (externalId, userName, givenName, familyName) => {
          System.err.println("Hello " + externalId + " " + userName + " " + givenName + " " + familyName)
          val storedUID = User.storeUser(User("", Some(externalId), userName, (givenName, familyName), None, ("User", "", "")))
          if(storedUID != "0") {
            val user = User.loadUser(storedUID)
            val jsonObj = JsObject(Seq("schemas" -> JsArray(Seq(JsString("urn:ietf:params:scim:schemas:core:2.0:User"))))) ++ Json.toJson(user).as[JsObject]
            System.err.println(Json.prettyPrint(jsonObj))
            Created(jsonObj)
          }
          else {
            val jsonObj = JsObject(Seq(
              "schemas" -> JsArray(Seq(JsString("urn:ietf:params:scim:api:messages:2.0:Error"))),
              "scimType" -> JsString("uniqueness"),
              "detail" -> JsString("Attempt to duplicate a Resource"),
              "status" -> JsString("409")
            ))
            System.err.println(Json.prettyPrint(jsonObj))
            Conflict(jsonObj)
          }
        }
    }.recoverTotal{
      e => BadRequest("Detected error:"+ JsError.toFlatJson(e))
    }
  }

  //PUT /scim/v2/Users/:uid  controllers.SCIM.updateUser(uid:String)
  //HTTP request must specify the text/json or application/json mime type in its Content-Type header!!!!
  def updateUser(uid:String) = Action(parse.json) { request =>
    // Provide logic to update a single user
    request.body.validate[User].map{
      case userobj => {
        val user = User.updateUser(uid, userobj)
        if(user.nonEmpty) {
          val jsonObj = JsObject(Seq("schemas" -> JsArray(Seq(JsString("urn:ietf:params:scim:schemas:core:2.0:User"))))) ++ Json.toJson(user).as[JsObject]
          System.err.println(Json.prettyPrint(jsonObj))
          Ok(jsonObj)
        }
        else {
          BadRequest("")
        }
      }
    }.recoverTotal{
      e => BadRequest("Detected error:"+ JsError.toFlatJson(e))
    }
  }

  //DELETE /scim/v2/Users/:uid  controllers.SCIM.deleteUser(uid:String)
  def deleteUser(uid:String) = Action { request =>
    // Provide logic to delete a single user
    if(User.deleteUser(uid) > 0) {
      NoContent
    }
    else {
      val jsonObj = JsObject(Seq(
        "schemas" -> JsArray(Seq(JsString("urn:ietf:params:scim:api:messages:2.0:Error"))),
        "detail" -> JsString("Resource " + uid + " not found"),
        "status" -> JsString("404")
      ))
      System.err.println(Json.prettyPrint(jsonObj))
      NotFound(jsonObj)
    }
  }

  //GET /scim/v2/Groups  controllers.SCIM.groups(count:Option[String], startIndex:Option[String])
  def groups(count:Option[String], startIndex:Option[String]) = Action { request =>
    // Provide logic to get groups
    // Support optional count parameter to limit responses
    // Support optional startIndex parameter to begin responses on the nth group
    val groups = Group.getGroups(count, startIndex)
    val jsonObj = Json.obj(
      "schemas" -> Json.arr("urn:ietf:params:scim:schemas:core:2.0:ListResponse"),
      "totalResults" -> groups.length,
      "Resources" -> Json.toJson(groups)
    )
    System.err.println(Json.prettyPrint(jsonObj))
    Ok(jsonObj)
  }

  //GET /scim/v2/Groups/:gid  controllers.SCIM.group(gid:String)
  def group(gid:String) = Action { request =>
    // Provide logic to get a single user, identified by user id
    val group = Group.loadGroup(gid)
    if(group.nonEmpty) {
      val jsonObj = JsObject(Seq("schemas" -> JsArray(Seq(JsString("urn:ietf:params:scim:schemas:core:2.0:Group"))))) ++ Json.toJson(group).as[JsObject]
      System.err.println(Json.prettyPrint(jsonObj))
      Ok(jsonObj)
    }
    else {
      val jsonObj = JsObject(Seq(
        "schemas" -> JsArray(Seq(JsString("urn:ietf:params:scim:api:messages:2.0:Error"))),
        "detail" -> JsString("Resource " + gid + " not found"),
        "status" -> JsString("404")
      ))
      System.err.println(Json.prettyPrint(jsonObj))
      NotFound(jsonObj)
    }
  }

  //PATCH /scim/v2/Groups/:gid  controllers.SCIM.patchGroup(gid:String)
  //HTTP request must specify the text/json or application/json mime type in its Content-Type header!!!!
  def patchGroup(gid:String) = Action(parse.json) { request =>
    // Provide logic to patch a single group
    // Update group assignment --> only ADD and REMOVE operations supported
    implicit val rds = (
      (__ \ "schemas").read[Seq[String]] and
      (__ \ "Operations").read[Option[Seq[Operation]]]
      tupled
    )

    request.body.validate[(Seq[String], Option[Seq[Operation]])].map{
      case (schemas, opobj) => {
        if(schemas.contains("urn:ietf:params:scim:api:messages:2.0:PatchOp")) {
          val returncode:Int = Operation.applyOperations(gid, opobj)
          returncode match {
            case Operation.ERROR => BadRequest("")
            case Operation.OP_SUCCESSFUL => {
              val group = Group.loadGroup(gid)
              val jsonObj = JsObject(Seq("schemas" -> JsArray(Seq(JsString("urn:ietf:params:scim:schemas:core:2.0:Group"))))) ++ Json.toJson(group).as[JsObject]
              System.err.println(Json.prettyPrint(jsonObj))
              Ok(jsonObj)
            }
            case Operation.OP_NOT_SUPPORTED => {
              val jsonObj = JsObject(Seq(
                "schemas" -> JsArray(Seq(JsString("urn:ietf:params:scim:api:messages:2.0:Error"))),
                "detail" -> JsString("Operation not supported"),
                "status" -> JsString("400")
              ))
              System.err.println(Json.prettyPrint(jsonObj))
              BadRequest(jsonObj)
            }
            case Operation.OP_NOTHING_TO_DO => NoContent
          }
        }
        else {
          val jsonObj = JsObject(Seq(
            "schemas" -> JsArray(Seq(JsString("urn:ietf:params:scim:api:messages:2.0:Error"))),
            "detail" -> JsString("Required schema urn:ietf:params:scim:api:messages:2.0:PatchOp not found in request"),
            "status" -> JsString("400")
          ))
          System.err.println(Json.prettyPrint(jsonObj))
          BadRequest(jsonObj)
        }
      }
    }.recoverTotal{
      e => BadRequest("Detected error:"+ JsError.toFlatJson(e))
    }
  }
}