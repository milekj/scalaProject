import org.json.{JSONException, JSONObject}

class SongLyricsParser(requestResult: String)
{
  private var wasInitialized = false
  private var lyrics: String = _

  def getLyrics: String = {initialize(); lyrics}

  private def initialize()
  {
    if(!wasInitialized)
    {
      try
      {
        lyrics =  new JSONObject(requestResult)
          .getJSONObject("message")
          .getJSONObject("body")
          .getJSONObject("lyrics")
          .getString("lyrics_body")
      }

      catch{case e: JSONException => throw new Exception("Argument is not a valid lyrics date JSON", e)}

      wasInitialized = true
    }
  }
}