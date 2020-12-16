import java.util.regex.Matcher;
import java.util.regex.Pattern;




public class NameTest{

private static final Pattern nameParser =
      Pattern.compile("([^/@]+)(/([^/@]+))?(@([^/@]+))?");

public static void main(String[] args) throws Exception {
    
    String name = args[0];

    Matcher match = nameParser.matcher(name);
    String serviceName = null;
    String hostName = null;
    String realm = null;

    if (!match.matches()) {
      if (name.contains("@")) {
        throw new IllegalArgumentException("Malformed Kerberos name: " + name);
      } else {
        serviceName = name;
        hostName = null;
        realm = null;
      }
    } else {
      serviceName = match.group(1);
      hostName = match.group(3);
      realm = match.group(5);
    }
    System.out.println("serviceName:"+serviceName);
    System.out.println("hostname:"+hostName);
    System.out.println("realm:"+realm);
}
}
