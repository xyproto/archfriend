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

import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class ArchFriendSplashActivity extends Activity {

	private boolean spinnerCanChangeStuffYet;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.splash);
	}

	@Override
	public void onStart() {
		spinnerCanChangeStuffYet = false;
		super.onStart();
		// Wait 0.5 sec then close splash screen (replace view context)
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

				TextView tv = (TextView) findViewById(R.id.txtArchNews);
				tv.setMovementMethod(new ScrollingMovementMethod());
			}
		}, 500);
	}

	private String getNewsText() throws InterruptedException,
			ExecutionException {
		String source = new HTTPTask().execute(
				"http://www.archlinux.org/feeds/news/").get();

		if (source != null) {
			// Get the relevant block of html text
			String item = source.split("description")[3].split("description")[0];
			// Remove the first and two last characters
			item = item.substring(1, item.length() - 2);
			// Change &lt; into <
			item = item.replaceAll("&lt;", "<");
			// Change &gt; into >
			item = item.replaceAll("&gt;", ">");
			// Change <p> into nothing
			item = item.replaceAll("<p>", "");
			// Change </p> into a newline
			item = item.replaceAll("</p>", "\n");
			// Change <b> into ***
			item = item.replaceAll("<b>", "*** ");
			// Change </b> into ***
			item = item.replaceAll("</b>", " ***");
			// Change <code> into "
			item = item.replaceAll("<code>", "\"");
			// Change </code> into "
			item = item.replaceAll("</code>", "\"");
			// Change <a href=" into \n[
			item = item.replaceAll("<a href=\"", "\n[ ");
			// Change "> into ]\n
			item = item.replaceAll("\">", " ]\n");
			// Change </a> into nothing
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

	/*
	 * Return a colon separated list of maintainer usernames as a string
	 */
	private String getMaintainersColonSep() throws InterruptedException,
			ExecutionException {
		String source = new HTTPTask().execute(
				"https://www.archlinux.org/packages/?limit=1").get();

		if (source != null) {
			// Pattern pattern = Pattern.compile("value=\"([a-z]+)");
			// Matcher matcher = pattern.matcher(source);

			String[] names = source.split("Maintainer")[1].split("/select")[0]
					.split("\"");

			// Pick out the usernames of the maintainers from the block of html
			String joined = "";
			for (int i = 10; i < names.length; i++) {
				// the real names are in the discarded strings
				if (names[i].indexOf('>') == -1) {
					joined += names[i] + ";";
				}
			}
			// Remove the final ";"
			joined = joined.substring(0, joined.length() - 1);

			return joined;
		}

		return "Received no data from web";
	}

	private String getFlaggedPackageText(String username)
			throws InterruptedException, ExecutionException {
		String source = new HTTPTask().execute(
				"http://www.archlinux.org/packages/?sort=&arch=any&arch=x86_64&q=&maintainer="
						+ username + "&last_update=&flagged=Flagged&limit=all")
				.get();

		if (source != null) {
			// Number of <td>'s
			String searchText = "class=\"flagged\"";
			int numFlagged = source.split(searchText).length - 1;

			String outputText = username + " has ";
			String isare = "are";
			String packages = "packages";
			if (numFlagged == 0) {
				outputText += "zero";
			} else if (numFlagged == 1) {
				outputText += "only one";
				isare = "is";
				packages = "package";
			} else {
				outputText += Integer.valueOf(numFlagged).toString();
			}
			outputText += " " + packages + " that " + isare
					+ " flagged out of date.";
			return outputText;
		}
		return "Received no data from web";
	}

	private void populateSpinner() throws InterruptedException,
			ExecutionException {
		Spinner spinner = (Spinner) findViewById(R.id.lstMaintainers);
		String[] maintainers = getMaintainersColonSep().split(";");
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, maintainers);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);

		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@SuppressWarnings("rawtypes")
			@Override
			public void onItemSelected(AdapterView adapter, View v, int i,
					long lng) {
				TextView tv = (TextView) findViewById(R.id.txtArchNews);
				String maintainer = (String) adapter.getAdapter().getItem(i);

				String outputText = null;
				try {
					outputText = getFlaggedPackageText(maintainer);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (spinnerCanChangeStuffYet && outputText != null) {
					tv.setText(outputText);
					scrollHome();
				} else {
					// This is to avoid the spinner from overwriting the news
					// with the initial default selection
					spinnerCanChangeStuffYet = true;
				}
			}

			@SuppressWarnings("rawtypes")
			@Override
			public void onNothingSelected(AdapterView arg0) {
				TextView tv = (TextView) findViewById(R.id.txtArchNews);
				String outputText = "";
				if (spinnerCanChangeStuffYet) {
					tv.setText(outputText);
					scrollHome();
				}
			}
		});
	}

	private void populateNews() throws InterruptedException, ExecutionException {
		TextView tv = (TextView) findViewById(R.id.txtArchNews);
		String outputText = getNewsText();
		tv.setText(outputText);
		scrollHome();
	}

	public void btnNews_clicked(View view) throws InterruptedException,
			ExecutionException {
		populateNews();
	}

	private void scrollHome() {
		TextView tv = (TextView) findViewById(R.id.txtArchNews);
		tv.scrollTo(0, 0);
	}

}