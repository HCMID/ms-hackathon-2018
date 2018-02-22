
// must be at least 2.11 to use hmt_textmodel
scalaVersion := "2.12.2"




resolvers += Resolver.jcenterRepo
resolvers += Resolver.bintrayRepo("neelsmith","maven")
libraryDependencies ++=   Seq(
  "edu.holycross.shot.cite" %% "xcite" % "3.2.1",
  "edu.holycross.shot" %% "ohco2" % "10.4.0"
)
