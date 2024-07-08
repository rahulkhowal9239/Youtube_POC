package com.youtubeAPI.business

import akka.stream.Materializer
import akka.stream.scaladsl.Source
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.model.{ChannelListResponse, PlaylistItemListResponse, PlaylistListResponse}
import com.youtubeAPI.utils.JsonFormat
import org.slf4j.LoggerFactory

import scala.collection.concurrent.TrieMap
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters.*

/**
 * Service object to interact with YouTube API for fetching playlist and video details.
 */
class YouTubeVideoService(implicit ec: ExecutionContext, materializer: Materializer) extends JsonFormat{
  private val apiKey = "AIzaSyDlQPR2PSKLa5C8J1XOcsBOibOxuhWQ0Ek"
  private val logger = LoggerFactory.getLogger(this.getClass)
  private val playlistCache: TrieMap[String, List[String]] = TrieMap()
  private val videoCache: TrieMap[String, List[VideoDetail]] = TrieMap()

  /**
   * Creates a YouTube service instance.
   *
   * @return YouTube service instance
   */
  private def createYouTubeService(): YouTube = {
    new YouTube.Builder(
      GoogleNetHttpTransport.newTrustedTransport(),
      JacksonFactory.getDefaultInstance,
      null
    ).setApplicationName("YoutubeStreaming").build()
  }

  /**
   * Fetches all playlist IDs by channel ID, with pagination support.
   *
   * @param channelId The channel ID
   * @return Future containing a list of playlist IDs
   */
  private def fetchAllPlaylistIdsByChannelId(channelId: String): Future[List[String]] = {
    playlistCache.get(channelId) match {
      case Some(playlistIds) => Future.successful(playlistIds)
      case None =>
        def fetchPage(pageToken: Option[String] = None): Future[(List[String], Option[String])] = Future {
          val youtube = createYouTubeService()
          val playlistRequest = youtube.playlists()
            .list("id")
            .setChannelId(channelId)
            .setMaxResults(50)
            .setKey(apiKey)

          pageToken.foreach(playlistRequest.setPageToken)
          val playlistResponse: PlaylistListResponse = playlistRequest.execute()
          val playlists = playlistResponse.getItems
          val nextPageToken = Option(playlistResponse.getNextPageToken)

          val playlistIds = if (playlists != null && !playlists.isEmpty) {
            playlists.asScala.map(_.getId).toList
          } else {
            List.empty[String]
          }

          (playlistIds, nextPageToken)
        }

        def fetchAllPages(pageToken: Option[String] = None, accumulatedIds: List[String] = Nil): Future[List[String]] = {
          fetchPage(pageToken).flatMap { case (ids, nextPageToken) =>
            val allIds = accumulatedIds ++ ids
            nextPageToken match {
              case Some(token) => fetchAllPages(Some(token), allIds)
              case None => Future.successful(allIds)
            }
          }
        }

        fetchAllPages().map { playlistIds =>
          playlistCache.put(channelId, playlistIds)
          playlistIds
        }
    }
  }

  /**
   * Fetches playlist IDs by channel name.
   *
   * @param channelName The channel name
   * @return Future containing a list of playlist IDs
   */
  def fetchPlaylistIdsByChannelName(channelName: String): Future[List[String]] = {
    val youtube = createYouTubeService()
    val channelRequest = youtube.channels()
      .list("contentDetails")
      .setForUsername(channelName)
      .setKey(apiKey)

    Future {
      val channelResponse: ChannelListResponse = channelRequest.execute()
      val channels = channelResponse.getItems

      if (channels != null && !channels.isEmpty) {
        val channelId = channels.get(0).getId
        fetchAllPlaylistIdsByChannelId(channelId)
      } else {
        Future.successful(List.empty[String])
      }
    }.flatMap(identity)
  }

  /**
   * Fetches videos by playlist ID, with pagination support.
   *
   * @param playlistId The playlist ID
   * @return Future containing a list of video details
   */
  private def fetchVideosByPlaylistId(playlistId: String): Future[List[VideoDetail]] = {
    videoCache.get(playlistId) match {
      case Some(videoDetails) => Future.successful(videoDetails)
      case None =>
        def fetchPage(pageToken: Option[String] = None): Future[(List[VideoDetail], Option[String])] = Future {
          val youtube = createYouTubeService()
          val playlistRequest = youtube.playlistItems()
            .list("snippet")
            .setPlaylistId(playlistId)
            .setMaxResults(50)
            .setKey(apiKey)

          pageToken.foreach(playlistRequest.setPageToken)
          val playlistResponse: PlaylistItemListResponse = playlistRequest.execute()
          val items = playlistResponse.getItems
          val nextPageToken = Option(playlistResponse.getNextPageToken)

          val videoDetails = if (items != null && !items.isEmpty) {
            items.asScala.map { item =>
              val videoId = item.getSnippet.getResourceId.getVideoId
              val title = item.getSnippet.getTitle
              VideoDetail(videoId, title)
            }.toList
          } else {
            List.empty[VideoDetail]
          }

          (videoDetails, nextPageToken)
        }

        def fetchAllPages(pageToken: Option[String] = None, accumulatedVideos: List[VideoDetail] = Nil): Future[List[VideoDetail]] = {
          fetchPage(pageToken).flatMap { case (videos, nextPageToken) =>
            val allVideos = accumulatedVideos ++ videos
            nextPageToken match {
              case Some(token) => fetchAllPages(Some(token), allVideos)
              case None => Future.successful(allVideos)
            }
          }
        }

        fetchAllPages().map { videoDetails =>
          videoCache.put(playlistId, videoDetails)
          videoDetails
        }
    }
  }

  /**
   * Fetches videos by channel name.
   *
   * @param channelName  The channel name
   * @return Future containing a response with a list of video details and the count
   */
  def fetchVideosByChannelName(channelName: String): Future[VideosResponse] = {
    fetchPlaylistIdsByChannelName(channelName).flatMap { playlistIds =>
      val videoFutures = playlistIds.map(fetchVideosByPlaylistId)

      Source(videoFutures)
        .mapAsyncUnordered(10)(identity)
        .runFold(List.empty[VideoDetail])((acc, videos) => acc ++ videos)
        .map { videos =>
          val uniqueVideos = videos.distinct
          VideosResponse(uniqueVideos, uniqueVideos.size)
        }
    }
  }
}
