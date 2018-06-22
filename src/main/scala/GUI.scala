import java.nio.file._

import swing._

class GUI extends MainFrame {
  def restrictHeight(s: Component) {
    s.maximumSize = new Dimension(Short.MaxValue, s.preferredSize.height)
  }
  title = "Music Files Management"

  val pathField = new TextField { columns = 32 }

  val statistics = new CheckBox("statistics")
  statistics.selected = true
  val rename = new CheckBox("rename")
  rename.selected = true
  val saveLyrics = new CheckBox("saveLyrics")
  saveLyrics.selected = true
  val randomQuote = new CheckBox("randomQuote")
  randomQuote.selected = true
  val setTags = new CheckBox("setTags")
  setTags.selected = true

  restrictHeight(pathField)

  contents = new BoxPanel(Orientation.Vertical) {
    contents += new BoxPanel(Orientation.Horizontal) {
      contents += new Label("PathName")
      contents += Swing.HStrut(5)
      contents += pathField
    }
    contents += Swing.VStrut(5)
    contents += statistics
    contents += Swing.VStrut(5)
    contents += rename
    contents += Swing.VStrut(5)
    contents += saveLyrics
    contents += Swing.VStrut(5)
    contents += randomQuote
    contents += Swing.VStrut(5)
    contents += setTags
    contents += Swing.VStrut(5)


    contents += new BoxPanel(Orientation.Horizontal) {
      contents += Button("Submit") { doyourjob() }
      contents += Swing.HGlue
      contents += Button("Close") { reportAndClose() }
    }
    for (e <- contents)
      e.xLayoutAlignment = 0.0
    border = Swing.EmptyBorder(10, 10, 10, 10)
  }

  def reportAndClose() {
    sys.exit(0)
  }

  def doyourjob() {

    if(pathField.text == "") Dialog.showMessage(contents.head, "Give me your path" , title="Error")
    tmp.path = pathField.text

    // var directory: Path = null
    //directory = Paths.get(tmp.path)

   /* try Files.newDirectoryStream(directory)
    catch{
        case e: NoSuchFileException => {
          Dialog.showMessage(contents.head, tmp.path + " does not exist" , title="Error")
          throw new IllegalArgumentException(tmp.path + " does not exist", e)
        }
        case e: NotDirectoryException => {
          Dialog.showMessage(contents.head, tmp.path + " is not a directory" , title="Error")
          throw new IllegalArgumentException(tmp.path + " is not a directory", e)
        }
        case e: Throwable => {
          Dialog.showMessage(contents.head, "Failed to read the directory" , title="Error")
          throw new IllegalArgumentException("Failed to read the directory", e)
        }
      }*/



    val c = new ACRConfig("identify-eu-west-1.acrcloud.com", "a10f5c60f5e25167c65853b0bf8748ef", "2QRYUcccB6qsOJvNt1AmU3qFwhU3WkQZGohvPDZP", 10, 60)
    val m = new DirectoryManipulator(pathField.text, c, "7a23fab73af11b37110c268a87ac3a57")

    if (saveLyrics.selected)  m.applyOnFile(FileManipulatorUtils.saveLyrics)
    if(setTags.selected) m.applyOnFile(FileManipulatorUtils.setTags)
    if (rename.selected) m.applyOnFile(FileManipulatorUtils.rename)
    if (statistics.selected) m.applyOnDirectory(DirectoryManipulatorUtils.statictics)
    if (randomQuote.selected) {
      m.applyOnDirectory(DirectoryManipulatorUtils.randomQuote)
      quote()
    }

  }

  def quote() {
    if(tmp.quote!="") Dialog.showMessage(contents.head, tmp.quote , title="Quote of the day")
  }

}

object GuiProgram {
  def main(args: Array[String]) {
    val ui = new GUI
    ui.visible = true
  }
}