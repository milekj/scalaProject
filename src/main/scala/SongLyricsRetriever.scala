class SongLyricsRetriever (songID: Int, apiKey: String)
{
  import QueryUtils._
  private val URLText = URLRoot +
    getLyricsQuery +
    idParam +
    encode(songID.toString) +
    keyParam +
    apiKey

  private val retriever = new URLContentRetriever(URLText)
  private val parser = new SongLyricsParser(retriever.content)

  var lyrics: String = _

  try{lyrics = parser.getLyrics}
  catch{case e: Exception => throw new Exception("Failed to retrieve lyrics for " + songID, e)}

}