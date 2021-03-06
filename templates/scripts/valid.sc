import edu.holycross.shot.cite._
import edu.holycross.shot.ohco2._
import scala.io.Source
import java.io.PrintWriter
import scala.xml.XML
import scala.collection.mutable.ArrayBuffer


val paleoData = "paleography/paleography.cex"


def textRepo: TextRepository = {
  val catCex = "editions/ctscatalog.cex"
  val citeConf = "editions/citationconfig.cex"
  val baseDir = "editions"
  TextRepositorySource.fromFiles(catCex,citeConf,baseDir)
}

def sToCiteU(urnString: String): Option[Cite2Urn]= {
  try {
    val u = Cite2Urn(urnString)
     Some(u)
  } catch {
    case _ : Throwable =>  {
      println("\t=>INVALID object URN syntax: " + urnString)
      None
    }
  }
}

def sToCtsU(urnString: String): Option[CtsUrn] = {
  try {
    val u = CtsUrn(urnString)
    Some(u)
  } catch {
    case _ : Throwable => {
      println("\t=>INVALID text URN syntax: " + urnString)
      None
    }
  }
}

def paleoImages(fName: String, thumbSize: Int = 300):  Vector[String] = {
  // Setting for HMT ICT2 service:
  val iipsrvBaseUrl = "http://www.homermultitext.org/iipsrv?OBJ=IIP,1.0&FIF=/project/homer/pyramidal/deepzoom"

  val lines = Source.fromFile(fName).getLines.toVector.tail
  for (l <- lines) yield {
    val cols = l.split("#").toVector
    val label = try {
      val  txt = CtsUrn(cols(1))
      txt.passageNodeSubref
    } catch {
      case _ : Throwable => "Invalid text urn " + cols(1)
    }

    val formatted = try {
      val u = Cite2Urn(cols(0))
      val pathString = List(iipsrvBaseUrl, u.namespace, u.collection, u.version, u.dropExtensions.objectComponent).mkString("/")
      s"| **${label}** | ![${label}](${pathString}.tif&RGN=${u.objectExtension}&WID=${thumbSize}&CVT=JPEG) | "

    } catch {
      case _ : Throwable => s"| ${label} | Invalid image URN: ${cols(0)} |"
    }
    formatted
  }
}

def validatePaleo(paleoFile: String) = {
  val lines = Source.fromFile(paleoFile).getLines.toVector.tail
  val cols = for (l <- lines) yield {
    l.split("#")
  }

  println("Found " + lines.size + " paleographic entries.")
  val imgs = cols.map(_(0))
  val txt = cols.map(_(1))

  val imgUrns = imgs.map(sToCiteU(_))
  if (imgUrns.flatten.size == lines.size) {
    println("All image references syntactically valid.")
  } else {
    //val badImgReff = imgUrns.filter(  )
    println("Found " + imgUrns.flatten.size + " valid image URNs.")
    println("Please correct errors .")
  }

  // check that URNs are syntactically valid and unique
  val txtUrns = txt.map(sToCtsU(_))
  if (txtUrns.flatten.size == lines.size) {
    println("All text references syntactically valid.")
    if (txtUrns.distinct.size == lines.size) {
      println("All text references are unique.")
    } else {
      println("\nYou have duplicate text references:")
      val urnCounts = txtUrns.groupBy(identity).map{ case (u,lst) => (u,lst.size) }
      val duplicateOpts = urnCounts.filter(_._2 > 1).keySet
      //println(duplicateOpts.map(_.get).mkString("\n")
      for (dupe <- duplicateOpts) {
        println("\t" + dupe.get + " appears " + urnCounts(dupe) + " times.")
      }
    }


  } else {
    //val badImgReff = imgUrns.filter(  )
    println("Found " + txtUrns.flatten.size + " valid text URNs.")
    println("Please correct errors .")
  }
  val tableRows = paleoImages(paleoFile)

  val hdrLabels =  "| Reading of glyph | Image |\n"
  val hdrSeparator =  List.fill(2)("|:-------------").mkString + "|\n"
  val mdTable = hdrLabels + hdrSeparator + tableRows.filter(_.nonEmpty).mkString("\n") + "\n"

  val pageHeader = "# Paleographic inventory\n\n"
  new java.io.PrintWriter("reports/paleography.md"){write(pageHeader + mdTable);close;}
  println("\n\nVisualization of paleographic observations written to reports/paleography.md")
  println("Please verify results.\n")
}

def paleography = {
  validatePaleo(paleoData)
}

def collectElements(n: scala.xml.Node, buff: ArrayBuffer[String]): ArrayBuffer[String] = {
  var newBuff = buff
  n match {
    case t: scala.xml.Text => {
    }

    case e: scala.xml.Elem => {
      for (ch <- e.child) {
        newBuff = collectElements(ch, buff += e.label)
      }
    }
  }
  newBuff
}


def collectText(n: scala.xml.Node, s: String): String = {
  var buff = StringBuilder.newBuilder
  buff.append(s)
  n match {
    case t: scala.xml.Text => {
      buff.append(t.text)
    }

    case e: scala.xml.Elem => {
      for (ch <- e.child) {
        buff = new StringBuilder(collectText(ch, buff.toString))
      }
    }
  }
  buff.toString
}

def validTEI(n: scala.xml.Node) = {
  val validElements = Source.fromFile("standards/tei-elements.txt").getLines.toVector.toSet
  val actualElements = collectElements(n, ArrayBuffer.empty[String]).toSet


  if (actualElements.subsetOf(validElements)) {
    println("\tAll TEI elements are valid")
  } else {
    println("\n  Your text includes the following invalid TEI elements:")
    val bad = actualElements.diff(validElements)
    for (b <- bad) {
      println("\t" + b)
    }
  }

}


/*
def codeptList (s: String, idx : Int = 0, codepoints: List[Int] = Nil): List[Int] = {
  if (idx >= s.length) {
    codepoints.reverse
  } else {
    val cp = s.codePointAt(idx)
    codeptList(s, idx + java.lang.Character.charCount(cp), cp :: codepoints)
  }
}*/

def validCharset(n: scala.xml.Node) = {
  val whitespace = """
""" + " "
  val validChars = Source.fromFile("standards/characters.txt").getLines.toVector.mkString("").distinct.toSet ++ whitespace.distinct.toSet

  val t = collectText(n, "")
  val actualChars = t.distinct.toSet
  if (actualChars.subsetOf(validChars)) {
    println("\tAll characters are valid")
  } else {
    println("\n  Your text includes the following invalid characters:")
    val bad = actualChars.diff(validChars)
    for (b <- bad) {
      println("\t" + b + s" (${Character.codePointAt(Array(b),0,1)})")
    }
  }
}


def dseTriples = {
  val lines = Source.fromFile("relations/dsetriples.cex").getLines.toVector
  for (l <- lines.tail) yield {
    val cols = l.split("#").toVector
    val textOpt = try {
      Some(CtsUrn(cols(0)))
    } catch {
      case _ : Throwable => None
    }
    if (cols.size == 3) {
      (textOpt,Vector(cols(1),cols(2)))
    } else {
      (textOpt,Vector(cols(1)))
    }
  }
}

def validateDSE(urn: CtsUrn, thumbSize: Int = 300) = {
  val iipsrvBaseUrl = "http://www.homermultitext.org/iipsrv?OBJ=IIP,1.0&FIF=/project/homer/pyramidal/deepzoom"
  val ictBaseUrl = "http://www.homermultitext.org/ict2/?urn="

  println(s"\n  Checking DSE relations for ${urn}")
  val allTriples = dseTriples
  val entries = allTriples.filter(_._2.size == 2)
  // now find entry/ies matching urn...
  val relevant = entries.filter(_._1.get ~~ urn)
  println(s"\tFound ${relevant.size} entry/ies ")
  val lines = for (entry <- relevant) yield {
    val img = Cite2Urn(entry._2(1))

    val pathString = List(iipsrvBaseUrl, img.namespace, img.collection, img.version, img.dropExtensions.objectComponent).mkString("/")
    s"| **${urn.passageComponent}** | [![${urn.passageComponent}](${pathString}.tif&RGN=${img.objectExtension}&WID=${thumbSize}&CVT=JPEG)](${ictBaseUrl}${img}) | "
  }

  val hdrLabels =  "| Passage | Image |\n"
  val hdrSeparator =  List.fill(2)("|:-------------").mkString + "|\n"
  val mdTable = hdrLabels + hdrSeparator + lines.filter(_.nonEmpty).mkString("\n") + "\n"


  val pageHeader = "# DSE inventory\n\n"

  new java.io.PrintWriter("reports/dse.md"){write(pageHeader + mdTable);close;}
  println("\n\nVisualization of DSE observations written to reports/dse.md")
  println("Please verify results.\n")
}



def validateEdition(baseUrl: String) = {
  val xml = XML.loadFile("editions/physiologus.xml")
  val chunks = xml \ "text" \ "body" \ "div"
  println("\n\nValidating editorial work in " + baseUrl)
  for (c <- chunks) {
    val unit = (c \ "@n").text
    val u = CtsUrn(baseUrl + unit)
    println("\nValidating section " + u.passageComponent + " ...")
    validCharset(c)
    validTEI(c)
    validateDSE(u)
  }



}

def edition = {
  validateEdition("urn:cts:mid:bestiaries.bern318.hc:")
}
println("\n\n\n")
println("You may run either of two validating scripts:\n")
println("\tpaleography")
println("\tedition")
