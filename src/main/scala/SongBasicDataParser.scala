import org.json.{JSONException, JSONObject}

class SongBasicDataParser(requestResult: String)
{
  private var wasInitialized = false
  private var songJSONData: JSONObject = _
  private var title: String = _
  private var author: String = _

  def getTitle: String = {initialize(); title}
  def getAuthor: String = {initialize(); author}

  private def initialize()
  {
    if (!wasInitialized)
    {
      try
      {
        initializeData()
        initializeAuthor()
        title = songJSONData.getString("title")
      }
      catch
        {case e: JSONException => throw new IllegalArgumentException("Argument passed to BasicDataParser is not a valid song data JSON", e)}

      wasInitialized = true
    }
  }

  private def initializeData()
  {
    songJSONData = new JSONObject(requestResult)
      .getJSONObject("metadata")
      .getJSONArray("music")
      .getJSONObject(0)
  }

  private def initializeAuthor()
  {
    val artistsArray = songJSONData.getJSONArray("artists")
    val authors = new StringBuilder()

    val length = artistsArray.length
    for (i <- 0 until length)
    {
      authors.append(artistsArray.getJSONObject(i).getString("name"))
      if (i != length - 1 && length > 1)
        authors.append(", ")
    }

    author = authors.toString()
  }

}