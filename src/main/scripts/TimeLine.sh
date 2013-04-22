sed s/%s/@$1@/ conf/reports/INDI-TimeLineWithCloseRelatives.arq > tmp.arq
java -Xmx1024M -cp ${project.build.finalName}.jar gedcom2sem.semweb.Select conf/result-to-html.xsl tmp.arq kennedy.ttl JFKtimeline.html
