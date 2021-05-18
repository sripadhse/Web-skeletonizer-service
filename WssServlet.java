import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class WssServlet extends HttpServlet
{
    public static final String PARM = "url";

    /**
     * Just call the doGet method.
     */
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, java.io.IOException
    {
	doGet(req,resp);
    }


    public void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, java.io.IOException
    {
	resp.setContentType("text/html");
	
	String subjectURL = req.getParameter(PARM);
	HttpSession sess = req.getSession(true);
	PrintWriter out = resp.getWriter();

	// Every page has four parts: title/header, intro,
	// results (if any), and query form/history.
	sendBeginning(subjectURL, out, sess);
	
	if (subjectURL == null) 
	    sendIntro(out, sess);
	else {
	    boolean okay = false;
	    LinkHandler ln = null;
	    URL su = null;
	    try {
		su = new URL(subjectURL);
	    }
	    catch (MalformedURLException mue) {
		sendError("Could not process subject URL " + subjectURL, out, mue);
	    }
	    if (su != null) {
		try {
		    ln = new LinkHandler(su);
		}
		catch (Exception e) {
		    sendError("Could not perform analysis of " + subjectURL, out, e);
		}
	    }
	    if (ln != null) {
		Hashtable headers = new Hashtable();
		Vector r = ln.process(headers);
		sendResults(subjectURL, out, headers, r, req);
		Vector hist = (Vector)(sess.getValue("history"));
		if (hist == null) hist = new Vector();
		if (!hist.contains(subjectURL)) {
		    hist.addElement(subjectURL);
		    sess.putValue("history", hist);
		}
	    }
	}

	Vector hist = (Vector)(sess.getValue("history"));
	sendForm(subjectURL, out, hist, req);

	sendEnd(out);
    }


    void sendBeginning(String subjectURL, PrintWriter out, HttpSession sess) {
	out.println("<html>");
	out.println("<head>");
	out.println("<title>Web Skeletonizer Servlet (Version 0.1)");
	if (subjectURL != null) {
	    out.println(" - ");
	    out.println("subjectURL");
	}
	out.println("</title>");
	out.println("</head>");
	out.println("<body bgcolor='FFFFCC'>");
	out.println("<h2>WSS Servlet</h2>");
    }

    void sendEnd(PrintWriter out) {
	out.println("<hr>");
	out.println("<center><font size='-1'>");
	out.println("</font></center>");
	out.println("</body>");
	out.println("</html>");
    }

    void sendForm(String subjectURL, PrintWriter out, Vector hist, HttpServletRequest req)
    {
	out.println("<hr width='96%'>");
	out.println("<h3>Query Form</h3>");
	out.println("<ul>");
	out.print("<form method=GET action=\"");
	out.print(req.getRequestURI());
	out.println("\">URL: ");
	out.println("<input type=TEXT name="+PARM+" size=48>");
	out.println("<p>");
	out.print(" &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ");
	out.println("<input type=SUBMIT> &nbsp; <input type=RESET>");
	out.println("</form>");
	out.println("<p>");
	out.println("Type a URL into the text field and click on the Submit button.");
	if (hist != null && hist.size() > 0) {
	    out.println("The list below shows URLs you have already visited.  Click on");
	    out.println("one to visit it again.");
	    out.println("<ul>");
	    Enumeration en = hist.elements();
	    while(en.hasMoreElements()) {
		out.print("<li>");
		String u = (String)(en.nextElement());
		out.print("<a href=\"");
		out.print(req.getRequestURI()+"?"+PARM+"=");
		out.print(URLEncoder.encode(u));
		out.println("\">"+u+"</a>");
		out.println("</li>");
	    }
	    out.println("</ul>");
	}
	out.println("</ul>");
    }

    void sendError(String remark, PrintWriter out, Exception e) {
	out.println("<hr width='96%'>");
	out.println("<h3>Error Report</h3>");
	out.println("<ul>");
	out.println("<p>");
	out.println("Sorry, ");
	out.println(remark);
	out.println("<br>");
	if (e != null) {
	    out.println("Exception was: <tt>");
	    out.println(e.toString());
	    out.println("</tt>");
	}
	out.println("</ul>");
    }

    void sendResults(String subjectURL, PrintWriter out, Hashtable headers, 
		     Vector r, HttpServletRequest req) {
	Enumeration en;
	out.println("<hr width='96%'>");
	out.println("<h3>Results of Analyzing<br>" + subjectURL + "</h3>");
	out.println("<ul>");
	out.println("Information about the web page:");
	out.println("<ul>");
	out.println("<table cellspacing=1 border=2>");
	out.println("<tr><th bgcolor='FFFFFF'>Header</th><th bgcolor='FFFFFF'>Value</th></tr>");
	for(en = headers.keys(); en.hasMoreElements(); ) {
	    out.print("<tr><td>");
	    String ki = (String)(en.nextElement());
	    out.print(ki);
	    out.print("</td>\n<td>");
	    out.print((String)(headers.get(ki)));
	    out.println("</td></tr>");
	}
	out.println("</table>");
	out.println("</ul>");
	out.println("<p> &nbsp; </p>");
	out.println("<p>");
	out.println("Links and references on the web page:");
	out.println("<ol>");
	for(en = r.elements(); en.hasMoreElements(); ) {
	    Ref ref = (Ref)(en.nextElement());
	    out.println("<p>");
	    out.println("<li>URL: ");
	    out.print("<a target=wssview href=\"");
	    out.print(ref.url.toExternalForm());
	    out.println("\"><b>"+ref.url.toExternalForm()+"</b></a>");
	    out.println("<br>");
	    out.println("Tag: &lt;"+ref.tag+"&gt; (type: " + ref.type + ")");
	    if (probablySkeletonizable(ref)) {
		out.print(" &nbsp;&nbsp; <a href=\"");
		out.print(req.getRequestURI() + "?" + PARM + "=");
		out.print(URLEncoder.encode(ref.url.toExternalForm()));
		out.println("\">&gt;&gt;analyze it</a>");
	    }
	    out.println("</li>");
	}
	out.println("</ol>");
	out.println("</ul>");
      return;
    }

    void sendIntro(PrintWriter out, HttpSession sess) {
	out.println("<h3>Introduction</h3>");
	out.println("<ul>");
	out.println("<p>");
	out.println("The Web Skeletonizing Servlet is a simple server-side");
	out.println("application that produces a link skeleton for a web page.");
	out.println("Basically, it extracts the links, image references, and");
	out.println("other connectivity from a HTML web page, and presents it");
	out.println("in the form of a list.");
	out.println("<p>");
	out.println("To use this service, simply enter the URL of a web page");
	out.println("into the form below.  If you have used the service before,");
	out.println("then URLs you visited recently should be listed below the");
	out.println("form.");
	out.println("</ul>");
	return;
    }

    /**
     * Return false if it seems unlikely that this servlet
     * would be able to analyze the given reference, or
     * true otherwise.  Things we cannot analyze include
     * images, scripts, stylesheets.
     */
    boolean probablySkeletonizable(Ref ref) {
	if (ref.tag.equalsIgnoreCase("img")) return false;
	if (ref.tag.equalsIgnoreCase("embed")) return false;
	if (ref.tag.equalsIgnoreCase("script")) return false;
	if (ref.url.toString().indexOf(".gif") > 0) return false;
	if (ref.url.toString().indexOf(".GIF") > 0) return false;
	if (ref.url.toString().indexOf(".jpg") > 0) return false;
	if (ref.url.toString().indexOf(".JPG") > 0) return false;
	if (ref.url.toString().indexOf(".css") > 0) return false;
	if (ref.url.toString().indexOf(".map") > 0) return false;
	if (ref.url.toString().indexOf(".wav") > 0) return false;
	return true;
    }

}

