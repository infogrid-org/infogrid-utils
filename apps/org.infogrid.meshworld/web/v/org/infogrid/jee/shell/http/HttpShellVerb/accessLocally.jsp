<candy:overlay id="org-infogrid-jee-shell-http-HttpShellVerb-accessLocally">
 <form method="post" action="${Viewlet.postUrl}">
  <h2>Open a remote MeshObject</h2>
  <table>
   <tr>
    <td class="label">Identifier:</td>
    <td>
     <input class="subject" name="mesh.subject" size="32"/>
     <input type="hidden" name="mesh.verb" value="accessLocally" />
    </td>
   </tr>
   <tr>
    <td colspan="2">
     <table class="dialog-buttons">
      <tr>
       <td><input type="submit" value="Open" /></td>
       <td><a href="javascript:overlay_hide( 'org-infogrid-jee-shell-http-HttpShellVerb-accessLocally' )">Cancel</a></td>
      </tr>
     </table>
    </td>
   </tr>
  </table>
 </form>
</candy:overlay>