#!/bin/bash

size=`ls -lah utils.jar`
size=${size:38:4}
if [ -z "`grep $size index.html`" ]
then
    echo "utils.jar size is $size but index.html does not show that."
    exit 1
fi

mv -f package.html temp
scp -r *.html utils.jar doc/ deadsea@ostermiller.org:www/utils
mv -f temp package.html