package net.davidwiles.flashcards.api.user

import net.davidwiles.flashcards.api.ServiceError
import net.davidwiles.flashcards.api.engine.UserEngine
import net.davidwiles.flashcards.common.models.User

class UserManager(userEngine: UserEngine):

  def getProfile(userId: String): Either[ServiceError, Option[User]] =
    userEngine.get(userId)
