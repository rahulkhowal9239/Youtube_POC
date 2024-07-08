package com.youtubeAPI.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport.*
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import com.youtubeAPI.business.{YouTubeVideoService, YoutubeTranscriptService}
import com.youtubeAPI.utils.JsonFormat
import spray.json.*

/**
 * Object to define routes for YouTube API requests.
 */
class YouTubeRoutes(youTubeVideoService: YouTubeVideoService,
                    youTubeTranscriptService: YoutubeTranscriptService
                   ) extends JsonFormat {

  /**
   * Defines the routes for the YouTube API server.
   *
   * @return The defined routes.
   */
  def routes: Route =
    path("fetchPlaylist") {
      parameters("channelName") { channelName =>
        onComplete(youTubeVideoService.fetchPlaylistIdsByChannelName(channelName)) {
          case util.Success(ids) => complete(ids.toJson)
          case util.Failure(ex) => complete(StatusCodes.InternalServerError -> ex.getMessage)
        }
      }
    } ~
      path("fetchVideosByChannelName") {
        parameters("channelName") { channelName =>
          onComplete(youTubeVideoService.fetchVideosByChannelName(channelName)) {
            case util.Success(videosResponse) => complete(videosResponse.toJson)
            case util.Failure(ex) => complete(StatusCodes.InternalServerError -> ex.getMessage)
          }
        }
      } ~
      path("fetchTranscripts") {
        parameters("videoId") { videoId =>
          onComplete(youTubeTranscriptService.fetchTranscriptsList(videoId)) {
            case scala.util.Success(transcripts) => complete(transcripts.toJson)
            case scala.util.Failure(ex) => complete(s"Failed to fetch transcripts: ${ex.getMessage}")
          }
        }
      } ~
      path("downloadTranscript") {
        parameters("transcriptId") { transcriptId =>
          onComplete(youTubeTranscriptService.downloadTranscript(transcriptId)) {
            case scala.util.Success(transcript) => complete(transcript)
            case scala.util.Failure(ex) => complete(s"Failed to download transcript: ${ex.getMessage}")
          }
        }
      }
}
