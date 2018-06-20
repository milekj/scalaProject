import java.io.{BufferedReader, IOException, InputStreamReader}
import java.net.{MalformedURLException, URL, URLEncoder}
import java.nio.file._
import java.util

import com.acrcloud.utils.ACRCloudRecognizer
import com.mpatric.mp3agic.{ID3v1, ID3v1Genres, ID3v1Tag, Mp3File}
import org.json.{JSONException, JSONObject}

import scala.collection.JavaConverters.iterableAsScalaIterableConverter

class ACRConfig(host: String, accessKey: String, accessSecret: String, timeout: Int, val startTime: Int)
{
  def configMap: util.HashMap[String, Object] =
  {
    val config = new util.HashMap[String, Object]()
    config.put("host", host)
    config.put("access_key", accessKey)
    config.put("access_secret", accessSecret)
    config.put("debug", false.asInstanceOf[Object])
    config.put("timeout", timeout.asInstanceOf[Object])
    config
  }

}

class SongBasicDataRetriever(val filePathname: String, config: ACRConfig)
{
  private val recognizer = new ACRCloudRecognizer(config.configMap)
  private val parser: SongBasicDataParser = new SongBasicDataParser(recognizer.recognizeByFile(filePathname, config.startTime))
  var title: String = _
  var author: String = _

  try
  {
    title = parser.getTitle
    author = parser.getAuthor
  }

  catch{case e: IllegalAccessException => throw new Exception("Failed to retrieve song data for" + filePathname, e)}

}

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

class SongFileManipulator(basicRetriever: SongBasicDataRetriever, detailsRetriever: SongDetailsRetriever) {
  private val filePathname = basicRetriever.filePathname
  private var path: Path = _

  try {path = Paths.get(filePathname)}
  catch {case e: InvalidPathException => throw new IllegalArgumentException(filePathname + " is not a valid path", e)}

  def rename(): Path = {
    val author = basicRetriever.author
    val title = basicRetriever.title
    val newPath = path.resolveSibling(title + " - " + author + fileExtension)
    try {Files.move(path, newPath); newPath}
    catch {case e: Throwable => throw new Exception("Failed to rename a file", e)}
  }

  def setID3Tags()
  {
    try
    {
      val mp3File = new Mp3File(filePathname)
      val tag = getOrCreateTag(mp3File)
      setTagAttributes(tag)
      val tmpName = filePathname + "$"
      mp3File.save(tmpName)
      deleteFileAndRenameTMP(tmpName)
    }

    catch{case e: Throwable => throw new Exception("Failed to set ID3 tags", e)}

  }

  private def getOrCreateTag(mp3File: Mp3File): ID3v1 =
  {
    var tag: ID3v1 = new ID3v1Tag()
    if (!mp3File.hasId3v1Tag)
      mp3File.setId3v1Tag(tag)
    else
      tag = mp3File.getId3v1Tag
    tag
  }

  private def setTagAttributes(tag: ID3v1)
  {
    tag.setTitle(basicRetriever.title)
    tag.setArtist(basicRetriever.author)
    tag.setAlbum(detailsRetriever.album)
    tag.setGenre(ID3v1Genres.matchGenreDescription(detailsRetriever.genre))
  }

  private def deleteFileAndRenameTMP(pathnameToRename: String)
  {
    Files.delete(path)
    val pathToRename = Paths.get(pathnameToRename)
    path = Files.move(pathToRename, pathToRename.resolveSibling(filePathname))
  }

  private def fileExtension = {val dotIndex = filePathname.lastIndexOf('.'); filePathname.substring(dotIndex)}
}

class URLContentRetriever(URLText: String)
{
  private var wasInitialized = false
  private var URL: URL =  _
  private val result = new StringBuilder()

  try {URL = new URL(URLText)}
  catch {case e:MalformedURLException => throw new IllegalArgumentException(URLText + " is not a valid URL", e)}

  def content: String = {initialize(); result.toString()}

  private def initialize()
  {
    if (!wasInitialized)
    {
      try
      {
        val linesStream = new BufferedReader(new InputStreamReader(URL.openStream()))
        var line = ""

        while (line != null)
        {
          line = linesStream.readLine()
          result.append(line)
        }
      }
      catch
      {case e: IOException => throw new Exception("Failed to read data from URL", e)}

      wasInitialized = true
    }
  }

}

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

}

class SongDetailsParser (requestResult: String)
{
  private var wasInitialized = false
  private var JSONData: JSONObject = _
  private var id: Int = _
  private var rating: Int = _
  private var album: String = _
  private var genre: String = _

  def getID: Int = {initialize(); id}
  def getRating: Int = {initialize(); rating}
  def getAlbum: String = {initialize(); album}
  def getGenre: String = {initialize(); genre}


  private def initialize()
  {
    if(!wasInitialized)
    {
      try
      {
        initializeData()
        id = JSONData.getInt("track_id")
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

}

class DirectoryManipulator (directoryPathname: String, config: ACRConfig, APIKey: String)
{
  private var directory: Path = _
  private var wasInitialized = false
  private val basicDataMap = new util.HashMap[Path, SongBasicDataRetriever]()
  private val detailsMap = new util.HashMap[Path, SongDetailsRetriever]()

  try{directory = Paths.get(directoryPathname)}
  catch{case e: InvalidPathException => throw new IllegalArgumentException(directoryPathname + " is not a valid path",e)}

  def applyOnFile(func: (Path, SongBasicDataRetriever, SongDetailsRetriever) => Unit)
  {
    val dirStream = getDirectoryStream
    initialize()
    for (filePath <- dirStream.asScala)
    {
      try
      {
        val basicRetriever  = basicDataMap.get(filePath)
        val detailsRetriever = detailsMap.get(filePath)
        func(filePath, basicRetriever, detailsRetriever)

      }
      catch {case _: Throwable => }

    }
  }

  def applyOnDirectory(func: (util.Collection[SongBasicDataRetriever], util.Collection[SongDetailsRetriever]) => Unit): Unit =
  {
    try{func(basicDataMap.values, detailsMap.values)}
    catch{case e: Throwable => throw new Exception("Failed to perform operation on directory", e)}
  }

  private def initialize()
  {
    if (!wasInitialized)
    {
      val dirStream = getDirectoryStream
      for (dir <- dirStream.asScala)
      {
        try
        {
          val basicRetriever = new SongBasicDataRetriever(dir.toString, config)
          basicDataMap.put(dir, basicRetriever)
          detailsMap.put(dir, new SongDetailsRetriever(basicRetriever, APIKey))
        }

        catch{case _: Throwable =>  }
      }

      wasInitialized = true
    }
  }

  private def getDirectoryStream: DirectoryStream[Path] =
  {
    try
    {
      Files.newDirectoryStream(directory)
    }
    catch
      {
        case e: NotDirectoryException => throw new IllegalArgumentException(directoryPathname + " is not a directory", e)
        case e: Throwable => throw new Exception("Failed to read contents of the directory", e)
      }

  }

}

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

object QueryUtils
{
  def encode(s: String): String = {URLEncoder.encode(s, "UTF-8")}
  val URLRoot = "http://api.musixmatch.com/ws/1.1/"
  val keyParam = "&apikey="
  val searchSongQuery = "track.search?"
  val getLyricsQuery = "track.lyrics.get?"
  val titleParam = "&q_track="
  val authorParam = "&q_artist="
  val idParam = "&track_id="
  val sortByRatingParam = "&s_track_rating=desc"
}

object FileManipulatorUtils
{
  def rename(p: Path, b: SongBasicDataRetriever, d: SongDetailsRetriever): Path =
  {
    val z = new SongFileManipulator(b, d)
    z.rename()
  }

  def printLyrics(p: Path, b: SongBasicDataRetriever, d: SongDetailsRetriever)
  {
    val x = new SongLyricsRetriever(d.id, "7a23fab73af11b37110c268a87ac3a57")
    System.out.println(x.lyrics)
  }

  def setTags(p: Path, b: SongBasicDataRetriever, d: SongDetailsRetriever)
  {
    val x = new SongFileManipulator(b, d)
    x.setID3Tags()
  }
}

object DirectoryManipulatorUtils
{
  def printAverage(b: util.Collection[SongBasicDataRetriever], d: util.Collection[SongDetailsRetriever])
  {
    var a = 0
    var i = 0
    var r: Double = 0

    for (e <- d.asScala) {
      a += e.rating
      i += 1
    }

    if (i == 0)
      r = 0
    else
      r = a / i
    println(r)
  }
}

object main extends App
{
  val c = new ACRConfig("identify-eu-west-1.acrcloud.com", "a10f5c60f5e25167c65853b0bf8748ef", "2QRYUcccB6qsOJvNt1AmU3qFwhU3WkQZGohvPDZP", 10, 60)
  val m = new DirectoryManipulator("D:\\nuty", c, "7a23fab73af11b37110c268a87ac3a57")
  m.applyOnFile(FileManipulatorUtils.setTags)
  m.applyOnFile(FileManipulatorUtils.printLyrics)
  m.applyOnDirectory(DirectoryManipulatorUtils.printAverage)
  m.applyOnFile(FileManipulatorUtils.rename)

}
