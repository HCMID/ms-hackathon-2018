
import scala.io.Source


//MAINIFESTFILE AFTER SED'ing to put one record on a line:
// break up 1-line JSON on string "resources" so that each record line
// (2nd line on) begin with {"@id":
val inFile = "bern88hackable.json"
val outFile =  "shellableWgetsBern88.sh"
val ms = "bern88"
val imgurlPrefix = "http://www.e-codices.unifr.ch:80/loris/bbb/bbb-0088/bbb-0088"
val pages = 37

// Drop first line:  remaining lines are image descriptions
val lines = Source.fromFile(inFile).getLines.toVector
val datalines = lines.tail


// Collection page and URL values from each line:

val pgs =  datalines.map(_.replaceAll("^...id...","").replaceAll("jpg.+","jpg").replaceAll(".jp2/full/full/0/default.jpg","").replaceAll(s"${imgurlPrefix},""))


val urls = datalines.map(_.replaceAll("^...id...","").replaceAll("jpg.+","jpg"))


// 281 entries
val wgetStrs = for (i <- 0 to pages) yield {
  s"wget ${urls(i)} -O  ${ms}_${pgs(i)}.jpg"
}


import java.io.PrintWriter
val hdr = "#!/bin/sh\n\n"
new PrintWriter(outFile){write(hdr + wgetStrs.mkString("\n") + "\n"); close; }

println("\n\nPrinted " + wgetStrs.size + " wget commands to " + outFile)
