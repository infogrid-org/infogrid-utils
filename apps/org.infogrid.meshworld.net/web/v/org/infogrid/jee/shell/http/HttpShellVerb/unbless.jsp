<candy:overlay id="org-infogrid-jee-shell-http-HttpShellVerb-unbless">
 <form method="post" action="${Viewlet.postUrl}">
  <h1>Do you really want to unbless this MeshObject?</h1>
  <table>
   <tr>
    <td class="label">MeshObject:</td>
    <td><input class="subject" name="mesh.subject" size="32" readonly="readonly" />
     <input type="hidden" name="mesh.verb" value="unbless" />
    </td>
   </tr>
   <tr>
    <td class="label">Type:</td>
    <td>
     <input class="subjecttype" name="mesh.subjecttype" size="32" />
    </td>
   </tr>
   <tr>
    <td colspan="2">
     <table class="dialog-buttons">
      <tr>
       <td><input type="submit" value="Unbless" /></td>
       <td><a href="javascript:overlay_hide( 'org-infogrid-jee-shell-http-HttpShellVerb-unbless' )">Cancel</a></td>
      </tr>
     </table>
    </td>
   </tr>
  </table>
 </form>
</candy:overlay>