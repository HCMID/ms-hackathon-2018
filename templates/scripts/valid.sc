import edu.holycross.shot.cite._
import scala.io.Source

val paleoData = "paleography/paleography.cex"

case class CandidateCite2Urn (s: String, u: Option[Cite2Urn])

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

  // check that URNs are syntactically valid and unique

}

def paleography = {
  validatePaleo(paleoData)
}
