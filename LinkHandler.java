import java.io.*;
import java.util.*;
import java.net.*;
import gnu.regexp.*;

public class LinkHandler 
{
    private URL url;
    private URLConnection uc;
    private static HtmlParser hp = null;

    private Vector refs;

 
    public LinkHandler(URL u) throws IOException 
    {
	if (hp == null) {
	    try {
		hp = new HtmlParser();
	    }
	    catch (gnu.regexp.REException gre) {
		throw new IOException("Regexp error!  Ack!");
	    }
	}
	url = u;
	uc = u.openConnection();
	uc.connect();
	String ct = uc.getContentType();
	if (ct == null ||
	    !(ct.equalsIgnoreCase("text/html") ||
	      ct.equalsIgnoreCase("text/plain"))) 
	    {
		if (uc instanceof HttpURLConnection) 
		    ((HttpURLConnection)uc).disconnect();
		else {
		    InputStream is = uc.getInputStream();
		    try { is.close(); } catch (IOException ie) { }
		}
		throw new 
	           IOException("Content type is not amenable to analysis: " + 
			       ((ct==null)?("None"):(ct)));
	}
	refs = new Vector();
    }

    /**
     * Process the contents of a URL.  Much of the work
     * takes place in HtmlParser.
     */
    public Vector process(Hashtable headers) {
	int cnt = 0;
	InputStream is = null;

	try { is = new BufferedInputStream(uc.getInputStream()); }
	catch (IOException ie) { }
	    
	cnt = hp.parse(is, refs, url);
	if (headers != null) {
	    if (uc instanceof HttpURLConnection) try {
		HttpURLConnection uch = (HttpURLConnection)uc;
		String rmsg = uch.getResponseMessage();
		headers.put("HTTP Response", 
			    "" + uch.getResponseCode() + " " +
			    ((rmsg==null)?(""):(rmsg)));
	    }
	    catch (Exception e2) { }
	    headers.put("content-type", uc.getContentType());
	    headers.put("content-length", uc.getContentLength() + "");
	    long d1 = uc.getDate();
	    if (d1 > 0)
		headers.put("date", (new Date(d1)).toString());
	    d1 = uc.getLastModified();
	    if (d1 > 0)
		headers.put("last-modified", (new Date(d1)).toString());
	    for(int hi = 0; true; hi++) {
		String ki = uc.getHeaderFieldKey(hi);
		if (ki != null) {
		    String vi = uc.getHeaderField(hi);
		    if (vi == null) vi = "";
		    headers.put(ki,vi);
		}
		else break;
	    }
	}
	if (cnt > 0)
	    return refs;
	else
	    return null;
    }

    public void dump(PrintStream ps, Hashtable headers, Vector refs) {
	ps.println("URL: " + url);
	ps.println("Headers hashtable: ");
	Enumeration en;
	for(en = headers.keys(); en.hasMoreElements(); ) {
	    String key = (String)(en.nextElement());
	    ps.println("\tKey: " + key + "    Value: " + 
		       headers.get(key).toString());
	}
	ps.println("Results refs: ");
	for(en = refs.elements(); en.hasMoreElements(); ) {
	    Ref r = (Ref)(en.nextElement());
	    ps.println("\t" + r.toString());
	}
	ps.println("--------------------");
    }


    public static void main(String [] args) {
	if (args.length < 1) {
	    System.out.println("Usage: java LinkHandlers url-string");
	    System.exit(0);
	}

	LinkHandler ln;
	System.out.println("URL to try: " + args[0]);
	try {
	    URL u = new URL(args[0]);
	    ln = new LinkHandler(u);
	    Vector v;
	    Hashtable h = new Hashtable();
	    v = ln.process(h);
	    ln.dump(System.out, h, v);
	}
	catch (Exception e) {
	    System.err.println("Error in doing link handling: " + e);
	    e.printStackTrace();
	}
    }
}
	
