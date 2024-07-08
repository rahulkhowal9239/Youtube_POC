package com.youtubeAPI.utils

import spray.json.{DefaultJsonProtocol, RootJsonFormat}

/**
 * Object to define JSON formats for case classes.
 * This ensures the case classes can be serialized and deserialized to and from JSON.
 */
trait JsonFormat extends DefaultJsonProtocol {

  /**
   * Case class to represent details of a video.
   * @param videoId The unique identifier of the video.
   * @param title The title of the video.
   */
  final case class VideoDetail(videoId: String, title: String)

  /**
   * Case class to represent a response containing a list of videos and a count of the total number of videos.
   * @param videos A list of VideoDetail objects.
   * @param count The total number of videos.
   */
  final case class VideosResponse(videos: List[VideoDetail], count: Int)
  

  /**
   * Case class to represent the snippet of a transcript item.
   * @param language The language of the transcript.
   * @param name The name of the transcript.
   * @param isDraft Indicates if the transcript is a draft.
   */
  final case class TranscriptSnippet(language: String, name: String, isDraft: Boolean)

  /**
   * Case class to represent a transcript item.
   * @param id The ID of the transcript.
   * @param snippet The snippet details of the transcript.
   */
  final case class TranscriptItem(id: String, snippet: TranscriptSnippet)

  /**
   * Case class to represent a response containing a list of transcript items.
   * @param items A list of TranscriptItem objects.
   */
  final case class TranscriptsResponse(items: List[TranscriptItem])

  /**
   * Implicit JSON format for VideoDetail case class.
   */
  implicit val videoDetailFormat: RootJsonFormat[VideoDetail] = jsonFormat2(VideoDetail)

  /**
   * Implicit JSON format for VideosResponse case class.
   */
  implicit val videosResponseFormat: RootJsonFormat[VideosResponse] = jsonFormat2(VideosResponse)
  
  /**
   * Implicit JSON format for TranscriptSnippet case class.
   */
  implicit val transcriptSnippetFormat: RootJsonFormat[TranscriptSnippet] = jsonFormat3(TranscriptSnippet)

  /**
   * Implicit JSON format for TranscriptItem case class.
   */
  implicit val transcriptItemFormat: RootJsonFormat[TranscriptItem] = jsonFormat2(TranscriptItem)

  /**
   * Implicit JSON format for TranscriptsResponse case class.
   */
  implicit val transcriptsResponseFormat: RootJsonFormat[TranscriptsResponse] = jsonFormat1(TranscriptsResponse)
}
