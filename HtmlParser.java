import java.io.*;
import java.util.*;
import java.net.*;
import gnu.regexp.*;

public class HtmlParser implements Parser
{
    public static String types_table[][] = {
	{ "href", Ref.TYPE_LINK },
	{ "src", Ref.TYPE_OTHER },
	{ "archive", Ref.TYPE_OTHER },
	{ "action", Ref.TYPE_FORM },
    };

    private String re_pattern;
    private int re_flags;
    private RE  re;
    private URL baseURL;
    
    public HtmlParser() throws gnu.regexp.REException {
	re_pattern = "<\\s*([a-zA-Z0-9]+)[^>]+(";
	for(int i = 0; i < types_table.length; ) {
	    re_pattern += types_table[i][0];
	    i++;
	    if (i < types_table.length) re_pattern += "|";
	}
	re_pattern += ")";
	re_pattern += "\\s*=\\s*[\"']([^\"']+)[\"']";
	re_flags = RE.REG_ICASE;

	re = new RE(re_pattern, re_flags);
    }


    public int parse(InputStream is, Vector refs, URL base) {
	REMatchEnumeration ren;
	int cnt;
	String attr;
	String val;
	String typ;
	String tag;
	REMatch rem;

	if (is == null) return 0;
	ren = re.getMatchEnumeration(is);

	for(cnt = 0; ren.hasMoreMatches(); ) {
	    rem = ren.nextMatch();
	    tag = rem.toString(1);
	    attr = rem.toString(2);
	    val = rem.toString(3);
	    if (attr.length() == 0 || val.length() == 0) 
		continue;
	    if (val.startsWith("#")) 
		continue;
	    typ = Ref.TYPE_OTHER;
	    for(int ix = 0; ix < types_table.length; ix++) {
		if (attr.equalsIgnoreCase(types_table[ix][0])) {
		    typ = types_table[ix][1];
		    break;
		}
	    }
	    URL nu = null;
	    try {
		if (base != null) {
		    nu = new URL(base, val);
		}
		else {
		    nu = new URL(val);
		}
	    }
	    catch (MalformedURLException mue) { }
	    if (nu != null) {
		cnt++;
		refs.addElement(new Ref(nu, typ, tag));
	    }
	}
	try { is.close(); } catch (IOException ie) { }
	return cnt;
    }
}
