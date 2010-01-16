#!/bin/sh -x
#
# Perform the automated build

svn update trunk
ig-tools/build.sh -c ig-config/build.infogrid.org.tomcat55.properties -clean -build
code=$?

if [ $code = 0 ]; then
	ig-tools/build.sh -c ig-config/build.infogrid.org.tomcat55.properties -run
	code=$?
fi

# Clean up after ourselves so the next svn update does not fail
# ig-tools/build.sh -c ig-config/build.infogrid.org.tomcat55.properties -clean

echo Exiting with code $code.
exit $code
