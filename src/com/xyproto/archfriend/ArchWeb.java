/*

  MIT license:

  Copyright (c) 2012 Alexander Rødseth
  
  Permission is hereby granted, free of charge, to any person obtaining
  a copy of this software and associated documentation files (the
  "Software"), to deal in the Software without restriction, including
  without limitation the rights to use, copy, modify, merge, publish,
  distribute, sublicense, and/or sell copies of the Software, and to
  permit persons to whom the Software is furnished to do so, subject to
  the following conditions:
  
  The above copyright notice and this permission notice shall be
  included in all copies or substantial portions of the Software.
  
  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
  LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
  OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
  WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 */

package com.xyproto.archfriend;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.xyproto.archfriend.model.Maintainer;
import com.xyproto.archfriend.model.Package;


/**
 * A class for fetching various Arch Linux related content from the web
 */
public class ArchWeb {

  private static String NewsURL = "https://www.archlinux.org/feeds/news/";
  private static String MaintainerURLp1 = "https://www.archlinux.org/packages/?sort=&arch=any&arch=x86_64&q=&maintainer=";
  private static String MaintainerURLp2 = "&last_update=&flagged=Flagged&limit=all";
  private static String MaintainerListURL = "https://www.archlinux.org/packages/?limit=1";

  /**
   * Return the list of the packages flagged as out of date
   * 
   * @param maintainer
   *          The maintainer in question
   * @return A list of packages
   * @throws InterruptedException
   * @throws ExecutionException
   */
  public static List<Package> getFlaggedPackages(Maintainer maintainer) throws InterruptedException, ExecutionException {
    String source = Web.get(MaintainerURLp1 + maintainer.getUsername() + MaintainerURLp2);
    List<Package> packages = new ArrayList<Package>();

    if (source.length() != 0) {
      Document doc = Jsoup.parse(source);

      Elements trs = doc.getElementsByTag("tr");

      for (Element tr : trs) {
        Elements flagged = tr.getElementsByClass("flagged");
        if (!flagged.isEmpty()) {
          packages.add(new Package(tr.getElementsByTag("a").text(), flagged.get(0).text()));
        }
      }
    }

    return packages;
  }

  /**
   * Get the list of maintainers
   * 
   * @return A list of maintainers
   * @throws InterruptedException
   * @throws ExecutionException
   */
  public static List<Maintainer> getMaintainers() throws InterruptedException, ExecutionException {
    List<Maintainer> maintainers = new ArrayList<Maintainer>();
    String source = Web.get(MaintainerListURL);

    if (source.length() != 0) {
      Document doc = Jsoup.parse(source);

      Elements options = doc.getElementById("id_maintainer").getElementsByTag("option");

      // Pick out the usernames of the maintainers from the block of html
      String username;
      for (int i = 0; i < options.size(); i++) {
        username = options.get(i).val();
        if (!username.equals("orphan")) {
          maintainers.add(new Maintainer(username, options.get(i).text()));
        }
      }
    }

    return maintainers;
  }

  /**
   * Fetch the latest news item and convert it to some sort of plain text
   * 
   * @return The latest news item as a string
   * @throws InterruptedException
   * @throws ExecutionException
   */
  public static String getNewsText() throws InterruptedException, ExecutionException {
    String newsText = "";

    String source = Web.get(NewsURL);
    if (source.length() != 0) {
      // Get the relevant block of html text
      newsText = source.split("description")[3].split("description")[0];
      // Remove the first and the two last characters
      newsText = newsText.substring(1, newsText.length() - 2);
      // Strip away newlines in the html first
      newsText = newsText.replaceAll("\n", "");
      // Angle brackets
      newsText = newsText.replaceAll("&lt;", "<");
      newsText = newsText.replaceAll("&gt;", ">");
      // Paragraphs
      newsText = newsText.replaceAll("<p>", "");
      newsText = newsText.replaceAll("</p>", "\n");
      // Bold
      newsText = newsText.replaceAll("<b>", "*** ");
      newsText = newsText.replaceAll("</b>", " ***");
      // Code
      newsText = newsText.replaceAll("<code>", "\"");
      newsText = newsText.replaceAll("</code>", "\"");
      // Links
      newsText = newsText.replaceAll("<a href=\"", "\n[ ");
      newsText = newsText.replaceAll("\">", " ]\n");
      newsText = newsText.replaceAll("</a>", "");
      // Lists
      newsText = newsText.replaceAll("<li>", "* \n");
      newsText = newsText.replaceAll("</li>", "");
      newsText = newsText.replaceAll("<ol>", "");
      newsText = newsText.replaceAll("</ol>", "");
      newsText = newsText.replaceAll("<ul>", "");
      newsText = newsText.replaceAll("</ul>", "");
      // Italic
      newsText = newsText.replaceAll("<i>", "_");
      newsText = newsText.replaceAll("</i>", "_");
    }

    return newsText;
  }

}
