package net.davidwiles.flashcards.common.api

import java.time.ZonedDateTime
import java.util.UUID

case class UserProfile(id: UUID, username: String, email: String, dateCreated: ZonedDateTime)
