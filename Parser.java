import java.io.*;
import java.util.*;
import java.net.*;

public interface Parser {
    public int parse(InputStream is, Vector refs, URL base);
}
