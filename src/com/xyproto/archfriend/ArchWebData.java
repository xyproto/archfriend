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

import com.xyproto.archfriend.model.Maintainer;
import com.xyproto.archfriend.model.News;
import com.xyproto.archfriend.model.Package;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;


/**
 * A class for fetching various Arch Linux related content from the web
 */
public class ArchWebData {

    private static final String ArchURL = "https://www.archlinux.org";
    private static final String NewsURL = ArchURL + "/news/";
    private static final String MaintainerURLp1 = ArchURL + "/packages/?sort=&arch=any&arch=x86_64&q=&maintainer=";
    private static final String MaintainerURLp2 = "&last_update=&flagged=Flagged&limit=all";
    private static final String MaintainerListURL = ArchURL + "/packages/?limit=1";

    /**
     * Return the list of the packages flagged as out of date
     *
     * @param maintainer The maintainer in question
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
            Elements flagged;

            for (Element tr : trs) {
                flagged = tr.getElementsByClass("flagged");
                if (!flagged.isEmpty()) {
                    packages.add(new Package(tr.getElementsByTag("a").text(), flagged.get(0).text()));
                }
            }
        }

        return packages;
    }

    /**
     * Get the list of the maintainers
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
            for (Element option : options) {
                username = option.val();
                if (!username.equals("orphan")) {
                    maintainers.add(new Maintainer(username, option.text()));
                }
            }
        }

        return maintainers;
    }

    /**
     * Fetch the link to the latest N news item
     *
     * @param n
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public static List<String> getNewsURLs(int n) throws InterruptedException, ExecutionException {
        List<String> news = new ArrayList<String>();

        String source = Web.get(NewsURL);
        if (source.length() != 0) {
            Document doc = Jsoup.parse(source);

            Elements urls = doc.getElementsByClass("wrap");
            for (int i = 0; i < urls.size() && i < n; i++) {
                String url = urls.get(i).getElementsByTag("a").get(0).attr("href");
                news.add(ArchURL + url);
            }
        }

        return news;
    }

    private static String oldSchoolTextFormatting(Element element) {
        final String[] replacements = new String[]{
                "\n-> ", "&lt;-><", "&gt;->>", "<p>->", "</p>->\n\n", "<b>->*** ", "</b>-> ***", "<code>->\"", "</code>->\"", "</a>->",
                "<li>->* ", "</li>->\n", "<ol>->", "</ol>->\n", "<ul>->", "</ul>->\n", "<i>->_", "</i>->_"};
        final String aStart = "<a href=", aEnd = "\">";
        String text, part1, part2;
        int pos1, pos2;

        text = element.html();
        if (text.length() > 0) {
            // Remove links (but keep the text)
            while (text.contains(aStart)) {
                pos1 = text.indexOf(aStart);
                pos2 = text.indexOf(aEnd, pos1 + 1);
                part1 = text.substring(0, pos1 - 1);
                part2 = text.substring(pos2 + aEnd.length(), text.length());
                text = part1 + part2;
            }
            // Perform various replacements
            for (String replacement : replacements) {
                String[] fields = replacement.split("->", 1);
                text = text.replaceAll(fields[0], fields[1]);
            }
            // Remove double spaces
            while (text.contains("  ")) {
                text = text.replaceAll("  ", " ");
            }
            // Remove leading spaces
            text = text.replaceAll("\n ", "\n");
            // Final trim
            text = text.trim();
        }
        return text;
    }

    /**
     * Fetch the news item
     *
     * @param url the news to fetch
     * @return The latest news item as a string
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws ParseException
     */
    public static News getNews(String url) throws InterruptedException, ExecutionException, ParseException {
        News news = null;

        String source = Web.get(url);
        if (source.length() != 0) {
            Document doc = Jsoup.parse(source);

            // Get the relevant block of html text
            Element content = doc.getElementsByClass("article-content").get(0);
            String text = oldSchoolTextFormatting(content);

            String title = doc.getElementsByTag("h2").get(0).text();

            String[] info = doc.getElementsByClass("article-info").get(0).text().split(" - ");
            String date = info[0];
            String author = info[1];

            news = new News();
            news.setText(text);
            news.setTitle(title);
            news.setAuthor(author);
            SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            long timestamp = yyyyMMdd.parse(date).getTime();
            news.setDate(timestamp);
            news.setUrl(url);
        }

        return news;
    }
}
