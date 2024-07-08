package com.youtubeAPI

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.settings.ServerSettings
import akka.stream.ActorMaterializer
import com.youtubeAPI.business.{YouTubeVideoService, YoutubeTranscriptService}
import com.youtubeAPI.routes.YouTubeRoutes

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.io.StdIn

/**
 * Main server object to handle YouTube API requests.
 */
object YouTubeApiServer {

  implicit val system: ActorSystem = ActorSystem("youtube")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  /**
   * Main method to start the HTTP server.
   * @param args Command line arguments.
   */
  def main(args: Array[String]): Unit = {
    val youTubeVideoService = new YouTubeVideoService()
    val youTubeTranscriptService = new YoutubeTranscriptService

    val youTubeRoutes = new YouTubeRoutes(youTubeVideoService, youTubeTranscriptService)

    val serverSettings = ServerSettings(system).withTimeouts(ServerSettings(system).timeouts.withRequestTimeout(120.seconds))

    val bindingFuture = Http().newServerAt("localhost", 8080).withSettings(serverSettings).bind(youTubeRoutes.routes)

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}
