<%@    page contentType="text/html"
 %><%@ taglib prefix="mesh"  uri="/v/org/infogrid/jee/taglib/mesh/mesh.tld"
 %><%@ taglib prefix="candy" uri="/v/org/infogrid/jee/taglib/candy/candy.tld"
 %><%@ taglib prefix="u"     uri="/v/org/infogrid/jee/taglib/util/util.tld"
 %><%@ taglib prefix="v"     uri="/v/org/infogrid/jee/taglib/viewlet/viewlet.tld"
 %><%@ taglib prefix="tmpl"  uri="/v/org/infogrid/jee/taglib/templates/templates.tld"
 %><%@ taglib prefix="c"     uri="http://java.sun.com/jsp/jstl/core"
 %>
<tmpl:stylesheet href="${CONTEXT}/v/org/infogrid/jee/viewlet/propertysheet/PropertySheetViewlet.css"/>
<tmpl:stylesheet href="${CONTEXT}/v/org/infogrid/jee/taglib/viewlet/ChangeViewletStateTag.css"/>

<v:viewletAlternatives />
<v:changeViewletState viewletStates="edit" display="compact"/>
<v:viewlet>
 <table class="audit"> <!-- IE is unable to render float:right correctly, so here is a table for you -->
  <tr>
   <td>
    <v:ifState viewletState="edit">
     <div class="slide-in-button"><a href="javascript:overlay_show( 'org-infogrid-jee-shell-http-HttpShellVerb-delete', { 'shell.subject' : '<mesh:meshObjectId meshObjectName="Subject" stringRepresentation="Plain" filter="true" />' } )" title="Delete this MeshObject"><img src="${CONTEXT}/s/images/trash.png" alt="Delete"/></a></div>
    </v:ifState>
    <h1>Property Sheet for: <mesh:meshObjectId meshObjectName="Subject" stringRepresentation="Html" maxLength="30"/></h1>
   </td>
   <td class="audit">
 <%@ include file="/v/org/infogrid/jee/viewlet/propertysheet/PropertySheetViewlet/audit.jsp" %>
   </td>
  </tr>
 </table>

<%@ include file="/v/org/infogrid/jee/viewlet/propertysheet/PropertySheetViewlet/attributes.jsp" %>
<%@ include file="/v/org/infogrid/jee/viewlet/propertysheet/PropertySheetViewlet/neighbors.jsp" %>

 <v:ifState viewletState="edit">
  <u:safeFormHiddenInput/>
  <table class="dialog-buttons">
   <tr>
    <td><button type="submit" name="ViewletStateTransition" value="do-cancel">Stop Editing</button></td>
    <td><button type="submit" name="ViewletStateTransition" value="do-commit">Save</button></td>
   </tr>
  </table>
 </v:ifState>
</v:viewlet>

 <v:ifState viewletState="edit">
<%@ include file="/v/org/infogrid/jee/shell/http/HttpShellVerb.jsp" %>
 </v:ifState>

