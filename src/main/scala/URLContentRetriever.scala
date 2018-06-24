import java.io.{BufferedReader, IOException, InputStreamReader}
import java.net.{MalformedURLException, URL}

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