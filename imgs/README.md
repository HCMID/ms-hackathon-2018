# Public-domain images of Bern 318 from the e-codices project

This directory documents how we made citable images from the wonderful public-domain collection hosted by e-codices.


## Collecting e-codices images

Contents of this directory:

-  `bern318-iiif-manifest.json`:  IIIF manifest for the e-codices images
-  `hackable.json`: the IIIF manifest artfully split into one line per image so we can rip out URLs and page IDs with string replacement rather than having to parse the onerous IIIF JSON.
-   `snagurls.sc`:  a scala script that reads in `hackable.json` and writes out a series of `wget` commands in a shell script.


From anywhere on the internet, start a scala REPL and `:load snagurls.sc`. This creates a file name `shellableWgets.sh` with 280 `wget` commands to download all the e-codices images.  Then just run `sh shellableWgets.sh` and wait for all 281 images to appear on your computer.

## Cataloging e-codices images

TBA


## Creating pyramidal files

TBA

## Serving citable images
