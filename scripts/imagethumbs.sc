/*
Script to create gh page (in markdown format) for displaying
thumbnails of images.

Run this script from the root directory of the repository.
E.g., in the repo's root dir, open a scala REPL, and

    :load scripts/imagethumbs.sc

*/

val ms = "bern88"

// width of table in columns
val colSize = 6
// width of thubmnail images in pixels
val thumbSize = 100

// Includes some improved, manually added captions in addition
// to content automatically extracted from IIIF manifest:
val cex = s"imgs/${ms}catalog.cex"

// Settings for HMT ICT2 service:
val collectionBaseUrl = s"http://www.homermultitext.org/iipsrv?OBJ=IIP,1.0&FIF=/project/homer/pyramidal/deepzoom/ecod/${ms}/v1/"
val ict = "http://www.homermultitext.org/ict2"
val imgSnip = s".tif&WID=${thumbSize}&CVT=JPEG"

import scala.io.Source
val lines  = Source.fromFile(cex).getLines.toVector
// Convert vector of delimited text to vector of markdown
// content for a single cell in the table
val imgContent = for (l <- lines.tail) yield {
  // Manually split collection, version, img in CEX source
  val cols = l.split("#")
  val urnParts = cols(0).split(":")
  val objId = urnParts(4)

  s"[![${cols(1)}](${collectionBaseUrl}${objId}${imgSnip})](${ict}?urn=${cols(0)})<br/><span style='text-align: center; font-size: 60%; line-height: normal;display:block;'>${cols(1)}</span>"
}

// convert vector of markdown cells to rows of markdown table
val rows = for (i <- 0 until imgContent.size) yield {
    //  easier for computing slicing:
    val oneBasedIndex = i + 1
    //println(s"${oneBasedIndex}->${oneBasedIndex % colSize}")
    if (oneBasedIndex % colSize == 0){
      //println(s"\tWriting onebased at ${oneBasedIndex}")
      val sliver = imgContent.slice( oneBasedIndex - colSize, oneBasedIndex)
      "| " + sliver.mkString(" | ") + " |"
    } else ""
}


// Collect any left-over cells
val remndr =  imgContent.size % colSize
val trailer = if (remndr != 0)  {
  val sliver = imgContent.slice(imgContent.size - remndr, imgContent.size)
  val pad = List.fill( colSize - remndr - 1)( " | ").mkString
  "| " + sliver.mkString(" | ") + pad + " |\n"
} else ""


// Pieces of the completed md table:
val hdrLabels =  List.fill(colSize)("| ").mkString + "|\n"
val hdrSeparator =  List.fill(colSize)("|:-------------").mkString + "|\n"
val mdTable = hdrLabels + hdrSeparator + rows.filter(_.nonEmpty).mkString("\n") + "\n" + trailer

// And the rest of the page content:
val pghdr = "---\ntitle: Images\nlayout: page\n---\n\n"
val intro = "Thumbnail images are linked to an image citation tool\n\n"

import java.io.PrintWriter
new java.io.PrintWriter("docs/${ms}-thumbs.md"){write(pghdr + intro + mdTable);close;}
