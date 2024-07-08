package com.youtubeAPI.business

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.*
import akka.stream.{Materializer}
import io.circe.generic.auto.*
import io.circe.parser.decode
import com.github.scribejava.apis.GoogleApi20
import com.github.scribejava.core.builder.ServiceBuilder
import com.github.scribejava.core.model.OAuth2AccessToken
import com.github.scribejava.core.oauth.OAuth20Service
import com.youtubeAPI.utils.JsonFormat

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
import scala.io.StdIn

class YoutubeTranscriptService(implicit ec: ExecutionContext, system: ActorSystem, materializer: Materializer) extends JsonFormat {

  private val clientId = ""
  private val clientSecret = ""
  private val redirectUri = "urn:ietf:wg:oauth:2.0:oob"

  /**
   * OAuth 2.0 service for managing authentication with YouTube API.
   */
  private val service: OAuth20Service = new ServiceBuilder(clientId)
    .apiSecret(clientSecret)
    .defaultScope("https://www.googleapis.com/auth/youtube.force-ssl")
    .callback(redirectUri)
    .build(GoogleApi20.instance())

  /**
   * Fetches the OAuth2 access token by guiding the user through the authorization process.
   *
   * @return The access token for making authenticated API requests.
   */
  def fetchAccessToken: OAuth2AccessToken = {
    val authorizationUrl = service.getAuthorizationUrl
    println(s"Go to the following URL and authorize the app: $authorizationUrl")
    println("Enter the authorization code:")
    val code = StdIn.readLine()
    service.getAccessToken(code)
  }

  /**
   * Fetches the list of transcripts for a given video ID.
   *
   * @param videoId     The ID of the video for which to fetch transcripts.
   * @param accessToken The OAuth2 access token for authenticated API requests.
   * @return A future containing a list of transcript items.
   */
  def fetchTranscriptsList(videoId: String): Future[List[TranscriptItem]] = {
    val url = s"https://www.googleapis.com/youtube/v3/captions?part=snippet&videoId=$videoId"
    val request = HttpRequest(uri = url).withHeaders(headers.Authorization(headers.OAuth2BearerToken(fetchAccessToken.getAccessToken)))
    val responseFuture = Http().singleRequest(request)

    responseFuture.flatMap { response =>
      response.status match {
        case StatusCodes.OK =>
          response.entity.dataBytes.runFold("")(_ ++ _.utf8String).map { entity =>
            decode[TranscriptsResponse](entity) match {
              case Right(transcriptsResponse) => transcriptsResponse.items
              case Left(error) => throw new Exception(s"Failed to parse transcripts list: $error")
            }
          }
        case StatusCodes.Forbidden =>
          Future.failed(new Exception("Access forbidden: ensure the video allows third-party transcripts and the OAuth2 token has the correct scope"))
        case StatusCodes.NotFound =>
          Future.failed(new Exception("Video not found: ensure the video ID is correct"))
        case _ =>
          response.entity.dataBytes.runFold("")(_ ++ _.utf8String).flatMap { entity =>
            Future.failed(new Exception(s"Failed to fetch transcripts list: $entity"))
          }
      }
    }
  }

  /**
   * Downloads the transcript for a given transcript ID in SRT format.
   *
   * @param transcriptId The ID of the transcript to download.
   * @param accessToken  The OAuth2 access token for authenticated API requests.
   * @return A future containing the transcript content in SRT format.
   */
  def downloadTranscript(transcriptId: String): Future[String] = {
    val url = s"https://www.googleapis.com/youtube/v3/captions/$transcriptId?tfmt=srt"
    val request = HttpRequest(uri = url).withHeaders(headers.Authorization(headers.OAuth2BearerToken(fetchAccessToken.getAccessToken)))
    val responseFuture = Http().singleRequest(request)

    responseFuture.flatMap { response =>
      response.status match {
        case StatusCodes.OK =>
          response.entity.dataBytes.runFold("")(_ ++ _.utf8String)
        case StatusCodes.Forbidden =>
          Future.failed(new Exception("Access forbidden: ensure the video allows third-party transcripts and the OAuth2 token has the correct scope"))
        case StatusCodes.NotFound =>
          Future.failed(new Exception("Transcript not found: ensure the transcript ID is correct"))
        case _ =>
          response.entity.dataBytes.runFold("")(_ ++ _.utf8String).flatMap { entity =>
            Future.failed(new Exception(s"Failed to download transcript: $entity"))
          }
      }
    }
  }
}
