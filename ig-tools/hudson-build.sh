#!/bin/sh -x
#
# Perform the automated build

trap stop 2
function stop {
        echo 'Interrupted by control-c. Exiting ...'
        exit 1;
}

CMD=

# Clean up last build
pushd trunk
$CMD ig-tools/build.sh -c ig-config/build.infogrid.org.tomcat55.properties -clean
popd

# Get new code
$CMD svn update trunk tags/tests-pass-latest

# New build
pushd trunk
$CMD ig-tools/build.sh -c ig-config/build.infogrid.org.tomcat55.properties -clean -build
code=$?
popd

# If build worked, run tests
if [ $code = 0 ]; then
	pushd trunk
	$CMD ig-tools/build.sh -c ig-config/build.infogrid.org.tomcat55.properties -run
	code=$?
	popd
fi

# If build worked and tests passed, spill beans and attempt to promote
if [ $code = 0 ]; then
	pushd trunk
	$CMD ig-tools/spill-beans.sh
	popd

	# Attempt to promote
	trunkVersion=`svnversion trunk`
	passLatestVersion=`svnversion tags/tests-pass-latest`
	mergeCommand="svn merge -r ${passLatestVersion}:${trunkVersion} http://svn.infogrid.org/infogrid/trunk"
	if [[ "${trunkVersion}" =~ ^[0-9]+$ ]]; then
		echo Found clean version ${trunkVersion}, promoting ...
		pushd tags/tests-pass-latest
		$CMD ${mergeCommand}
		$CMD svn commit . -m "${mergeCommand}"
		popd
	else
		echo Version ${trunkVersion} not clean, not promoting. Command would have been ${mergeCommand}
	fi
fi

echo Exiting with code $code.
exit $code
