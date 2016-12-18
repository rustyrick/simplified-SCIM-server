package controllers

import play.api.mvc.{Controller, Action}


object SCIM extends Controller {

  def users(filter:Option[String], count:Option[String], startIndex:Option[String]) = Action { request =>
    // Todo: Provide logic to get users
    // Todo: Support optional filter parameter on exact email match
    // Todo: Support optional count parameter to limit responses
    // Todo: Support optional startIndex parameter to begin responses on the nth user
    Ok
  }

  def user(uid:String) = Action { request =>
    // Todo: Provide logic to get a single user, identified by user id
    Ok
  }

  def createUser() = Action { request =>
    // Todo: Provide logic to create a single user
    Ok
  }

  def updateUser(uid:String) = Action { request =>
    // Todo: Provide logic to update a single user
    Ok
  }

  def deleteUser(uid:String) = Action { request =>
    // Todo: Provide logic to delete a single user
    Ok
  }

  def groups(count:Option[String], startIndex:Option[String]) = Action { request =>
    // Todo: Provide logic to get groups
    // Todo: Support optional count parameter to limit responses
    // Todo: Support optional startIndex parameter to begin responses on the nth group
    Ok
  }

  def patchGroup(gid:String) = Action { request =>
    // Todo: Provide logic to patch a single group
    // Todo: Update group assignment
    Ok
  }

}