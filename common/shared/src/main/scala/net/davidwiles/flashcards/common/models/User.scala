package net.davidwiles.flashcards.common.models

import java.time.ZonedDateTime
import java.util.UUID

case class User(
    id: UUID,
    username: String,
    email: String,
    isActive: Boolean,
    passHash: String,
    dateCreated: ZonedDateTime,
    dateModified: ZonedDateTime
)
