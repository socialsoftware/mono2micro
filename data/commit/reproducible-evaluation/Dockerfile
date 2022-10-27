FROM rocker/verse:4.2.1

RUN apt-get update && apt-get install -y imagemagick

#USER rstudio
#WORKDIR /home/rstudio/report
WORKDIR /report
ENV RENV_VERSION 0.16.0

RUN R -e "install.packages('remotes', repos = c(CRAN = 'https://cloud.r-project.org'))"
RUN R -e "remotes::install_github('rstudio/renv@${RENV_VERSION}')"

COPY renv.lock renv.lock
RUN mkdir -p renv
COPY .Rprofile .Rprofile
COPY renv/activate.R renv/activate.R
COPY renv/settings.dcf renv/settings.dcf
RUN R -e "renv::restore()"

RUN tlmgr install amsmath latex-amsmath-dev iftex kvoptions ltxcmds kvsetkeys etoolbox xcolor geometry booktabs mdwtools multirow wrapfig float colortbl pdflscape tabu varwidth threeparttable threeparttablex environ trimspaces ulem makecell auxhook bigintcalc bitset etexcmds gettitlestring hycolor hyperref intcalc kvdefinekeys letltxmacro pdfescape refcount rerunfilecheck stringenc uniquecounter zapfding pdftexcmds infwarerr epstopdf-pkg subfig caption

COPY entrypoint.sh entrypoint.sh
COPY helper_functions.R helper_functions.R
COPY data data

USER rstudio
ENTRYPOINT ["./entrypoint.sh"]
