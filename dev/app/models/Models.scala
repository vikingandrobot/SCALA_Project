package models

import java.sql.Timestamp

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

case class Event(
  id: Option[Long],
  title: String,
  description: String,
  price: Float,
  address: String,
  region: String,
  startDate: Timestamp,
  endDate: Timestamp,
  scheduleDescription: String,
  userId: Long,
  organizationId: Long)