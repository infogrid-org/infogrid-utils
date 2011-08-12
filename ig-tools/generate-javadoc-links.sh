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
# Generate JavaDoc links. Usually invoked from build.sh with -dist command.
#

PROJECT=$1
shift
PARTS=$*
PARTS=`echo ${PARTS} | tr ' ' '\n' | sort`

cat <<END
<html>
 <head>
  <title>InfoGrid JavaDoc: Project ${PROJECT}</title>
  <style>
* {
    font-family: Arial, Sans;
}
p.h {
    margin-bottom: 0;
}
h1 {
    margin-top: 0;
}
div.footer {
    border-top: 1px solid #808080;
    padding: 10px;
}
div.footer > p {
    text-align: center;
    margin: 0;
    padding: 0;
    font-size: small;
    color: #808080;
}
  </style>
 </head>
 <body>
  <p class="h">InfoGrid&trade; JavaDoc:</p>
  <h1>Project ${PROJECT}</h1>
  <p>This project contains the following parts:</p>
  <ul>
END

for p in ${PARTS}; do
    cat <<END
   <li><a href="../../$p/dist/javadoc/index.html">$p</a></li>
END
done

cat <<END
  </ul>
  <div class="footer">
    <p>&copy; 2001-2011 NetMesh Inc. All rights reserved. NetMesh and InfoGrid are trademarks or registered
       trademarks of NetMesh Inc.</p>
    <p><a href="http://infogrid.org/">Learn more</a> about InfoGrid&trade;.</p>
  </div>
 </head>
</html>
END
