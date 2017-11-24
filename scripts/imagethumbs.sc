
import scala.io.Source

val cex = "imgs/catalog-tweaked.cex"
val colSize = 6
val thumbSize = 100
val baseUrl = "http://www.homermultitext.org/iipsrv?OBJ=IIP,1.0&FIF=/project/homer/pyramidal/deepzoom/ecod/bern318imgs/v1/"

val ict = "http://www.homermultitext.org/ict2"


val imgSnip = s".tif&WID=${thumbSize}&CVT=JPEG"


//coll, version, img
val lines  = Source.fromFile(cex).getLines.toVector
val imgContent = for (l <- lines.tail) yield {

  val cols = l.split("#")
  val urnParts = cols(0).split(":")
  val objId = urnParts(4)

  s"[![${cols(1)}](${baseUrl}${objId}${imgSnip})](${ict}?urn=${cols(0)})<br/><span style='text-align: center; font-size: 60%; line-height: normal;display:block;'>${cols(1)}</span>"
}


val rows = for (i <- 0 until imgContent.size) yield {
    if ((i % colSize == 0) && (i > 0)){
      val sliver = imgContent.slice( i - colSize, i)
      //println(s"From ${i - colSize} to  ${i} = ${sliver.size}")
      "| " + sliver.mkString(" | ") + " |"
    } else ""
}

val remndr =  imgContent.size % colSize
val trailer = if (remndr != 0) {
  val sliver = imgContent.slice(imgContent.size - remndr, imgContent.size)
  val pad = List.fill( colSize - remndr - 1)( " | ").mkString



  "| " + sliver.mkString(" | ") + pad + " |\n"
} else ""


val hdrLabels =  List.fill(colSize)("| ").mkString + "|\n"
val hdrSeparator =  List.fill(colSize)("|:-------------").mkString + "|\n"

val mdTable = hdrLabels + hdrSeparator + rows.filter(_.nonEmpty).mkString("\n") + "\n" + trailer


val pghdr = "---\ntitle: Images\nlayout: page\n---\n\n"

val intro = "Thumbnail images are linked to an image citation tool\n\n"

import java.io.PrintWriter
new java.io.PrintWriter("docs/thumbs.md"){write(pghdr + intro + mdTable);close;}
