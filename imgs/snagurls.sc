
import scala.io.Source


//MAINIFESTFILE AFTER SED'ing to put one record on a line:
val inFile = "hackable.json"
val outFile =  "shellableWgets.sh"
val ms = "bern318"


// Drop first line:  remaining lines are image descriptions
val lines = Source.fromFile(inFile).getLines.toVector
val datalines = lines.tail


// Collection page and URL values from each line:

val pgs =  datalines.map(_.replaceAll("^...id...","").replaceAll("jpg.+","jpg").replaceAll(".jp2/full/full/0/default.jpg","").replaceAll("http://www.e-codices.unifr.ch:80/loris/bbb/bbb-0318/bbb-0318_",""))

val urls = datalines.map(_.replaceAll("^...id...","").replaceAll("jpg.+","jpg"))


// 281 entries
val wgetStrs = for (i <- 0 to 280) yield {
  s"wget ${urls(i)} -O  ${ms}_${pgs(i)}.jpg"
}


import java.io.PrintWriter
val hdr = "#!/bin/sh\n\n"
new PrintWriter(outFile){write(hdr + wgetStrs.mkString("\n") + "\n"); close; }

println("\n\nPrinted " + wgetStrs.size + " wget commands to " + outFile)
