<%@    page contentType="text/html"
 %><%@ taglib prefix="set"   uri="/v/org/infogrid/jee/taglib/mesh/set/set.tld"
 %><%@ taglib prefix="mesh"  uri="/v/org/infogrid/jee/taglib/mesh/mesh.tld"
 %><%@ taglib prefix="candy" uri="/v/org/infogrid/jee/taglib/candy/candy.tld"
 %><%@ taglib prefix="u"     uri="/v/org/infogrid/jee/taglib/util/util.tld"
 %><%@ taglib prefix="v"     uri="/v/org/infogrid/jee/taglib/viewlet/viewlet.tld"
 %><%@ taglib prefix="tmpl"  uri="/v/org/infogrid/jee/taglib/templates/templates.tld"
 %><%@ taglib prefix="c"     uri="http://java.sun.com/jsp/jstl/core"
 %>
<tmpl:stylesheet href="${CONTEXT}/v/org/infogrid/jee/viewlet/meshbase/AllMeshObjectsViewlet.css"/>

<v:viewletAlternatives />
<mesh:refresh>Reload page</mesh:refresh>
<v:viewlet>
 <div class="slide-in-button"><a href="javascript:overlay_show( 'org-infogrid-jee-shell-http-HttpShellVerb-create', {} )" title="Create a MeshObject"><img src="${CONTEXT}/s/icons/add.png" alt="Create"/></a></div>
 <h1>All MeshObjects in the MeshBase</h1>
 <table class="set">
  <thead>
   <tr>
    <th>Identifier</th>
    <th>Types and Attributes</th>
    <th>Audit</th>
   </tr>
  </thead>
  <tbody>
   <c:forEach items="${Viewlet.cursorIterator}" var="current" varStatus="currentStatus">
    <u:rotatingTr varStatus="currentStatus" htmlClasses="bright,dark" firstRowHtmlClass="first" lastRowHtmlClass="last">
     <td>
      <div class="slide-in-button"><a href="javascript:overlay_show( 'org-infogrid-jee-shell-http-HttpShellVerb-delete', { 'mesh.subject' : '<mesh:meshObjectId meshObjectName="current" stringRepresentation="Plain" filter="true" />' } )" title="Delete this MeshObject"><img src="${CONTEXT}/s/icons/trash.png" alt="Delete"/></a></div>
      <mesh:meshObjectLink meshObjectName="current"><mesh:meshObjectId meshObjectName="current" maxLength="30"/></mesh:meshObjectLink>
     </td>
     <td>
      <div class="slide-in-button"><a href="javascript:overlay_show( 'org-infogrid-jee-shell-http-HttpShellVerb-bless', { 'mesh.subject' : '<mesh:meshObjectId meshObjectName="current" stringRepresentation="Plain" filter="true" />' } )" title="Bless this MeshObject"><img src="${CONTEXT}/s/icons/add.png" alt="Add type"/></a></div>
      <ul class="types">
       <mesh:blessedByIterate meshObjectName="current" blessedByLoopVar="blessedBy">
        <li>
         <div class="slide-in-button"><a href="javascript:overlay_show( 'org-infogrid-jee-shell-http-HttpShellVerb-unbless', { 'mesh.subject' : '<mesh:meshObjectId meshObjectName="current" stringRepresentation="Plain" filter="true" />', 'mesh.subjecttype' : '<mesh:meshTypeId meshTypeName="blessedBy" stringRepresentation="Plain" filter="true" />' } )" title="Unbless this MeshObject"><img src="${CONTEXT}/s/icons/trash.png" alt="Delete"/></a></div>
         <mesh:type meshTypeName="blessedBy"/>
         <ul class="properties">
          <mesh:propertyIterate meshObjectName="current" meshTypeName="blessedBy" propertyTypeLoopVar="propertyType" propertyValueLoopVar="propertyValue">
          <li><mesh:type          meshTypeName="propertyType" />:&nbsp;<mesh:propertyValue propertyValueName="propertyValue" /></li>
          </mesh:propertyIterate>
         </ul>
        </li>
       </mesh:blessedByIterate>
      </ul>
     </td>
     <td>
      <table class="audit">
       <tr>
        <td class="label">Created:</td><td><mesh:timeCreated meshObjectName="current" /></td>
       </tr>
       <tr>
        <td class="label">Updated:</td><td><mesh:timeUpdated meshObjectName="current" /></td>
       </tr>
       <tr>
        <td class="label">Last&nbsp;read:</td><td><mesh:timeRead meshObjectName="current" /></td>
       </tr>
      </table>
     </td>
    </u:rotatingTr>
   </c:forEach>
  </tbody>
 </table>
 <%@ include file="/v/org/infogrid/jee/shell/http/HttpShellVerb.jsp" %>
</v:viewlet>
