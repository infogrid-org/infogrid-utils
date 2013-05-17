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
# Copyright 1998-2013 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
# All rights reserved.
#
# (end of header)
#
# Performs some sanity checks for common errors. Should be run prior to
# check-in

script=ig-tools/`basename $0`

if [ ! -d ig-tools ]; then
        echo "ERROR: this script must be invoked from the root directory of the branch using the command ${script}"
        exit 1;
fi;

FLAGS="-i";
THISYEAR="2013";

echo '** Checking that no funny paths exist. **'
grep ${FLAGS} '\.\./\.\./\.\./\.\.' `find ig-* -name project.properties`

echo '** Checking that the vendor is set right. **'
grep ${FLAGS} application.vendor `find ig-* -name project.properties` | grep -v InfoGrid.org

echo '** Checking charset. **'
grep ${FLAGS} source.encoding= `find ig-* -name project.properties` | grep -v UTF-8
grep ${FLAGS} -L source.encoding= `find ig-* -name project.properties`

echo '** Checking copyright. **'
for f in `svn status | grep -v '^$' | grep -v Changelist | egrep '^[AMR]?..... ' | cut -c 8-`; do
	[ -f $f ] && egrep -H '(copyright|Copyright|&copy|\(C\)).*[0-9]{4}' $f | grep -v "${THISYEAR}" > /dev/null && echo $f
done

echo '** Checking for empty directories. **'
for f in `find ig-* -type d -and -not -path '*.svn*' -and -not -name src -and -not -path '*/build*' -and -not -path '*/dist*' -print`; do
	if [ 0 == `ls -1 "$f/" | wc -l` ]; then
		echo $f
	fi
done

echo '** Checking that TLD files copied across projects are the same **'
# The outer loop looks for TLD files in web projects ("web/v") while the wildcard expression in module/*/src
# looks for the master definitions underneath a project's src hierarchy

for v in `find . -path '*/web/v' -and -not -path '*/build/*'`; do
	for f in `find ${v} -name '*.tld' -print`; do
		g=`echo $f | sed "s#^\${v}/##g"`;
#		echo -n '---- now looking at: '
#		echo */modules/*/src/$g ${f}
		diff -H -q */modules/*/src/$g ${f} | sed 's/ and//'
	done;
done;

echo '** Checking that Viewlet CSS files copied across projects are the same **'
for f in */*/org.infogrid.meshworld.net */*/org.infogrid.myhealth.www ; do
	for c in \
web/v/org/infogrid/jee/taglib/candy/OverlayTag.css \
web/v/org/infogrid/jee/taglib/mesh/RefreshTag.css \
web/v/org/infogrid/jee/taglib/viewlet/ViewletAlternativesTag.css \
web/v/org/infogrid/jee/viewlet/graphtree/GraphTreeViewlet.css \
web/v/org/infogrid/jee/viewlet/meshbase/AllMeshObjectsViewlet.css \
web/v/org/infogrid/jee/viewlet/modelbase/AllMeshTypesViewlet.css \
web/v/org/infogrid/jee/viewlet/objectset/ObjectSetViewlet.css \
web/v/org/infogrid/jee/viewlet/propertysheet/PropertySheetViewlet.css \
web/v/org/infogrid/jee/viewlet/wikiobject/WikiObjectDisplayViewlet.css \
web/v/org/infogrid/jee/viewlet/wikiobject/WikiObjectEditViewlet.css \
; do
		if [ -r "$f/$c" ]; then
			diff -H -q */*/org.infogrid.meshworld/$c $f/$c | sed 's/ and//'
		fi
	done;
done

echo '** Checking that Overlays and the Viewlet JSPs and JavaScripts are the same across projects **'
for f in */*/org.infogrid.jee.testapp */*/org.infogrid.meshworld.net */*/org.infogrid.myhealth.www ; do
	for c in \
web/v/org/infogrid/jee/shell/http/HttpShellVerb/bless.jsp \
web/v/org/infogrid/jee/shell/http/HttpShellVerb/blessRole.jsp \
web/v/org/infogrid/jee/shell/http/HttpShellVerb/create.jsp \
web/v/org/infogrid/jee/shell/http/HttpShellVerb/delete.jsp \
web/v/org/infogrid/jee/shell/http/HttpShellVerb/relate.jsp \
web/v/org/infogrid/jee/shell/http/HttpShellVerb/setProperty.jsp \
web/v/org/infogrid/jee/shell/http/HttpShellVerb/unbless.jsp \
web/v/org/infogrid/jee/shell/http/HttpShellVerb/unblessRole.jsp \
web/v/org/infogrid/jee/shell/http/HttpShellVerb/unrelate.jsp \
web/s/templates/default-iframe/text/html/template.jsp \
web/v/org/infogrid/jee/viewlet/bulk/BulkLoaderViewlet.jsp \
web/v/org/infogrid/jee/viewlet/graphtree/GraphTreeViewlet.jsp \
web/v/org/infogrid/jee/viewlet/modelbase/AllMeshTypesViewlet.jsp \
web/v/org/infogrid/jee/viewlet/objectset/ObjectSetViewlet/application/atom+xml/ObjectSetViewlet.jsp \
web/v/org/infogrid/jee/viewlet/objectset/ObjectSetViewlet/application/rss+xml/ObjectSetViewlet.jsp \
web/v/org/infogrid/jee/viewlet/objectset/ObjectSetViewlet/text/json/ObjectSetViewlet.jsp \
web/v/org/infogrid/jee/viewlet/objectset/ObjectSetViewlet.jsp \
web/v/org/infogrid/jee/viewlet/propertysheet/PropertySheetViewlet/attributes.jsp \
web/v/org/infogrid/jee/viewlet/propertysheet/PropertySheetViewlet/audit.jsp \
web/v/org/infogrid/jee/viewlet/propertysheet/PropertySheetViewlet/neighbors.jsp \
web/v/org/infogrid/jee/viewlet/propertysheet/PropertySheetViewlet.jsp \
web/v/org/infogrid/jee/viewlet/wikiobject/WikiObjectDisplayViewlet.jsp \
web/v/org/infogrid/jee/viewlet/wikiobject/WikiObjectEditViewlet.jsp \
web/v/org/infogrid/jee/taglib/candy/OverlayTag.js \
web/v/org/infogrid/jee/taglib/candy/ToggleCssClass.js \
web/v/org/infogrid/jee/taglib/mesh/PropertyTag.js \
web/v/org/infogrid/jee/taglib/candy/OverlayTag.js \
web/v/org/infogrid/jee/taglib/candy/ToggleCssClass.js \
web/v/org/infogrid/jee/taglib/mesh/PropertyTag.js \
; do
                if [ -r "$f/$c" ]; then
			diff -H -q */*/org.infogrid.meshworld/$c $f/$c | sed 's/ and//'
		fi
	done;
done

echo '** Checking that HttpShell HTML and the Viewlet JSPs and JavaScripts are the same across projects **'
for f in */*/org.infogrid.jee.net.testapp */*/org.infogrid.meshworld.net */*/org.infogrid.myhealth.www ; do
	for c in \
web/o/accessLocally.jspo \
web/o/bless.jspo \
web/o/blessRole.jspo \
web/o/create.jspo \
web/o/delete.jspo \
web/o/relate.jspo \
web/o/sweep.jspo \
web/o/sweepAll.jspo \
web/o/unbless.jspo \
web/o/unblessRole.jspo \
web/o/unrelate.jspo \
web/s/templates/default-iframe/text/html/template.jsp \
web/v/org/infogrid/jee/viewlet/bulk/BulkLoaderViewlet.jsp \
web/v/org/infogrid/jee/viewlet/graphtree/GraphTreeViewlet.jsp \
web/v/org/infogrid/jee/viewlet/modelbase/AllMeshTypesViewlet.jsp \
web/v/org/infogrid/jee/viewlet/objectset/ObjectSetViewlet/application/atom+xml/ObjectSetViewlet.jsp \
web/v/org/infogrid/jee/viewlet/objectset/ObjectSetViewlet/application/rss+xml/ObjectSetViewlet.jsp \
web/v/org/infogrid/jee/viewlet/objectset/ObjectSetViewlet/text/json/ObjectSetViewlet.jsp \
web/v/org/infogrid/jee/viewlet/objectset/ObjectSetViewlet.jsp \
web/v/org/infogrid/jee/viewlet/wikiobject/WikiObjectDisplayViewlet.jsp \
web/v/org/infogrid/jee/viewlet/wikiobject/WikiObjectEditViewlet.jsp \
web/v/org/infogrid/jee/taglib/candy/OverlayTag.js \
web/v/org/infogrid/jee/taglib/candy/ToggleCssClass.js \
web/v/org/infogrid/jee/taglib/mesh/PropertyTag.js \
web/v/org/infogrid/jee/taglib/candy/OverlayTag.js \
web/v/org/infogrid/jee/taglib/candy/ToggleCssClass.js \
web/v/org/infogrid/jee/taglib/mesh/PropertyTag.js \
; do
                if [ -r "$f/$c" ]; then
			diff -H -q */*/org.infogrid.meshworld/$c $f/$c | sed 's/ and//'
		fi
	done;
done

for pattern in "$@"; do
	echo '** Checking that pattern' ${pattern} 'does not exist. **'
	find . -type f -and -not -path '*/.svn/*' -exec grep ${FLAGS} -H ${pattern} {} \;
done

exit 0;
