#!/bin/bash
# This file is part of InfoGrid(tm). You may not use this file except in
# compliance with the InfoGrid license. The InfoGrid license and important
# disclaimers are contained in the file LICENSE.InfoGrid.txt that you should
# have received with InfoGrid. If you have not received LICENSE.InfoGrid.txt
# or you do not consent to all aspects of the license and the disclaimers,
# no license is granted; do not use this file.
#
# For more information about InfoGrid go to http://infogrid.org/
#
# Copyright 1998-2011 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
# All rights reserved.
#
# (end of header)
#
# Print some statistics. Not the world's most efficient algorithm, but that doesn't
# seem very important for this purpose here.
#

script=ig-tools/`basename $0`

if [ ! -d ig-tools ]; then
        echo "ERROR: this script must be invoked from the root directory of the branch using the command ${script}"
        exit 1;
fi;

PROJECTS="ig-utils ig-graphdb ig-graphdb-grid ig-model-library ig-lid ig-ui ig-stores ig-probe";
HEAD_FORMAT="%-18s %12s %10s %17s %16s %10s %11s %10s %12s %9s %9s\n"
FORMAT="%-18s %'12d %'10d %'17d %'16d %'10d %'11d %'10d %'12d %'9d %'9d\n"

printf "${HEAD_FORMAT}" "Project" "Total Files" "Projects" "Total Java Files" "Java Test Files" "JSP Files" "JSP LOC" "HTML Files" "CSS Files" "Image Files" "Java LOC"

TOTAL_ALL_FILES=0
TOTAL_NB_FILES=0
TOTAL_JAVA_FILES=0
TOTAL_JAVA_TEST_FILES=0
TOTAL_JSP_FILES=0
TOTAL_JSP_LOC=0
TOTAL_HTML_FILES=0
TOTAL_CSS_FILES=0
TOTAL_IMAGE_FILES=0
TOTAL_JAVA_LOC=0

for p in ${PROJECTS}; do

	TMPFILE=`mktemp /tmp/infogrid-spill-temp.XXXX`
	find $p -type f -and -not -path \*/build/\* -and -not -path \*/dist/\* -and -not -path \*/.svn/\* > ${TMPFILE}

	      ALL_FILES=`cat ${TMPFILE}                              | wc -l`
 	       NB_FILES=`cat ${TMPFILE} | grep '/project.xml$'       | wc -l`
	     JAVA_FILES=`cat ${TMPFILE} | grep '\.java$'             | wc -l`
	JAVA_TEST_FILES=`cat ${TMPFILE} | grep 'Test[0-9]*\.java$'   | wc -l`
	      JSP_FILES=`cat ${TMPFILE} | grep '\.jsp$'              | wc -l`
	        JSP_LOC=`cat ${TMPFILE} | grep '\.jsp$' | xargs cat  | wc -l`
	     HTML_FILES=`cat ${TMPFILE} | grep '\.html$'             | wc -l`
	      CSS_FILES=`cat ${TMPFILE} | grep '\.css$'              | wc -l`
	    IMAGE_FILES=`cat ${TMPFILE} | egrep '\.(png|jpg|gif)$'   | wc -l`
 	       JAVA_LOC=`cat ${TMPFILE} | grep '\.java$' | xargs cat | wc -l`

	TOTAL_ALL_FILES=$((${ALL_FILES} + ${TOTAL_ALL_FILES}))
	TOTAL_NB_FILES=$((${NB_FILES} + ${TOTAL_NB_FILES}))
	TOTAL_JAVA_FILES=$((${JAVA_FILES} + ${TOTAL_JAVA_FILES}))
	TOTAL_JAVA_TEST_FILES=$((${JAVA_TEST_FILES} + ${TOTAL_JAVA_TEST_FILES}))
	TOTAL_JSP_FILES=$((${JSP_FILES} + ${TOTAL_JSP_FILES}))
	TOTAL_JSP_LOC=$((${JSP_LOC} + ${TOTAL_JSP_LOC}))
	TOTAL_HTML_FILES=$((${HTML_FILES} + ${TOTAL_HTML_FILES}))
	TOTAL_CSS_FILES=$((${CSS_FILES} + ${TOTAL_CSS_FILES}))
	TOTAL_IMAGE_FILES=$((${IMAGE_FILES} + ${TOTAL_IMAGE_FILES}))
	TOTAL_JAVA_LOC=$((${JAVA_LOC} + ${TOTAL_JAVA_LOC}))

	printf "${FORMAT}" "${p}" "${ALL_FILES}" "${NB_FILES}" "${JAVA_FILES}" "${JAVA_TEST_FILES}" "${JSP_FILES}" "${JSP_LOC}" "${HTML_FILES}" "${CSS_FILES}" "${IMAGE_FILES}" "${JAVA_LOC}"

	rm $TMPFILE
done
echo "======================================================================================================================================="

printf "${FORMAT}" "Total:" "${TOTAL_ALL_FILES}" "${TOTAL_NB_FILES}" "${TOTAL_JAVA_FILES}" "${TOTAL_JAVA_TEST_FILES}" "${TOTAL_JSP_FILES}" "${TOTAL_JSP_LOC}" "${TOTAL_HTML_FILES}" "${TOTAL_CSS_FILES}" "${TOTAL_IMAGE_FILES}" "${TOTAL_JAVA_LOC}"

echo ''
echo 'This branch currently takes up' `du -s -h | sed -e 's/[ \t\.]//g'` 'bytes of disk space. Did you expect more or less? ;-)'
exit 0

