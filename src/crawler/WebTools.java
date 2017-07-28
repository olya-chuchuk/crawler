package crawler;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class consists of some utility functions 
 * @author olya-chuchuk
 */
public class WebTools {

    /**
     * Remove leading "www" from a URL's host if present.
     * 
     * @param url String representation of URL
     * @return String-url without leading "www"
     */
    public static String removeWwwFromUrl(String url) {
      int index = url.indexOf("://www.");
      if (index != -1) {
        return url.substring(0, index + 3) +
          url.substring(index + 7);
      }

      return (url);
    }
    
    /**
     * Download web page
     * 
     * @param pageUrl 
     * @return String representation of a given page
     */
    public static String downloadPage(URL pageUrl) {
       try {
          // Open connection to URL for reading.
          BufferedReader reader =
            new BufferedReader(new InputStreamReader(
              pageUrl.openStream(),"UTF-8"));

          // Read page into buffer.
          String line;
          StringBuffer pageBuffer = new StringBuffer();
          while ((line = reader.readLine()) != null) {
            pageBuffer.append(line);
          }
          return pageBuffer.toString();
       } catch (Exception e) {
       }

       return null;
    }
    
    /**
     * Check whether robot is allowed to access the given URL.
     * 
     * @param urlToCheck
     * @return
     */
    public static boolean isRobotAllowed(URL urlToCheck, 
            HashMap<String, ArrayList<String>> disallowListCache) {
        String host = urlToCheck.getHost().toLowerCase();

        // Retrieve host's disallow list from cache.
        ArrayList<String> disallowList =
                (ArrayList) disallowListCache.get(host);
        // If list is not in the cache, download and cache it.
        if (disallowList == null) {
            disallowList = new ArrayList<String>();

            try {
                URL robotsFileUrl =
                new URL("http://" + host + "/robots.txt");

                // Open connection to robot file URL for reading.
                BufferedReader reader =
                  new BufferedReader(new InputStreamReader(
                    robotsFileUrl.openStream()));

                // Read robot file, creating list of disallowed paths.
                String line;
                while ((line = reader.readLine()) != null) {
                    //System.out.println(line);
                  if (line.indexOf("Disallow:") == 0) {
                    String disallowPath =
                      line.substring("Disallow:".length());

                    // Check disallow path for comments and remove if present.
                    int commentIndex = disallowPath.indexOf("#");
                    if (commentIndex != - 1) {
                      disallowPath =
                        disallowPath.substring(0, commentIndex);
                    }

                    // Remove leading or trailing spaces from disallow path.
                    disallowPath = disallowPath.trim();

                    // Add disallow path to list.
                    if(disallowPath.length() != 0)
                    {
                        disallowList.add(disallowPath);
                    }
                  }
                }

                // Add new disallow list to cache.
                disallowListCache.put(host, disallowList);
              } catch (Exception e) {
                  /* Assume robot is allowed since an exception
                   is thrown if the robot file doesn't exist. */
                  return true;
              }
           }

            /* Loop through disallow list to see if the
               crawling is allowed for the given URL. */
            String file = urlToCheck.getFile();
            for (int i = 0; i < disallowList.size(); i++) {
              String disallow = (String) disallowList.get(i);
              if (file.startsWith(disallow)) {
                return false;
              }
            }

            return true;
    }
    

    /**
     * Verify URL format.
     * 
     * @param url
     * @return valid URL or null otherwise
     */
    public static URL verifyUrl(String url) {
      // Only allow HTTP URLs.
      if (!url.toLowerCase().startsWith("http://"))
        return null;

      // Verify format of URL.
      URL verifiedUrl = null;
      try {
        verifiedUrl = new URL(url);
      } catch (Exception e) {
        return null;
      }

      return verifiedUrl;
    }
    
    /**
     * Parse through page contents and retrieve links.
     * 
     * @param pageUrl
     * @param pageContents
     * @param crawledList
     * @return ArrayList<String> of retrieved links
     */
    public static ArrayList<String> retrieveLinks(
      URL pageUrl, String pageContents, HashSet<String> crawledList)
    {
      // Compile link matching pattern.
      Pattern p =
        Pattern.compile("<a\\s+href\\s*=\\s*\"?(.*?)[\"|>]", // for example <a href="http://mi.unicyb.kiev.ua/ru/">
          Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(pageContents);

      // Create list of link matches.
      ArrayList<String> linkList = new ArrayList<String>();
      while (m.find()) {
        String link = m.group(1).trim();

        // Skip empty links.
        if (link.length() < 1) {
          continue;
        }

        // Skip links that are just page anchors.
        if (link.charAt(0) == '#') {
          continue;
        }

        // Skip mailto links.
        if (link.indexOf("mailto:") != -1) {
          continue;
        }

        // Skip JavaScript links.
        if (link.toLowerCase().indexOf("javascript") != -1) {
          continue;
        }

        // Prefix absolute and relative URLs if necessary.
        if (link.indexOf("://") == -1) {
          // Handle absolute URLs.
          if (link.charAt(0) == '/') {
            link = "http://" + pageUrl.getHost() + link;
          // Handle relative URLs.
          } else {
            String file = pageUrl.getFile();
            if (file.indexOf('/') == -1) {
              link = "http://" + pageUrl.getHost() + "/" + link;
            } else {
              String path =
                file.substring(0, file.lastIndexOf('/') + 1);
              link = "http://" + pageUrl.getHost() + path + link;
            }
          }
        }

        // Remove anchors from link.
        int index = link.indexOf('#');
        if (index != -1) {
          link = link.substring(0, index);
        }

        // Remove leading "www" from URL's host if present.
        link = WebTools.removeWwwFromUrl(link);
        
        link = link.trim();
        
        // add / to the end
        if(link.charAt(link.length()-1) != '/')
        {
            link = link + "/";
        }

        // Verify link and skip if invalid.
        URL verifiedLink = WebTools.verifyUrl(link);
        if (verifiedLink == null) {
          continue;
        }

        // Skip link if it has already been crawled.
        if (crawledList.contains(link)) {
          continue;
        }

        // Add link to list.
        linkList.add(link);
      }

      return (linkList);
    }
    
    /**
     * Determine whether or not search string is
     * matched in the given page contents.
     *
     * @param pageContents
     * @param searchString
     * @param caseSensitive
     * @return true if searchString was present within pageContents
     */
    public static boolean searchStringMatches(String pageContents, 
            String searchString,boolean caseSensitive) {
        String searchContents = pageContents;

       /* If case sensitive search, lowercase
          page contents for comparison. */
       if (!caseSensitive) {
         searchContents = pageContents.toLowerCase();
       }
    
       // Split search string into individual terms.
       Pattern p = Pattern.compile("[\\s]+");
       String[] terms = p.split(searchString);
    
       // Check to see if each term matches.
       for (int i = 0; i < terms.length; i++) {
         if (caseSensitive) {
           if (searchContents.indexOf(terms[i]) == -1) {
             return false;
           }
         } else {
           if (searchContents.indexOf(terms[i].toLowerCase()) == -1) {
             return false;
           }
         }
       }
    
       return true;
     }
}
