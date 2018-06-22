import com.acrcloud.utils.ACRCloudRecognizer

class SongBasicDataRetriever(val filePathname: String, config: ACRConfig)
{
  private val recognizer = new ACRCloudRecognizer(config.configMap)
  private val parser: SongBasicDataParser = new SongBasicDataParser(recognizer.recognizeByFile(filePathname, config.startTime))
  var title: String = _
  var author: String = _
 // var id: Int = _
  try
  {
    title = parser.getTitle
    author = parser.getAuthor
   // id = parser.getID
  }

  catch{case e: IllegalAccessException => throw new Exception("Failed to retrieve song data for" + filePathname, e)}

}