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

import java.util.List;
import java.util.concurrent.ExecutionException;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import com.xyproto.archfriend.model.Maintainer;
import com.xyproto.archfriend.model.Package;


public class ArchFriendSplashActivity extends Activity {

  private boolean spinnerCanChangeStuffYet;
  private WebContents wc;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    wc = new WebContents(getApplicationContext());
    spinnerCanChangeStuffYet = false;
    setContentView(R.layout.splash);
  }

  @Override
  public void onStart() {
    super.onStart();
    // Wait 0.7 sec then close splash screen (replace view context)
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
        }

        TextView tv = (TextView)findViewById(R.id.txtArchNews);
        tv.setMovementMethod(new ScrollingMovementMethod());
      }
    }, 700);
  }

  private void populateSpinner() throws InterruptedException, ExecutionException {
    Spinner spinner = (Spinner)findViewById(R.id.lstMaintainers);

    List<Maintainer> maintainers = wc.getMaintainers();

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
            List<Package> packages = wc.getFlaggedPackageText(maintainer);

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
              tvNews.setText(R.string.no_data);

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

  private void populateNews() throws InterruptedException, ExecutionException {
    TextView tvNews = (TextView)findViewById(R.id.txtArchNews);
    String outputText = wc.getNewsText();

    if (outputText != null)
      tvNews.setText(outputText);
    else
      tvNews.setText(R.string.no_data);

    scrollHome();
  }

  public void btnNews_clicked(View view) throws InterruptedException, ExecutionException {
    populateNews();
  }

  private void scrollHome() {
    TextView tv = (TextView)findViewById(R.id.txtArchNews);
    tv.scrollTo(0, 0);
  }

}