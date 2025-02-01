package net.davidwiles.flashcards.common.api

case class SignupRequest(username: String, email: String, password: String)

case class SignupResponse(token: String)

case class LoginRequest(username: String, password: String)

case class LoginResponse(token: String)

case class KeepaliveResponse(token: String)
