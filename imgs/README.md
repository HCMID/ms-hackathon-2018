# Public-domain images of Bern 318 from the e-codices project

This directory documents how we made citable images from the wonderful public-domain collection hosted by e-codices.


## Collecting e-codices images

Files in this directory you will need:

-  `bern318-iiif-manifest.json`:  IIIF manifest for the e-codices images
-  `hackable.json`: the IIIF manifest artfully split into one line per image so we can rip out URLs and page IDs with string replacement rather than having to parse the onerous IIIF JSON.
-   `snagurls.sc`:  a scala script that reads in `hackable.json` and writes out a series of `wget` commands in a shell script.


From anywhere on the internet, start a scala REPL and `:load snagurls.sc`. This creates a file name `shellableWgets.sh` with 281 `wget` commands to download all the e-codices images.  Then just run `sh shellableWgets.sh` and wait for all 281 images to appear on your computer.

## Cataloging e-codices images

Start a scala REPL and `:load catalogManifest.sc`. This creates a file name `catalog` with 281 cataloged image records in CEX format.


## Creating pyramidal files

1.  `rsync` local set of images with Houston server
2.   run `tileJpgs.sh` shell script on contents of image directory to create pyramidal `tif` files for each `jpg`.

## Serving citable images

Within the root directory of an ICT2 service, place the pyramidal `tif` files in a subdirectory named `citenamespace/collection/version`.  (For Bern 318, this would be `ecode/bern318imgs/v1`;  on `www.homermultitext.org`, the root directory of the ICT2 service is `/project/homer/pyramidal/deepzoom`.)
