import java.net.URLEncoder
import java.nio.file._
import java.util
import java.io.PrintWriter

import scala.collection.JavaConverters._

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
  //val sortByRatingParam = "&s_num_favourite=desc"
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

  def saveLyrics(p: Path, b: SongBasicDataRetriever, d: SongDetailsRetriever)
  {
    val x = new SongLyricsRetriever(d.id, "7a23fab73af11b37110c268a87ac3a57")

    val file_name = p.resolveSibling("")+"\\"+ b.title + " - " + b.author + " (Lyrics).txt"   //tmp.path??????????
    val writer = new PrintWriter(file_name, "UTF-8")

    val lines = x.lyrics.split("\n")
    for (line <- lines.take(lines.length-3)) writer.println(line)
    writer.close()
  }
}

object DirectoryManipulatorUtils
{
  def printAverage(p: Path, b: util.Map[Path, SongBasicDataRetriever], d: util.Map[Path, SongDetailsRetriever])
  {

    var a = 0
    var i = 0
    var r: Double = 0

    for (e <- d.values.asScala) {
      a += e.rating
      i += 1
    }

    if (i == 0) r = 0
    else r = a / i
    println(r)
  }


  def statictics(p :Path, b: util.Map[Path, SongBasicDataRetriever], d: util.Map[Path, SongDetailsRetriever])
  {
    val file_name = p.toString+"\\Statistics.txt"
    val writer = new PrintWriter(file_name, "UTF-8")

    val genre_counter = scala.collection.mutable.Map[String,Int]()
    val author_counter = scala.collection.mutable.Map[String,Int]()
    val album_counter = scala.collection.mutable.Map[String,Int]()
    val author_rating = scala.collection.mutable.Map[String,Int]()
    val song_rating = scala.collection.mutable.Map[String,Int]()
    val genre_rating = scala.collection.mutable.Map[String,Int]()
    val album_rating = scala.collection.mutable.Map[String,Int]()



    for (e <- d.values.asScala ) {
      if (genre_counter.contains(e.genre)){
        var i = genre_counter.get(e.genre).get + 1
        genre_counter.put(e.genre, i)
      }
      else genre_counter.put(e.genre, 1)
    }
    writer.println("Most common genre: "+ genre_counter.maxBy(_._2)._1 + " (" + genre_counter.maxBy(_._2)._2 + ")")


    for (e <- b.values.asScala ) {
      if (author_counter.contains(e.author)){
        var i = author_counter.get(e.author).get + 1
        author_counter.put(e.author, i)
      }
      else author_counter.put(e.author, 1)
    }
    writer.println("Most common author: " + author_counter.maxBy(_._2)._1 + " (" + author_counter.maxBy(_._2)._2 + ")")


    for (e <- d.values.asScala ) {
      if (album_counter.contains(e.album)){
        var i = album_counter.get(e.album).get + 1
        album_counter.put(e.album, i)
      }
      else album_counter.put(e.album, 1)
    }
    writer.println("Most common album: " + album_counter.maxBy(_._2)._1 + " (" + album_counter.maxBy(_._2)._2 + ")")


    for ( e <- d.asScala ) {
      if (author_rating.contains(b.get(e._1).author)){
        val i = author_rating.get(b.get(e._1).author).get + e._2.rating
        println("fav = "+e._2.rating)
        author_rating.put(b.get(e._1).author, i)
      }
      else {
        author_rating.put(b.get(e._1).author, e._2.rating)
        println("fav = "+e._2.rating)
      }
    }
    for (e <- author_rating )
      if(author_counter.get(e._1).get != 0) author_rating.put(e._1, e._2/author_counter.get(e._1).get)
    for (e <- author_rating ) println(e)

    writer.println("Best rated author: " + author_rating.maxBy(_._2)._1 + " (" + author_rating.maxBy(_._2)._2 + ")")
    writer.println("Worst rated author: " + author_rating.minBy(_._2)._1 + " (" + author_rating.minBy(_._2)._2 + ")")


    var max = 0
    var min = 100
    var title_max = ""
    var title_min = ""
    for (e <- d.asScala ) {
      println("song = " + b.get(e._1).title + " rating = " + e._2.rating)
      if (e._2.rating > max){
        max = e._2.rating
        title_max = b.get(e._1).title
      }
      if (e._2.rating < min){
        min = e._2.rating
        title_min = b.get(e._1).title
      }
    }
    writer.println("Best rated song: " + title_max + " (" + max + ")")
    writer.println("Worst rated song: " + title_min + " (" + min + ")")


    for (e <- d.values.asScala ) {
      if (genre_rating.contains(e.genre)){
        var i = genre_rating.get(e.genre).get + e.rating
        genre_rating.put(e.genre, i)
      }
      else genre_rating.put(e.genre, e.rating)
    }
    for (e <- genre_rating )
      if(genre_counter.get(e._1).get != 0) genre_rating.put(e._1, e._2/genre_counter.get(e._1).get)

    writer.println("Best rated genre: " + genre_rating.maxBy(_._2)._1 + " (" + genre_rating.maxBy(_._2)._2 + ")")
    writer.println("Worst rated genre: " + genre_rating.minBy(_._2)._1 + " (" + genre_rating.minBy(_._2)._2 + ")")


    for (e <- d.values.asScala ) {
      if (album_rating.contains(e.album)){
        var i = album_rating.get(e.album).get + e.rating
        album_rating.put(e.album, i)
      }
      else album_rating.put(e.album, e.rating)
    }
    for (e <- album_rating )
      if(album_counter.get(e._1).get != 0) album_rating.put(e._1, e._2/album_counter.get(e._1).get)

    writer.println("Best rated album: " + album_rating.maxBy(_._2)._1 + " (" + album_rating.maxBy(_._2)._2 + ")")
    writer.println("Worst rated album: " + album_rating.minBy(_._2)._1 + " (" + album_rating.minBy(_._2)._2 + ")")

    var av = 0
    var i = 0
    var r: Double = 0

    for (e <- d.values.asScala) {
      av += e.rating
      i += 1
    }

    if (i == 0) r = 0
    else r = av / i
    writer.println("Average rating: " + r)


    writer.close()
  }

}

class RandomQuoteRetriever {
  var quote = ""

  def randomQuote(p: Path, b: util.Map[Path, SongBasicDataRetriever], d: util.Map[Path, SongDetailsRetriever]) {
    val r = scala.util.Random
    if (d.values().size() > 0) {
      val r1 = r.nextInt(d.values().size()) //r1 - choosing song
      var i = 0
      for (e <- d.values.asScala) {
        if (i == r1) {
          val x = new SongLyricsRetriever(e.id, "7a23fab73af11b37110c268a87ac3a57")
          val lines = x.lyrics.split("\n")
          while (quote == "") {
            if ((lines.length - 4) > 0) {
              var r2 = r.nextInt(lines.length - 4)
              quote = lines(r2) //r2 - choosing line
            }
          }
        }
        i += 1
      }
    }
  }
}

object main extends App
{/*
  val c = new ACRConfig("identify-eu-west-1.acrcloud.com", "a10f5c60f5e25167c65853b0bf8748ef", "2QRYUcccB6qsOJvNt1AmU3qFwhU3WkQZGohvPDZP", 10, 60)
  val m = new DirectoryManipulator(tmp.path, c, "7a23fab73af11b37110c268a87ac3a57")
  m.applyOnFile(FileManipulatorUtils.setTags)
//  m.applyOnFile(FileManipulatorUtils.printLyrics)
  m.applyOnDirectory(DirectoryManipulatorUtils.printAverage)
//  m.applyOnFile(FileManipulatorUtils.rename)
  m.applyOnFile(FileManipulatorUtils.saveLyrics)
  m.applyOnDirectory(DirectoryManipulatorUtils.randomQuote)
  m.applyOnDirectory(DirectoryManipulatorUtils.statictics)
*/
}
