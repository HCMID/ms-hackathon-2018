
import scala.io.Source


//MAINIFESTFILE AFTER SED'ing to put one record on a line:
val inFile = "hackable.json"
val outFile =  "catalog.cex"
val ms = "bern318"
val urnBase = "urn:cite2:mid:bern318imgs.mid:"

// Drop first line:  remaining lines are image descriptions
val lines = Source.fromFile(inFile).getLines.toVector
val datalines = lines.tail


// Collection page and URL values from each line:

val pgs =  datalines.map(_.replaceAll("^...id...","").replaceAll("jpg.+","jpg").replaceAll(".jp2/full/full/0/default.jpg","").replaceAll("http://www.e-codices.unifr.ch:80/loris/bbb/bbb-0318/bbb-0318_",""))

val urls = datalines.map(_.replaceAll("^...id...","").replaceAll("jpg.+","jpg"))

val hdr = "Image#Caption#Rights\n"
// 281 entries
val catalog = for (i <- 0 to 280) yield {
  val pg = pgs(i).replaceFirst("^[0]+","")
  s"${urnBase}${ms}_${pgs(i)}#Bern 318, page ${pg}#Public domain image from the e-codices project"
}


import java.io.PrintWriter

new PrintWriter(outFile){write(hdr + catalog.mkString("\n") + "\n"); close; }

println("\n\nPrinted " + catalog.size + " CEX records to " + outFile)
