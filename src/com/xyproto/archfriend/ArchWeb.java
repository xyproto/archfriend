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
import com.xyproto.archfriend.model.News;
import com.xyproto.archfriend.model.Package;


/**
 * A class for fetching various Arch Linux related content from the web
 */
public class ArchWeb {

  private static final String ArchURL = "https://www.archlinux.org";
  private static final String NewsURL = ArchURL + "/news/";
  private static final String MaintainerURLp1 = ArchURL + "/packages/?sort=&arch=any&arch=x86_64&q=&maintainer=";
  private static final String MaintainerURLp2 = "&last_update=&flagged=Flagged&limit=all";
  private static final String MaintainerListURL = ArchURL + "/packages/?limit=1";

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

  public static List<News> getNews(int limit) throws InterruptedException, ExecutionException {
    List<News> news = new ArrayList<News>();

    String source = Web.get(NewsURL);
    if (source.length() != 0) {
      Document doc = Jsoup.parse(source);

      Elements urls = doc.getElementsByClass("wrap");
      for (int i = 0; i < urls.size() && i < limit; i++) {
        String url = urls.get(i).getElementsByTag("a").get(0).attr("href");
        news.add(getNews(ArchURL + url));
      }
    }

    return news;
  }

  /**
   * Fetch the news item and convert it to some sort of plain text
   * 
   * @param url
   *          the news to fetch
   * @return The latest news item as a string
   * @throws InterruptedException
   * @throws ExecutionException
   */
  public static News getNews(String url) throws InterruptedException, ExecutionException {
    News news = null;

    String source = Web.get(url);
    if (source.length() != 0) {
      Document doc = Jsoup.parse(source);

      // Get the relevant block of html text
      Element content = doc.getElementsByClass("article-content").get(0);
      String text = content.text();

      String title = doc.getElementsByTag("h2").get(0).text();

      String[] info = doc.getElementsByClass("article-info").get(0).text().split(" - ");
      String date = info[0];
      String author = info[1];

      news = new News();
      news.setText(text);
      news.setTitle(title);
      news.setAuthor(author);
      news.setDate(date);
      news.setUrl(url);

      // TODO: Collect URLs and have choice in the menu named 'Launch URLs' that
      // opens up a list of all URLs mentioned in the new text, that can be
      // opened.
    }

    return news;
  }
}
