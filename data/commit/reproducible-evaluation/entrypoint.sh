#! /bin/bash
echo "Compiling $1"
/usr/local/bin/Rscript -e "renv::activate(); rmarkdown::render('$1')"
