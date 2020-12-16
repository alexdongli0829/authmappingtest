
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
 
public class RuleTest 
{

public static final String MECHANISM_HADOOP = "hadoop";


@SuppressWarnings("serial")
  public static class BadFormatString extends IOException {
    BadFormatString(String msg) {
      super(msg);
    }
    BadFormatString(String msg, Throwable err) {
      super(msg, err);
    }
  }

  @SuppressWarnings("serial")
  public static class NoMatchingRule extends IOException {
    NoMatchingRule(String msg) {
      super(msg);
    }
  }
  private static final Pattern nameParser =
      Pattern.compile("([^/@]+)(/([^/@]+))?(@([^/@]+))?");

  private static Pattern parameterPattern =
    Pattern.compile("([^$]*)(\\$(\\d*))?");

  private static final Pattern ruleParser =
    Pattern.compile("\\s*((DEFAULT)|(RULE:\\[(\\d*):([^\\]]*)](\\(([^)]*)\\))?"+
                    "(s/([^/]*)/([^/]*)/(g)?)?))/?(L)?");
  private static final Pattern nonSimplePattern = Pattern.compile("[/@]");

private static class Rule {
    private final boolean isDefault;
    private final int numOfComponents;
    private final String format;
    private final Pattern match;
    private final Pattern fromPattern;
    private final String toPattern;
    private final boolean repeat;
    private final boolean toLowerCase;

    Rule() {
      isDefault = true;
      numOfComponents = 0;
      format = null;
      match = null;
      fromPattern = null;
      toPattern = null;
      repeat = false;
      toLowerCase = false;
    }

    Rule(int numOfComponents, String format, String match, String fromPattern,
         String toPattern, boolean repeat, boolean toLowerCase) {
      isDefault = false;
      this.numOfComponents = numOfComponents;
      this.format = format;
      this.match = match == null ? null : Pattern.compile(match);
      this.fromPattern =
        fromPattern == null ? null : Pattern.compile(fromPattern);
      this.toPattern = toPattern;
      this.repeat = repeat;
      this.toLowerCase = toLowerCase;
    }

    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder();
      if (isDefault) {
        buf.append("DEFAULT");
      } else {
        buf.append("RULE:[");
        buf.append(numOfComponents);
        buf.append(':');
        buf.append(format);
        buf.append(']');
        if (match != null) {
          buf.append('(');
          buf.append(match);
          buf.append(')');
        }
        if (fromPattern != null) {
          buf.append("s/");
          buf.append(fromPattern);
          buf.append('/');
          buf.append(toPattern);
          buf.append('/');
          if (repeat) {
            buf.append('g');
          }
        }
        if (toLowerCase) {
          buf.append("/L");
        }
      }
      return buf.toString();
    }
    static String replaceParameters(String format,
                                    String[] params) throws BadFormatString {
      Matcher match = parameterPattern.matcher(format);
      int start = 0;
      StringBuilder result = new StringBuilder();
      while (start < format.length() && match.find(start)) {
        result.append(match.group(1));
        String paramNum = match.group(3);
        if (paramNum != null) {
          try {
            int num = Integer.parseInt(paramNum);
            if (num < 0 || num > params.length) {
              throw new BadFormatString("index " + num + " from " + format +
                                        " is outside of the valid range 0 to " +
                                        (params.length - 1));
            }
            result.append(params[num]);
          } catch (NumberFormatException nfe) {
            throw new BadFormatString("bad format in username mapping in " +
                                      paramNum, nfe);
          }

        }
        start = match.end();
      }
      return result.toString();
    }

    static String replaceSubstitution(String base, Pattern from, String to,
                                      boolean repeat) {
      Matcher match = from.matcher(base);
      if (repeat) {
        return match.replaceAll(to);
      } else {
        return match.replaceFirst(to);
      }
    }

    String apply(String[] params, String ruleMechanism) throws IOException {
      String result = null;
      if (isDefault) {
	//getDefaultRealm() replace with "default"
        if ("default".equals(params[0])) {
          result = params[1];
        }
      } else if (params.length - 1 == numOfComponents) {
        String base = replaceParameters(format, params);
	System.out.println(base);
        if (match == null || match.matcher(base).matches()) {
	  System.out.println("matchest");
          if (fromPattern == null) {
            result = base;
          } else {
            result = replaceSubstitution(base, fromPattern, toPattern,  repeat);
          }
        }
      }

      System.out.println("result now is:"+result);
      if (result != null
              && nonSimplePattern.matcher(result).find()
              && ruleMechanism.equalsIgnoreCase(MECHANISM_HADOOP)) {
        throw new NoMatchingRule("Non-simple name " + result +
                                 " after auth_to_local rule " + this);
      }
      if (toLowerCase && result != null) {
        result = result.toLowerCase(Locale.ENGLISH);
      }
      return result;
    }
  }


  static List<Rule> parseRules(String rules) {
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
  
    public static void main( String args[] ) throws IOException{
 


      // 按指定模式在字符串查找
      String line = "RULE:[1:$1@$0](.*@.*)s/@.*///L";
      String pattern = "\\s*((DEFAULT)|(RULE:\\[(\\d*):([^\\]]*)](\\(([^)]*)\\))?(s/([^/]*)/([^/]*)/(g)?)?))/?(L)?";
 

      //String[] params = new String[]{"dongaws", "test"};
      String[] params = new String[]{"test"};

      List<Rule> rules = parseRules(line);
      for(Rule r: rules) {
	      String result = r.apply(params, "hadoop");
	      System.out.println("final result:"+result);

      }
    
      // 创建 Pattern 对象
      Pattern r = Pattern.compile(pattern);
 
      // 现在创建 matcher 对象
      Matcher m = r.matcher(line);
      if (m.find( )) {
         System.out.println("Found value: " + m.group(4) ); 
         System.out.println("Found value: " + m.group(5) ); 
         System.out.println("Found value: " + m.group(7) ); 
         System.out.println("Found value: " + m.group(9) ); 
         System.out.println("Found value: " + m.group(10) ); 
         System.out.println("Found value: " + m.group(11) ); 
         System.out.println("Found value: " + m.group(12) ); 
      } else {
         System.out.println("NO MATCH");
      }
   }
}

