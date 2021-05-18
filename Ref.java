import java.util.*;
import java.net.*;

public class Ref {
    public URL url;
    public String tag;
    public String type;

    public static final String TYPE_LINK = "Link";
    public static final String TYPE_FORM = "Form";
    public static final String TYPE_OTHER = "Other";
    public static String Types[] = {
	"Link", "Form", "Other"
    };

    public Ref(URL u, String t, String tg) {
	url = u; type = t; tag = tg;
    }

    public String toString() {
	StringBuffer buf = new StringBuffer();
	buf.append("Ref[URL:");
	buf.append(url.toString());
	buf.append(", Type:");
	buf.append(type);
	buf.append(", Tag:");
	buf.append(tag);
	buf.append("]");
	return buf.toString();
    }
}

