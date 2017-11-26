
import scala.io.Source


//MAINIFESTFILE AFTER SED'ing to put one record on a line:
val inFile = "bern88hackable.json"
val outFile =  "catalog2.cex"
val ms = "bern88"
val urnBase = "urn:cite2:ecod:bern88imgs.v1:"
val pages = 37

// Drop first line:  remaining lines are image descriptions
val lines = Source.fromFile(inFile).getLines.toVector
val datalines = lines.tail


// Collection page and URL values from each line:

val pgs =  datalines.map(_.replaceAll("^...id...","").replaceAll("jpg.+","jpg").replaceAll(".jp2/full/full/0/default.jpg","").replaceAll("http://www.e-codices.unifr.ch:80/loris/bbb/bbb-0088/bbb-0088_",""))

val urls = datalines.map(_.replaceAll("^...id...","").replaceAll("jpg.+","jpg"))

val hdr = "Image#Caption#Rights\n"

val catalog = for (i <- 0 to pages) yield {
  val pg = pgs(i).replaceFirst("^[0]+","")
  s"${urnBase}${ms}_${pgs(i)}#Bern 88, page ${pg}#Public domain image from the e-codices project"
}


import java.io.PrintWriter

new PrintWriter(outFile){write(hdr + catalog.mkString("\n") + "\n"); close; }

println("\n\nPrinted " + catalog.size + " CEX records to " + outFile)
