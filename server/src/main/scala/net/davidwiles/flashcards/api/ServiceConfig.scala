package net.davidwiles.flashcards.api

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

import java.nio.file.Files
import java.nio.file.Paths

class ServiceConfig(val config: Config = ConfigFactory.load()):

  def privateKey(): String =
    Files.readString(Paths.get(config.getString("jwt.privateKey")))

  def publicKey(): Array[Byte] =
    Files.readAllBytes(Paths.get(config.getString("jwt.publicKey")))
