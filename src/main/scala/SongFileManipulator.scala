import java.nio.file.{Files, InvalidPathException, Path, Paths}

import com.mpatric.mp3agic.{ID3v1, ID3v1Genres, ID3v1Tag, Mp3File}

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
