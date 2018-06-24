import java.nio.file._
import java.util

import scala.collection.JavaConverters.iterableAsScalaIterableConverter
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
      Thread.sleep(300)

      }
      catch {case _: Throwable => }

    }
  }


  def applyOnDirectory(func: (Path, util.Map[Path, SongBasicDataRetriever], util.Map[Path, SongDetailsRetriever]) => Unit): Unit =
  {
    initialize()
    try{func(directory, basicDataMap, detailsMap)}
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
          val basicRetriever = new SongBasicDataRetriever(dir.toString(), config)
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