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
# Copyright 1998-2012 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
# All rights reserved.
#
# (end of header)
#
# Create InfoGrid release files
#

# Find branch root.
DIR=`pwd`
VERSION=$1;

if [ ! -d "$DIR/ig-tools" ]; then
	echo ERROR: Must be invoked from the top level of the branch.
	exit 1;
fi
if [ -z "${VERSION}" ]; then
	echo ERROR: Must specify name of the release as an argument.
	exit 1;
fi

if [ ! -d upload ]; then
	mkdir upload
fi

FLAGS="-c -j --exclude .svn --exclude tests --exclude apps --exclude testapps"

echo "Creating JavaDoc-archive"
tar ${FLAGS} -f upload/infogrid-${VERSION}-javadoc.tar.bz2 LICENSE* dist/javadoc */dist/javadoc */*/dist/javadoc */*/*/dist/javadoc

echo "Creating ALL JAR-archive"
tar ${FLAGS} -f upload/infogrid-${VERSION}-all-jar.tar.bz2 LICENSE* dist/*.jar

echo "Creating Project JAR-archive"
tar ${FLAGS} -f upload/infogrid-${VERSION}-project-jar.tar.bz2 LICENSE* */dist/*.jar

echo "Creating ByModule JAR-archive (includes [no-dist] JARs)"
tar ${FLAGS} -f upload/infogrid-${VERSION}-bymodule-jar.tar.bz2 LICENSE* */*/*/dist/*.jar

echo "Copying MeshWorld"
cp ig-ui/testapps/org.infogrid.meshworld/dist/org.infogrid.meshworld.war upload/org.infogrid.meshworld-${VERSION}.war
bzip2 upload/org.infogrid.meshworld-${VERSION}.war

echo "Copying NetMeshWorld"
cp ig-ui/testapps/org.infogrid.meshworld.net/dist/org.infogrid.meshworld.net.war upload/org.infogrid.meshworld.net-${VERSION}.war
bzip2 upload/org.infogrid.meshworld.net-${VERSION}.war

echo "Creating FirstStep"
tar ${FLAGS} -f upload/FirstStep-${VERSION}.tar.bz2 LICENSE* -C ig-graphdb/examples/FirstStep FirstStep.java README build.xml -C ../../../dist ig-all.jar

echo "Creating FirstStepWithMySQL"
tar ${FLAGS} -f upload/FirstStepWithMySQL-${VERSION}.tar.bz2 LICENSE* -C ig-graphdb/examples/FirstStepWithMySQL FirstStepWithMySQL_Create.java FirstStepWithMySQL_Read.java README build.xml -C ../../../dist ig-all.jar -C ../ig-vendors/libraries/dev.mysql.com/mysql-connector-java mysql-connector-java-bin.jar

exit 0;

