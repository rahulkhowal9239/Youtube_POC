# YouTube API Server

This project is an Akka HTTP-based server for interacting with the YouTube API. It provides routes for fetching playlists, videos, and transcripts from YouTube.

## Features

- Fetch playlists by channel name
- Fetch videos by channel name
- Fetch transcripts for a given video ID
- Download transcript in SRT format for a given transcript ID

## Technologies Used

- Scala
- Akka HTTP
- Akka Streams
- Google YouTube API
- Circe for JSON parsing
- ScribeJava for OAuth2 authentication
- Spray JSON for JSON marshalling/unmarshalling

## Setup Instructions

### Prerequisites

- Scala
- sbt (Scala Build Tool)
- Google Developer account with YouTube Data API enabled

### Configuration

1. Clone the repository:

    ```sh
    git clone <[repository-url](https://github.com/rahulkhowal9239/Youtube_POC)>
    cd <Youtube_POC>
    ```

2. Create a `src/main/resources/application.conf` file with the following content:

    ```hocon
    youtube {
      apiKey = "YOUR_YOUTUBE_API_KEY"
      oauth {
        clientId = "YOUR_CLIENT_ID"
        clientSecret = "YOUR_CLIENT_SECRET"
        redirectUri = "urn:ietf:wg:oauth:2.0:oob"
      }
    }
    ```

3. Replace `YOUR_YOUTUBE_API_KEY`, `YOUR_CLIENT_ID`, and `YOUR_CLIENT_SECRET` with your actual YouTube API key, OAuth client ID, and client secret.

### Running the Server

1. Start the server using sbt:

    ```sh
    sbt run
    ```

2. The server will start and listen on `http://localhost:8080`.

### API Endpoints

1. **Fetch Playlists by Channel Name**

    - **Endpoint:** `/fetchPlaylist`
    - **Method:** GET
    - **Parameters:** `channelName` (String)
    - **Response:** List of playlist IDs

    ```sh
    curl "http://localhost:8080/fetchPlaylist?channelName=thescriptlab"
    ```

2. **Fetch Videos by Channel Name**

    - **Endpoint:** `/fetchVideosByChannelName`
    - **Method:** GET
    - **Parameters:** `channelName` (String)
    - **Response:** JSON object containing a list of video details and the count

    ```sh
    curl "http://localhost:8080/fetchVideosByChannelName?channelName=thescriptlab"
    ```

3. **Fetch Transcripts**

    - **Endpoint:** `/fetchTranscripts`
    - **Method:** GET
    - **Parameters:** `videoId` (String)
    - **Response:** List of transcript items

    ```sh
    curl "http://localhost:8080/fetchTranscripts?videoId=ScyLDY8MgXc"
    ```

4. **Download Transcript**

    - **Endpoint:** `/downloadTranscript`
    - **Method:** GET
    - **Parameters:** `transcriptId` (String)
    - **Response:** Transcript content in SRT format

    ```sh
    curl "http://localhost:8080/downloadTranscript?transcriptId=YOUR_TRANSCRIPT_ID"
    ```
