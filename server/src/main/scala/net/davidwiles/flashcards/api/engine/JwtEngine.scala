package net.davidwiles.flashcards.api.engine

import com.typesafe.scalalogging.LazyLogging
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import net.davidwiles.flashcards.api.ServiceError

import java.nio.charset.StandardCharsets
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.util.Base64
import java.util.Date
import scala.util.Failure
import scala.util.Success
import scala.util.Try
import scala.util.chaining.*

trait JwtEngine:

  def sign(subject: String, expiration: Long = 604800000): Either[ServiceError, String]

  def verify(token: String): Either[ServiceError, Claims]

class JwtEngineImpl(pub: PublicKey, priv: PrivateKey) extends JwtEngine with LazyLogging:

  override def sign(subject: String, expiration: Long = 604800000): Either[ServiceError, String] =
    Try {
      Jwts
        .builder()
        .subject(subject)
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(priv)
        .compact()
    } match {
      case Failure(exception) =>
        Left(ServiceError.JwtSignError("unable to sign jwt", Some(exception)))
      case Success(value) => Right(value)
    }

  override def verify(token: String): Either[ServiceError, Claims] =
    Try {
      Jwts
        .parser()
        .verifyWith(pub)
        .build()
        .parseSignedClaims(token)
        .getPayload
    } match {
      case Failure(_: ExpiredJwtException) =>
        Left(ServiceError.JwtExpiredError("token expired"))
      case Failure(exception) =>
        logger.error("unable to verify jwt")
        Left(ServiceError.JwtVerifyError(exception.getMessage, Some(exception)))
      case Success(value) => Right(value)
    }

object JwtEngine:

  private val kf = KeyFactory.getInstance("RSA")

  private val b64 = Base64.getDecoder

  private def parsePrivateKey(text: String): PrivateKey =
    text
      .replace("\n", "")
      .replace("-----BEGIN PRIVATE KEY-----", "")
      .replace("-----END PRIVATE KEY-----", "")
      .getBytes(StandardCharsets.UTF_8)
      .pipe(b64.decode)
      .pipe(new PKCS8EncodedKeySpec(_))
      .pipe(kf.generatePrivate)

  private def parsePublicKey(bytes: Array[Byte]): PublicKey =
    bytes
      .pipe(new X509EncodedKeySpec(_))
      .pipe(kf.generatePublic)

  def apply(publicKey: Array[Byte], privateKey: String): JwtEngine =
    new JwtEngineImpl(parsePublicKey(publicKey), parsePrivateKey(privateKey))
