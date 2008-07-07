<%@ page
  contentType="text/html; charset=UTF-8"
  import="javax.servlet.*"
  import="javax.servlet.http.*"
  import="java.io.*"
  import="java.text.*"
  import="java.util.*"
  import="java.text.DecimalFormat"
  import="org.apache.hadoop.mapred.*"
  import="org.apache.hadoop.util.*"
  import="org.apache.hadoop.dfs.JspHelper"
%>

<%
  JobTracker tracker = (JobTracker) application.getAttribute("job.tracker");
  String trackerName = 
           StringUtils.simpleHostname(tracker.getJobTrackerMachine());
%>
<%!
  private static final String PRIVATE_ACTIONS_KEY 
		= "webinterface.private.actions";
 
  private void printTaskSummary(JspWriter out,
                                String jobId,
                                String kind,
                                double completePercent,
                                TaskInProgress[] tasks
                               ) throws IOException {
    int totalTasks = tasks.length;
    int runningTasks = 0;
    int finishedTasks = 0;
    int killedTasks = 0;
    int failedTaskAttempts = 0;
    int killedTaskAttempts = 0;
    for(int i=0; i < totalTasks; ++i) {
      TaskInProgress task = tasks[i];
      if (task.isComplete()) {
        finishedTasks += 1;
      } else if (task.isRunning()) {
        runningTasks += 1;
      } else if (task.wasKilled()) {
        killedTasks += 1;
      }
      failedTaskAttempts += task.numTaskFailures();
      killedTaskAttempts += task.numKilledTasks();
    }
    int pendingTasks = totalTasks - runningTasks - killedTasks - finishedTasks; 
    out.print("<tr><th><a href=\"jobtasks.jsp?jobid=" + jobId + 
              "&type="+ kind + "&pagenum=1\">" + kind + 
              "</a></th><td align=\"right\">" + 
              StringUtils.formatPercent(completePercent, 2) +
              JspHelper.percentageGraph((int)(completePercent * 100), 80) +
              "</td><td align=\"right\">" + 
              totalTasks + 
              "</td><td align=\"right\">" + 
              ((pendingTasks > 0) 
               ? "<a href=\"jobtasks.jsp?jobid=" + jobId + "&type="+ kind + 
                 "&pagenum=1" + "&state=pending\">" + pendingTasks + "</a>"
               : "0") + 
              "</td><td align=\"right\">" + 
              ((runningTasks > 0) 
               ? "<a href=\"jobtasks.jsp?jobid=" + jobId + "&type="+ kind + 
                 "&pagenum=1" + "&state=running\">" + runningTasks + "</a>" 
               : "0") + 
              "</td><td align=\"right\">" + 
              ((finishedTasks > 0) 
               ?"<a href=\"jobtasks.jsp?jobid=" + jobId + "&type="+ kind + 
                "&pagenum=1" + "&state=completed\">" + finishedTasks + "</a>" 
               : "0") + 
              "</td><td align=\"right\">" + 
              ((killedTasks > 0) 
               ?"<a href=\"jobtasks.jsp?jobid=" + jobId + "&type="+ kind +
                "&pagenum=1" + "&state=killed\">" + killedTasks + "</a>"
               : "0") + 
              "</td><td align=\"right\">" + 
              ((failedTaskAttempts > 0) ? 
                  ("<a href=\"jobfailures.jsp?jobid=" + jobId + 
                   "&kind=" + kind + "&cause=failed\">" + failedTaskAttempts + 
                   "</a>") : 
                  "0"
                  ) + 
              " / " +
              ((killedTaskAttempts > 0) ? 
                  ("<a href=\"jobfailures.jsp?jobid=" + jobId + 
                   "&kind=" + kind + "&cause=killed\">" + killedTaskAttempts + 
                   "</a>") : 
                  "0"
                  ) + 
              "</td></tr>\n");
  }
  
  private void printConfirm(JspWriter out, String jobId) throws IOException{
    String url = "jobdetails.jsp?jobid=" + jobId;
    out.print("<html><head><META http-equiv=\"refresh\" content=\"15;URL="
        + url+"\"></head>"
        + "<body><h3> Are you sure you want to kill " + jobId
        + " ?<h3><br><table border=\"0\"><tr><td width=\"100\">"
        + "<a href=\"" +  url + "&action=kill"
        + "\">Kill</a></td><td width=\"100\"><a href=\"" + url
        + "\">Cancel </a></td></tr></table></body></html>");
  }
  
%>       
<%   
    String jobId = request.getParameter("jobid"); 
    String refreshParam = request.getParameter("refresh");
    
    int refresh = 60; // refresh every 60 seconds by default
    if (refreshParam != null) {
        try {
            refresh = Integer.parseInt(refreshParam);
        }
        catch (NumberFormatException ignored) {
        }
    }
    
    JobInProgress job = (JobInProgress) tracker.getJob(jobId);
    
    String action = request.getParameter("action");
    if("changeprio".equalsIgnoreCase(action)) {
      tracker.setJobPriority(jobId, 
                             JobPriority.valueOf(request.getParameter("prio")));
    }
    
    if(JspHelper.conf.getBoolean(PRIVATE_ACTIONS_KEY, false)) {
        action = request.getParameter("action");
	    if(action!=null && action.equalsIgnoreCase("confirm")) {
  	      printConfirm(out, jobId);
    	    return;
	    }
  	    else if(action != null && action.equalsIgnoreCase("kill")) {
	      tracker.killJob(jobId);
	    }
    }
%>

<%@page import="org.apache.hadoop.mapred.StatusHttpServer.TaskGraphServlet"%>
<html>
<head>
  <% 
  if (refresh != 0) {
      %>
      <meta http-equiv="refresh" content="<%=refresh%>">
      <%
  }
  %>
<title>Hadoop <%=jobId%> on <%=trackerName%></title>
<link rel="stylesheet" type="text/css" href="/static/hadoop.css">
</head>
<body>
<h1>Hadoop <%=jobId%> on <a href="jobtracker.jsp"><%=trackerName%></a></h1>

<% 
    if (job == null) {
      out.print("<b>Job " + jobId + " not found.</b><br>\n");
      return;
    }
    JobProfile profile = job.getProfile();
    JobStatus status = job.getStatus();
    int runState = status.getRunState();
    int flakyTaskTrackers = job.getNoOfBlackListedTrackers();
    out.print("<b>User:</b> " + profile.getUser() + "<br>\n");
    out.print("<b>Job Name:</b> " + profile.getJobName() + "<br>\n");
    out.print("<b>Job File:</b> <a href=\"jobconf.jsp?jobid=" + jobId + "\">" 
              + profile.getJobFile() + "</a><br>\n");
    if (runState == JobStatus.RUNNING) {
      out.print("<b>Status:</b> Running<br>\n");
      out.print("<b>Started at:</b> " + new Date(job.getStartTime()) + "<br>\n");
      out.print("<b>Running for:</b> " + StringUtils.formatTimeDiff(
          System.currentTimeMillis(), job.getStartTime()) + "<br>\n");
    } else {
      if (runState == JobStatus.SUCCEEDED) {
        out.print("<b>Status:</b> Succeeded<br>\n");
        out.print("<b>Started at:</b> " + new Date(job.getStartTime()) + "<br>\n");
        out.print("<b>Finished at:</b> " + new Date(job.getFinishTime()) +
                  "<br>\n");
        out.print("<b>Finished in:</b> " + StringUtils.formatTimeDiff(
            job.getFinishTime(), job.getStartTime()) + "<br>\n");
      } else if (runState == JobStatus.FAILED) {
        out.print("<b>Status:</b> Failed<br>\n");
        out.print("<b>Started at:</b> " + new Date(job.getStartTime()) + "<br>\n");
        out.print("<b>Failed at:</b> " + new Date(job.getFinishTime()) +
                  "<br>\n");
        out.print("<b>Failed in:</b> " + StringUtils.formatTimeDiff(
            job.getFinishTime(), job.getStartTime()) + "<br>\n");
      }
    }
    if (flakyTaskTrackers > 0) {
      out.print("<b>Black-listed TaskTrackers:</b> " + 
          "<a href=\"jobblacklistedtrackers.jsp?jobid=" + jobId + "\">" +
          flakyTaskTrackers + "</a><br>\n");
    }
    out.print("<hr>\n");
    out.print("<table border=2 cellpadding=\"5\" cellspacing=\"2\">");
    out.print("<tr><th>Kind</th><th>% Complete</th><th>Num Tasks</th>" +
              "<th>Pending</th><th>Running</th><th>Complete</th>" +
              "<th>Killed</th>" +
              "<th><a href=\"jobfailures.jsp?jobid=" + jobId + 
              "\">Failed/Killed<br>Task Attempts</a></th></tr>\n");
    printTaskSummary(out, jobId, "map", status.mapProgress(), 
                     job.getMapTasks());
    printTaskSummary(out, jobId, "reduce", status.reduceProgress(),
                     job.getReduceTasks());
    out.print("</table>\n");
    
    %>
    <p/>
    <table border=2 cellpadding="5" cellspacing="2">
    <tr>
      <th><br/></th>
      <th>Counter</th>
      <th>Map</th>
      <th>Reduce</th>
      <th>Total</th>
    </tr>
    <%
    Counters mapCounters = job.getMapCounters();
    Counters reduceCounters = job.getReduceCounters();
    Counters totalCounters = job.getCounters();
    
    for (String groupName : totalCounters.getGroupNames()) {
      Counters.Group totalGroup = totalCounters.getGroup(groupName);
      Counters.Group mapGroup = mapCounters.getGroup(groupName);
      Counters.Group reduceGroup = reduceCounters.getGroup(groupName);
      
      Format decimal = new DecimalFormat();
      
      boolean isFirst = true;
      for (Counters.Counter counter : totalGroup) {
        String name = counter.getDisplayName();
        String mapValue = decimal.format(mapGroup.getCounter(name));
        String reduceValue = decimal.format(reduceGroup.getCounter(name));
        String totalValue = decimal.format(counter.getCounter());
        %>
        <tr>
          <%
          if (isFirst) {
            isFirst = false;
            %>
            <td rowspan="<%=totalGroup.size()%>"><%=totalGroup.getDisplayName()%></td>
            <%
          }
          %>
          <td><%=name%></td>
          <td align="right"><%=mapValue%></td>
          <td align="right"><%=reduceValue%></td>
          <td align="right"><%=totalValue%></td>
        </tr>
        <%
      }
    }
    %>
    </table>

<hr>Map Completion Graph - 
<%
if("off".equals(request.getParameter("map.graph"))) {
  session.setAttribute("map.graph", "off");
} else if("on".equals(request.getParameter("map.graph"))){
  session.setAttribute("map.graph", "on");
}
if("off".equals(request.getParameter("reduce.graph"))) {
  session.setAttribute("reduce.graph", "off");
} else if("on".equals(request.getParameter("reduce.graph"))){
  session.setAttribute("reduce.graph", "on");
}

if("off".equals(session.getAttribute("map.graph"))) { %>
<a href="/jobdetails.jsp?jobid=<%=jobId%>&refresh=<%=refresh%>&map.graph=on" > open </a>
<%} else { %> 
<a href="/jobdetails.jsp?jobid=<%=jobId%>&refresh=<%=refresh%>&map.graph=off" > close </a>
<br><embed src="/taskgraph?type=map&jobid=<%=jobId%>" 
       width="<%=StatusHttpServer.TaskGraphServlet.width + 2 * StatusHttpServer.TaskGraphServlet.xmargin%>" 
       height="<%=StatusHttpServer.TaskGraphServlet.height + 3 * StatusHttpServer.TaskGraphServlet.ymargin%>"
       style="width:100%" type="image/svg+xml" pluginspage="http://www.adobe.com/svg/viewer/install/" />
<%}%>

<%if(job.getReduceTasks().length > 0) { %>
<hr>Reduce Completion Graph -
<%if("off".equals(session.getAttribute("reduce.graph"))) { %>
<a href="/jobdetails.jsp?jobid=<%=jobId%>&refresh=<%=refresh%>&reduce.graph=on" > open </a>
<%} else { %> 
<a href="/jobdetails.jsp?jobid=<%=jobId%>&refresh=<%=refresh%>&reduce.graph=off" > close </a>
 
 <br><embed src="/taskgraph?type=reduce&jobid=<%=jobId%>" 
       width="<%=StatusHttpServer.TaskGraphServlet.width + 2 * StatusHttpServer.TaskGraphServlet.xmargin%>" 
       height="<%=StatusHttpServer.TaskGraphServlet.height + 3 * StatusHttpServer.TaskGraphServlet.ymargin%>" 
       style="width:100%" type="image/svg+xml" pluginspage="http://www.adobe.com/svg/viewer/install/" />
<%} }%>

<hr>Change priority from <%=job.getPriority()%> to: 
<%
  JobPriority jobPrio = job.getPriority();
  for (JobPriority prio : JobPriority.values()) {
    if(jobPrio != prio) {
      %> <a style="margin-left: 5px; margin-right: 5px;" href="jobdetails.jsp?action=changeprio&jobid=<%=jobId%>&prio=<%=prio%>"> <%=prio%> </a> <%
    }
  }
%>
</br>
    
<% if(JspHelper.conf.getBoolean(PRIVATE_ACTIONS_KEY, false) 
    	&& runState == JobStatus.RUNNING) { %>
	<br/><a href="jobdetails.jsp?action=confirm&jobid=<%=jobId%>"> Kill this job </a>
<% } %>

<hr>

<hr>
<a href="jobtracker.jsp">Go back to JobTracker</a><br>
<%
out.println(ServletUtil.htmlFooter());
%>
