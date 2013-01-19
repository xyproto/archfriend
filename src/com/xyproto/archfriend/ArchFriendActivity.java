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

import java.text.ParseException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import com.xyproto.archfriend.db.NewsDataSource;
import com.xyproto.archfriend.model.Maintainer;
import com.xyproto.archfriend.model.News;
import com.xyproto.archfriend.model.Package;


public class ArchFriendActivity extends Activity {

  private boolean spinnerCanChangeStuffYet;
  private NewsDataSource datasource;
  private Notification noti;
  private static int notificationId = 256;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    spinnerCanChangeStuffYet = false;
    setContentView(R.layout.splash);

    datasource = new NewsDataSource(this);
    datasource.open();
  }

  @Override
  public void onStart() {
    super.onStart();
    // Wait 0.9 sec then close splash screen (replace view context)
    // TODO: Start loading the list of names
    Handler handler = new Handler();
    handler.postDelayed(new Runnable() {
      public void run() {
        setContentView(R.layout.archoverview);

        try {
          populateSpinner();
          populateNews();
        } catch (InterruptedException e) {
          e.printStackTrace();
        } catch (ExecutionException e) {
          e.printStackTrace();
        } catch (ParseException e) {
          e.printStackTrace();
        }

        TextView tv = (TextView)findViewById(R.id.txtArchNews);
        tv.setMovementMethod(new ScrollingMovementMethod());
      }
    }, 900);
    // TODO: Find the best way to load the data for the spinner in the
    // background
    // Handler handler2 = new Handler();
    // handler2.postDelayed(new Runnable() {
    // public void run() {
    // try {
    // populateSpinner();
    // } catch (InterruptedException e) {
    // e.printStackTrace();
    // } catch (ExecutionException e) {
    // e.printStackTrace();
    // }
    // }
    // }, 900);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    datasource.close();
  }

  private void populateSpinner() throws InterruptedException, ExecutionException {
    Spinner spinner = (Spinner)findViewById(R.id.lstMaintainers);

    List<Maintainer> maintainers = ArchWeb.getMaintainers();

    if (!maintainers.isEmpty()) {
      ArrayAdapter<Maintainer> adapter = new ArrayAdapter<Maintainer>(this, android.R.layout.simple_spinner_item, maintainers);
      adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      spinner.setAdapter(adapter);

      spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> adapter, View v, int i, long lng) {
          TextView tvNews = (TextView)findViewById(R.id.txtArchNews);
          Maintainer maintainer = (Maintainer)adapter.getAdapter().getItem(i);

          String outputText = null;
          try {
            List<Package> packages = ArchWeb.getFlaggedPackages(maintainer);

            outputText = maintainer.getFullName();
            if (packages.isEmpty()) {
              outputText += " " + String.format(getString(R.string.flagged_ood), getString(R.string.zero));
            } else if (packages.size() == 1) {
              outputText += " " + getString(R.string.only_one);
              outputText += "\n\n" + packages.get(0);
            } else {
              outputText += " " + String.format(getString(R.string.flagged_ood), Integer.valueOf(packages.size()));
              outputText += "\n\n";
              for (Package pkg : packages) {
                outputText += pkg + "\n";
              }
            }
          } catch (InterruptedException e) {
            e.printStackTrace();
          } catch (ExecutionException e) {
            e.printStackTrace();
          }

          if (spinnerCanChangeStuffYet) {
            if (outputText != null)
              tvNews.setText(outputText);
            else
              tvNews.setText(R.string.no_maintainers);

            scrollHome();
          } else {
            // This is to avoid the spinner from overwriting the
            // news with the initial default selection
            spinnerCanChangeStuffYet = true;
          }
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
          TextView tv = (TextView)findViewById(R.id.txtArchNews);
          if (spinnerCanChangeStuffYet) {
            tv.setText("");
            scrollHome();
          }
        }

      });
    } else {
      spinner.setVisibility(View.INVISIBLE);
      TextView tvNoData = (TextView)findViewById(R.id.tvNoData);
      tvNoData.setVisibility(View.VISIBLE);
    }
  }

  private void populateNews() throws InterruptedException, ExecutionException, ParseException {
    TextView tvNews = (TextView)findViewById(R.id.txtArchNews);

    // Get the latest news
    List<String> urls = ArchWeb.getNewsURLs(1);

    if (urls.size() != 0) {
      String latestNewsUrl = urls.get(0);
      News news;

      News latestNewsDB = datasource.getLatestNews();
      if (latestNewsDB != null && latestNewsDB.getUrl().equals(latestNewsUrl)) {
        news = latestNewsDB;
      } else {
        news = ArchWeb.getNews(latestNewsUrl);
        datasource.createNews(news);
      }

      String outputText = news.formatArticle(getString(R.string.latest_news));
      tvNews.setText(outputText);
    } else
      tvNews.setText(R.string.no_data);

    scrollHome();
  }

  public void btnNews_clicked(View view) throws InterruptedException, ExecutionException, ParseException {
    NotificationCompat.Builder nb = new NotificationCompat.Builder(this);
    nb.setContentTitle("Waaaaoooaaaaa!");
    nb.setContentText("OJOJOJOJOJOJ!");
    nb.setSmallIcon(R.drawable.archfriend_tiny_bw_logo2013);
    nb.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
    // nb.setLargeIcon(bitmap);
    noti = nb.build();

    NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
    notificationManager.notify(notificationId, noti);
    populateNews();
  }

  private void scrollHome() {
    TextView tv = (TextView)findViewById(R.id.txtArchNews);
    tv.scrollTo(0, 0);
  }

}