package SchoolMealPlanner.model

import scalafx.beans.property.StringProperty
import SchoolMealPlanner.util.Database
import scalikejdbc.*
import scala.util.{Try, Failure}

class User(val nameS: String, val emailS: String, val passwordS: String, val roleS: Role)
  extends Database:

  def this() = this(null, null, null, Role.CafeteriaStaff)

  var name:     StringProperty = StringProperty(nameS)
  var email:    StringProperty = StringProperty(emailS)
  var password: StringProperty = StringProperty(passwordS)
  var avatarPath: StringProperty = StringProperty(null)
  var role: Role = roleS

  private def nameOrEmailPrefix: String =
    Option(name.value).filter(_.trim.nonEmpty)
      .getOrElse(email.value.split("@").headOption.getOrElse("User"))

  def save(): Try[Int] =
    if !isExist then
      Try(DB autoCommit { implicit session =>
        sql"""
          INSERT INTO "user" ("name", email, password, "userType", "avatarPath")
          VALUES (${nameOrEmailPrefix}, ${email.value}, ${password.value}, ${Role.toString(role)}, ${avatarPath.value})
        """.update.apply()
      })
    else
      Try(DB autoCommit { implicit session =>
        sql"""
          UPDATE "user"
          SET "name"      = $nameOrEmailPrefix,
              password    = ${password.value},
              "userType"  = ${Role.toString(role)},
              "avatarPath"= ${avatarPath.value}
          WHERE email = ${email.value}
        """.update.apply()
      })

  def delete(): Try[Int] =
    if isExist then
      Try(DB autoCommit { implicit session =>
        sql"""DELETE FROM "user" WHERE email = ${email.value}""".update.apply()
      })
    else Failure(new Exception("User does not exist in the database"))

  def isExist: Boolean =
    DB readOnly { implicit session =>
      sql"""SELECT 1 AS result FROM "user" WHERE email = ${email.value}"""
        .map(_.int("result")).single().isDefined
    }

object User extends Database:

  def apply(nameS: String, emailS: String, passwordS: String, role: Role): User =
    new User(nameS, emailS, passwordS, role)

  def apply(nameS: String, emailS: String, passwordS: String, userTypeS: String): User =
    new User(nameS, emailS, passwordS, Role.fromString(userTypeS))

  def apply(emailS: String, passwordS: String, role: Role): User =
    new User(null, emailS, passwordS, role)

  def initializeTable(): Unit =
    DB autoCommit { implicit session =>
      val tableExists =
        sql"""
          SELECT COUNT(*) AS count
          FROM SYS.SYSTABLES
          WHERE UPPER(TABLENAME) = 'USER'
        """.map(_.int("count")).single.apply().getOrElse(0) > 0

      if !tableExists then
        sql"""
          CREATE TABLE "user" (
            "name"      VARCHAR(255),
            email       VARCHAR(255) NOT NULL,
            password    VARCHAR(255),
            "userType"  VARCHAR(50),
            "avatarPath" VARCHAR(1024),
            CONSTRAINT pk_user PRIMARY KEY (email)
          )
        """.execute.apply()
      else
        val hasNameColumn =
          sql"""
            SELECT COUNT(*) AS count
            FROM SYS.SYSCOLUMNS c
            JOIN SYS.SYSTABLES t ON c.REFERENCEID = t.TABLEID
            WHERE UPPER(t.TABLENAME) = 'USER' AND UPPER(c.COLUMNNAME) = 'NAME'
          """.map(_.int("count")).single.apply().getOrElse(0) > 0
        if (!hasNameColumn)
          sql"""ALTER TABLE "user" ADD COLUMN "name" VARCHAR(255)""".execute.apply()
        
        val hasAvatarCol =
          sql"""
            SELECT COUNT(*) AS count
            FROM SYS.SYSCOLUMNS c
            JOIN SYS.SYSTABLES t ON c.REFERENCEID = t.TABLEID
            WHERE UPPER(t.TABLENAME) = 'USER' AND UPPER(c.COLUMNNAME) = 'AVATARPATH'
          """.map(_.int("count")).single.apply().getOrElse(0) > 0
        if (!hasAvatarCol)
          sql"""ALTER TABLE "user" ADD COLUMN "avatarPath" VARCHAR(1024)""".execute.apply()
      }

  private def fromRow(rs: WrappedResultSet): User =
    val u = User(
      rs.string("name"),
      rs.string("email"),
      rs.string("password"),
      Role.fromString(rs.string("userType"))
    )
    u.avatarPath.value = rs.stringOpt("avatarPath").orNull
    u

  def getAllUsers: List[User] =
    DB readOnly { implicit session =>
      sql"""SELECT "name", email, password, "userType", "avatarPath" FROM "user""""
        .map(fromRow).list.apply()
    }

  def findByEmailAndPassword(email: String, password: String): Option[User] =
    DB readOnly { implicit session =>
      sql"""SELECT "name", email, password, "userType", "avatarPath"
            FROM "user" WHERE email = $email AND password = $password"""
        .map(fromRow).single.apply()
    }

  def findByEmail(email: String): Option[User] =
    DB readOnly { implicit session =>
      sql"""SELECT "name", email, password, "userType", "avatarPath"
            FROM "user" WHERE email = $email"""
        .map(fromRow).single.apply()
    }