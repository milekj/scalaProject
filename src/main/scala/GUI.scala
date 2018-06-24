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
      contents += Button("Submit") { runOrError() }
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

  private def runOrError() {
    if (!isValid)
      Dialog.showMessage(contents.head, "Invalid path", title="Error" )
    else
      run()
  }

  private def isValid: Boolean = {
    Files.isDirectory(Paths.get(pathField.text)) && !pathField.text.isEmpty
  }

  def run() {

    val c = new ACRConfig("identify-eu-west-1.acrcloud.com", "c7fef473bb7e68f294a5ac2bd42566ea", "K6wn9AOHzaGY8mym3nKOhA4w6rk8rcPqRVUQ3yEM", 10, 60)
    val m = new DirectoryManipulator(pathField.text, c, "7a23fab73af11b37110c268a87ac3a57")

    if (saveLyrics.selected)  m.applyOnFile(FileManipulatorUtils.saveLyrics)
    if(setTags.selected) m.applyOnFile(FileManipulatorUtils.setTags)
    if (rename.selected) m.applyOnFile(FileManipulatorUtils.rename)
    if (statistics.selected) m.applyOnDirectory(DirectoryManipulatorUtils.statictics)
    if (randomQuote.selected) {
      val r = new RandomQuoteRetriever()
      m.applyOnDirectory(r.randomQuote)
      quote(r.quote)
    }

  }

  def quote(q: String) {
    Dialog.showMessage(contents.head, q , title="Quote of the day")
  }

}

object GuiProgram {
  def main(args: Array[String]) {
    val ui = new GUI
    ui.visible = true
  }
}