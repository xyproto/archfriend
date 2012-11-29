/*

  MIT license:

  Copyright (c) 2011 Alexander Rødseth
  
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
import org.jsoup.select.Elements;
import com.xyproto.archfriend.model.Maintainer;


public class WebContents {

  private static String NewsURL = "https://www.archlinux.org/feeds/news/";
  private static String MaintainerURLp1 = "https://www.archlinux.org/packages/?sort=&arch=any&arch=x86_64&q=&maintainer=";
  private static String MaintainerURLp2 = "&last_update=&flagged=Flagged&limit=all";
  private static String MaintainerListURL = "https://www.archlinux.org/packages/?limit=1";

  public String getFlaggedPackageText(Maintainer maintainer) throws InterruptedException, ExecutionException {
    String source = new HTTPTask().execute(MaintainerURLp1 + maintainer.getUsername() + MaintainerURLp2).get();

    if (source.length() != 0) {
      Document doc = Jsoup.parse(source);

      Elements pkgs = doc.getElementsByClass("flagged");

      String outputText = maintainer.getFullName() + " has ";
      String isare = "are";
      String packages = "packages";
      if (pkgs.isEmpty()) {
        outputText += "zero";
      } else if (pkgs.size() == 1) {
        outputText += "only one";
        isare = "is";
        packages = "package";
      } else {
        outputText += Integer.valueOf(pkgs.size()).toString();
      }
      return outputText + " " + packages + " that " + isare + " flagged out of date.";
    }
    return "Received no data from web";
  }

  /*
   * Return a colon separated list of maintainer usernames as a string
   */
  public List<Maintainer> getMaintainersColonSep() throws InterruptedException, ExecutionException {
    List<Maintainer> maintainers = new ArrayList<Maintainer>();
    String source = new HTTPTask().execute(MaintainerListURL).get();

    if (source.length() != 0) {
      Document doc = Jsoup.parse(source);

      Elements options = doc.getElementById("id_maintainer").getElementsByTag("option");

      // Pick out the usernames of the maintainers from the block of html
      for (int i = 0; i < options.size(); i++) {
        maintainers.add(new Maintainer(options.get(i).val(), options.get(i).text()));
      }
    }

    return maintainers;
  }

  public String getNewsText() throws InterruptedException, ExecutionException {
    String source = new HTTPTask().execute(NewsURL).get();

    if (source.length() != 0) {
      // Get the relevant block of html text
      String item = source.split("description")[3].split("description")[0];
      // Remove the first and the two last characters
      item = item.substring(1, item.length() - 2);
      // Angle brackets
      item = item.replaceAll("&lt;", "<");
      item = item.replaceAll("&gt;", ">");
      // Paragraphs
      item = item.replaceAll("<p>", "");
      item = item.replaceAll("</p>", "\n");
      // Bold
      item = item.replaceAll("<b>", "*** ");
      item = item.replaceAll("</b>", " ***");
      // Code
      item = item.replaceAll("<code>", "\"");
      item = item.replaceAll("</code>", "\"");
      // Links
      item = item.replaceAll("<a href=\"", "\n[ ");
      item = item.replaceAll("\">", " ]\n");
      item = item.replaceAll("</a>", "");
      // Lists
      item = item.replaceAll("<li>", "* \n");
      item = item.replaceAll("</li>", "");
      item = item.replaceAll("<ol>", "");
      item = item.replaceAll("</ol>", "");
      item = item.replaceAll("<ul>", "");
      item = item.replaceAll("</ul>", "");
      // Italic
      item = item.replaceAll("<i>", "_");
      item = item.replaceAll("</i>", "_");

      String outputText = "Latest news:\n\n" + item;
      return outputText;
    }

    return "Received no data from web";
  }

}
