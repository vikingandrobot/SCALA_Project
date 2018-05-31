package models


// Class User (with optional id for new user)
case class User(
   id: Option[Long],
   firstName: String,
   lastName: String,
   email: String,
   username: String,
   password: String)

case class Theme(
  id: Option[Long],
  name: String)


case class Interest(
  id: Option[Long],
  userId: Long,
  themeId: Long)

case class Organization(
  id: Option[Long],
  organizationType: String,
  name: String,
  address: String)

case class UserOrganization(
  id: Option[Long],
  userId: Long,
  organizationId: Long)