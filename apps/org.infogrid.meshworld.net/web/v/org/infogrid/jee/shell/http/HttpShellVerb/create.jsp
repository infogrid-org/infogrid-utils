<candy:overlay id="org-infogrid-jee-shell-http-HttpShellVerb-create" htmlClass="org-infogrid-jee-shell-http-HttpShellVerb">
 <form method="post" action="${Viewlet.postUrl}">
  <h2>Create a MeshObject</h2>
  <table>
   <tr>
    <td rowspan="2" class="label">Identifier:</td>
    <td>
     <input class="subject" name="mesh.subject" size="32"/>
     <input type="hidden" name="mesh.verb" value="create" />
    </td>
   </tr>
   <tr>
    <td class="advice">(Empty Identifier will auto-create one)</td>
   </tr>
   <tr>
    <td colspan="2">
     <table class="dialog-buttons">
      <tr>
       <td><input type="submit" value="Create" /></td>
       <td><a href="javascript:overlay_hide( 'org-infogrid-jee-shell-http-HttpShellVerb-create' )">Cancel</a></td>
      </tr>
     </table>
    </td>
   </tr>
  </table>
 </form>
</candy:overlay>