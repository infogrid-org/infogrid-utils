<%@    page contentType="text/html"
 %><%@ page pageEncoding="UTF-8"
 %><%@ taglib prefix="set"   uri="/v/org/infogrid/jee/taglib/mesh/set/set.tld"
 %><%@ taglib prefix="mesh"  uri="/v/org/infogrid/jee/taglib/mesh/mesh.tld"
 %><%@ taglib prefix="candy" uri="/v/org/infogrid/jee/taglib/candy/candy.tld"
 %><%@ taglib prefix="u"     uri="/v/org/infogrid/jee/taglib/util/util.tld"
 %><%@ taglib prefix="v"     uri="/v/org/infogrid/jee/taglib/viewlet/viewlet.tld"
 %><%@ taglib prefix="c"     uri="http://java.sun.com/jsp/jstl/core"
 %><%@ taglib prefix="tmpl"  uri="/v/org/infogrid/jee/taglib/templates/templates.tld"
 %><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
 <head>
  <tmpl:inline sectionName="html-title"/>
  <link rel="stylesheet" href="${CONTEXT}/s/templates/default/master.css" type="text/css" />
  <link rel="stylesheet" href="${CONTEXT}/s/templates/default/layout.css" type="text/css" />
  <link rel="stylesheet" href="${CONTEXT}/s/templates/default/color.css"  type="text/css" />
  <link rel="stylesheet" href="${CONTEXT}/v/org/infogrid/jee/taglib/mesh/RefreshTag.css" type="text/css" />
  <tmpl:inline sectionName="html-head"/>
 </head>
 <body>
  <div id="canvas-top">
   <div id="canvas-app-row">
    <div class="canvas-main">
     <a class="infogrid" href="http://infogrid.org/"><img src="${CONTEXT}/s/images/infogrid-medium.png" alt="[InfoGrid logo]" /></a>
     <a href="${CONTEXT}/"><img id="app-logo" src="${CONTEXT}/s/images/meshworld.png" alt="[Logo]" /></a>
     <h1><a href="${CONTEXT}/">The Mesh World</a></h1>
    </div>
   </div>
  </div>
  <div id="canvas-middle">
   <div class="canvas-main">
    <tmpl:ifErrors>
     <div class="errors">
      <h2>Errors:</h2>
      <tmpl:inlineErrors stringRepresentation="Html"/>
     </div>
    </tmpl:ifErrors>
    <mesh:refresh>Reload page</mesh:refresh>
    <tmpl:inline sectionName="text-default"/>
   </div>
  </div>
  <div id="canvas-bottom">
   <div class="canvas-main footnote">
    <p>&copy; 2001-2010 NetMesh Inc. All rights reserved. NetMesh and InfoGrid are trademarks or registered
       trademarks of NetMesh Inc.</p>
    <p>Silk Icons from <a href="http://www.famfamfam.com/lab/icons/silk/">famfamfam.com</a> using Creative Commons license.
       <a href="http://infogrid.org/">Learn more</a> about InfoGrid&trade;.</p>
   </div>
  </div>
 </body>
</html>