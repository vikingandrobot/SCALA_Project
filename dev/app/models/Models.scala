package models

// Class User (with optional id for new user)
case class User(
   id: Option[Long],
   firstName: String,
   lastName: String,
   email: String,
   username: String,
   password: String)