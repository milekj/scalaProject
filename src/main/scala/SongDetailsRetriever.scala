class SongDetailsRetriever(title: String, author: String, apiKey: String)
{
  def this(basicRetriever: SongBasicDataRetriever, apiKey: String)
  {
    this(basicRetriever.title, basicRetriever.author, apiKey)
  }

  import QueryUtils._
  private val URLText = URLRoot +
    searchSongQuery +
    titleParam +
    encode(title) +
    authorParam +
    encode(author) +
    sortByRatingParam +
    keyParam +
    apiKey

  private var retriever: URLContentRetriever = _

  try{retriever = new URLContentRetriever(URLText)}
  catch
    {
      case e: IllegalArgumentException =>
        throw new Exception("Failed to retrieve details for: " + title + ", " + author , e)
    }

  private val parser = new SongDetailsParser(retriever.content)

  val id: Int = parser.getID
  val rating: Int = parser.getRating
  val album: String = parser.getAlbum
  val genre: String = parser.getGenre
  val favourite: Int = parser.getFavourite
}