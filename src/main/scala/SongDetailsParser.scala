import org.json.{JSONException, JSONObject}

class SongDetailsParser (requestResult: String)
{
  private var wasInitialized = false
  private var JSONData: JSONObject = _
  private var id: Int = _
  private var rating: Int = _
  private var album: String = _
  private var genre: String = _
  private var favourite: Int = _

  def getID: Int = {initialize(); id}
  def getRating: Int = {initialize(); rating}
  def getAlbum: String = {initialize(); album}
  def getGenre: String = {initialize(); genre}
  def getFavourite: Int = {initialize(); favourite}

  private def initialize()
  {
    if(!wasInitialized)
    {
      try
      {
        initializeData()
        id = JSONData.getInt("track_id")
        initializeFavourite()
        initializeRating()
        album = JSONData.getString("album_name")
        initializeGenre()
      }
      catch
        {case e:JSONException => throw new IllegalArgumentException("Argument is not a valid song data JSON", e)}

      wasInitialized = true
    }
  }

  private def initializeData()
  {
    JSONData = new JSONObject(requestResult)
      .getJSONObject("message")
      .getJSONObject("body")
      .getJSONArray("track_list")
      .getJSONObject(0)
      .getJSONObject("track")
  }

  private def initializeGenre()
  {
    try
    {
      genre = JSONData
        .getJSONObject("primary_genres")
        .getJSONArray("music_genre_list")
        .getJSONObject(0)
        .getJSONObject("music_genre")
        .getString("music_genre_name")
    }

    catch
      {case _: JSONException => genre = ""}
  }

  private def initializeRating()
  {
    try{rating = JSONData.getInt("track_rating")}
    catch{case _: JSONException => rating = 0}
  }

  private def initializeFavourite()
  {
    try{rating = JSONData.getInt("num_favourite")}
    catch{case _: JSONException => favourite = 0}
  }

}