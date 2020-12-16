import java.util.regex.Matcher;
import java.util.regex.Pattern;




public class NameTest{

private static final Pattern nameParser =
      Pattern.compile("([^/@]+)(/([^/@]+))?(@([^/@]+))?");

private static final Pattern ruleParser =
    Pattern.compile("\\s*((DEFAULT)|(RULE:\\[(\\d*):([^\\]]*)](\\(([^)]*)\\))?"+
                    "(s/([^/]*)/([^/]*)/(g)?)?))/?(L)?");


private List<Rule> parseRules(String rules) {
    List<Rule> result = new ArrayList<Rule>();
    String remaining = rules.trim();
    while (remaining.length() > 0) {
      Matcher matcher = ruleParser.matcher(remaining);
      if (!matcher.lookingAt()) {
        throw new IllegalArgumentException("Invalid rule: " + remaining);
      }
      if (matcher.group(2) != null) {
        result.add(new Rule());
      } else {
        result.add(new Rule(Integer.parseInt(matcher.group(4)),
                            matcher.group(5),
                            matcher.group(7),
                            matcher.group(9),
                            matcher.group(10),
                            "g".equals(matcher.group(11)),
                            "L".equals(matcher.group(12))));
      }
      remaining = remaining.substring(matcher.end());
    }
    return result;
  }

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
