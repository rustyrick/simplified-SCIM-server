package models

import anorm._
import anorm.SqlParser._
import java.sql.Connection
import java.util.Calendar

case class Group(id:String, created:Calendar)
