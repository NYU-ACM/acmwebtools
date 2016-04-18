package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import java.io.{ FileInputStream, File }
import scala.io.Source
import org.jfree.chart.{ ChartFactory, ChartUtilities, JFreeChart }
import org.jfree.data.general.DefaultPieDataset
import org.apache.pdfbox.pdmodel.{PDDocument, PDPage }
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream
import org.apache.pdfbox.pdmodel.graphics.xobject.{ PDXObjectImage, PDJpeg }

@Singleton
class FileController extends Controller {
  def droid = Action {
    Ok(views.html.droid())
  }

  def upload = Action(parse.multipartFormData) { request =>
    request.body.file("file").map { file =>

      val filename = file.filename
      val contentType = file.contentType
      val tmp = new File(s"/tmp/$filename")
      file.ref.moveTo(tmp)
      val map = populateMap(tmp)
      val output = generatePDF(map)

      Ok.sendFile(
        content = output,
        fileName = _ => output.getName
      )
      
    }.getOrElse {
      Redirect(routes.FileController.droid).flashing("error" -> "Missing file")
    }
  }

  case class Entry(mime: String, format: String, count: Int, size: Long)

  def populateMap(file: File): Map[String, Entry] = {
    var map: Map[String, Entry] = Map()
    Source.fromFile(file).getLines.drop(1).foreach { line =>

      val fields = line.split(",", -1)
      if(fields.size == 18) {
        val puid = fields(14)
        val mime = fields(15)
        val format = fields(16)
        val size = fields(7).toLong

        if(map.contains(puid)) {
          val entry = map(puid)
          val count = entry.count + 1
          val newSize = entry.size + size
          map = map + (puid -> new Entry(mime, format, count, newSize))
        }
        else { map = map + (puid -> new Entry(mime, format, 1, size)) }
      }
    }
    map
  }


  def generatePDF(map: Map[String, Entry]): File = {
    val document = new PDDocument

    val sizeData = new DefaultPieDataset
    val countData = new DefaultPieDataset

    map.foreach{ entry =>
      sizeData.setValue(entry._2.format, entry._2.size)
      countData.setValue(entry._2.format, entry._2.count)
    }

    val sizeChart = ChartFactory.createPieChart("Files By Size", sizeData, false, true, false)
    val countChart = ChartFactory.createPieChart("Files By Size", sizeData, false, true, false)
    val sizeFile = new File("/tmp/size.jpg")
    val countFile = new File("/tmp/count.jpg")

    ChartUtilities.saveChartAsJPEG(sizeFile, sizeChart, 800, 450)
    ChartUtilities.saveChartAsJPEG(countFile, countChart, 800, 450)

    addImageToDocument(document, sizeFile)
    addImageToDocument(document, countFile)
    val output = new File("/tmp/report.pdf")
    document.save(output)
    document.close
    output 
  }

  def addImageToDocument(document: PDDocument, file: File) {
    import java.awt.geom.AffineTransform
    import java.lang.Math
    val page = new PDPage
    document.addPage(page)
    val stream = new PDPageContentStream(document, page)
    val image: PDXObjectImage = new PDJpeg(document, new FileInputStream(file))
    val width = ((image.getWidth  / 4.0) * 3.0).toFloat
    val height = ((image.getHeight  / 4.0) * 3.0).toFloat
    stream.drawXObject(image, 0, page.getMediaBox.getHeight - height, width, height)
    stream.close
  }

}