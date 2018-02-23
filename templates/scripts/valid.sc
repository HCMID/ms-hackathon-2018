import edu.holycross.shot.cite._
import scala.io.Source
import java.io.PrintWriter


val paleoData = "paleography/paleography.cex"


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
  // Settings for HMT ICT2 service:
  val iipsrvBaseUrl = s"http://www.homermultitext.org/iipsrv?OBJ=IIP,1.0&FIF=/project/homer/pyramidal/deepzoom"


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
}

def paleography = {
  validatePaleo(paleoData)
}
