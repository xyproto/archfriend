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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
				populateSpinner();
				populateNews();
				TextView tv = (TextView)findViewById(R.id.txtArchNews);
				tv.setMovementMethod(new ScrollingMovementMethod());
			}
		}, 500);
	}

	private static String convertStreamToString(InputStream is) throws UnsupportedEncodingException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		StringBuilder sb = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			// Log.v("ERROR", "Could not convert inputstream to string");
			// e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				// Log.v("ERROR",
				// "Could not close input stream when converting to string");
				// e.printStackTrace();
			}
		}
		return sb.toString();
	}

	/**
	 * Wraps up two strings as JSON then sends them to an URL
	 * 
	 * Thanks to primalpop at stackoverflow:
	 * http://stackoverflow.com/questions/3027066/how-to-send-a-json
	 * -object-over-request-with-android
	 * 
	 * Currently, this function can only send e-mail and password, it's not a
	 * general function
	 */
	protected void sendJson(final String url, final String email, final String pwd) {
		Thread t = new Thread() {
			public void run() {
				Looper.prepare(); // For Preparing Message Pool for the child Thread
				HttpClient client = new DefaultHttpClient();
				HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000); // Timeout
																																							// Limit
				JSONObject json = new JSONObject();
				try {
					HttpPost post = new HttpPost(url);
					json.put("email", email);
					json.put("password", pwd);
					StringEntity se = new StringEntity(json.toString());
					se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
					post.setEntity(se);
					// HttpResponse response = client.execute(post);
					client.execute(post);
				} catch (Exception e) {
					// Log.v("ERROR", "Could not send JSON");
					// e.printStackTrace();
				}
				Looper.loop(); // Loop in the message queue
			}
		};
		t.start();
	}

	public void btnJSON_clicked(View view) {
		TextView tv = (TextView)findViewById(R.id.txtArchNews);
		String outputText = "Sending an anonymous heart symbol to the author, over JSON...";
		tv.setText(outputText);
		sendJson("http://rambutan.zapto.org:80/api", "archuser@somewhere.com", "<3");
		outputText += "done.\n\nThank you. XD";
		tv.setText(outputText);
	}

	private String getNewsText() {
		URLConnection urlConnection = null;
		String source = null;
		URL feedUrl = null;
		try {
			feedUrl = new URL("http://www.archlinux.org/feeds/news/");
		} catch (MalformedURLException e) {
			// Log.v("ERROR", "Malformed URL exception");
			// e.printStackTrace();
		}
		try {
			if (feedUrl != null) {
				urlConnection = feedUrl.openConnection();
			}
		} catch (IOException e) {
			// Log.v("ERROR", "Could not get news from web");
			// e.printStackTrace();
		}
		try {
			if (urlConnection != null) {
				source = convertStreamToString(urlConnection.getInputStream());
			}
		} catch (IOException e) {
			// Log.v("ERROR", "Could not convert stream to string");
			// e.printStackTrace();
		}
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

			String outputText = "Latest news item:\n" + item;
			return outputText;
		}
		return "No data from web";
	}

	/*
	 * Return a colon separated list of maintainer usernames as a string
	 */
	private String getMaintainersColonSep() {
		URLConnection urlConnection = null;
		String source = null;
		URL feedUrl = null;
		try {
			feedUrl = new URL("http://www.archlinux.org/packages/?limit=1");
		} catch (MalformedURLException e) {
			// Log.v("ERROR", "Malformed URL exception");
		}
		try {
			if (feedUrl != null) {
				urlConnection = feedUrl.openConnection();
			}
		} catch (IOException e) {
			// Log.v("ERROR", "Could not get maintainer info from web");
			// e.printStackTrace();
		}
		try {
			if (urlConnection != null) {
				source = convertStreamToString(urlConnection.getInputStream());
			}
		} catch (IOException e) {
			// Log.v("ERROR", "Could not convert stream to string");
			// e.printStackTrace();
		}
		if (source != null) {
			String[] names = source.split("Maintainer")[1].split("/select")[0].split("\"");

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
		return "No data from web";
	}

	private String getFlaggedPackageText(String username) {
		URLConnection urlConnection = null;
		String source = null;
		URL feedUrl = null;
		try {
			feedUrl = new URL("http://www.archlinux.org/packages/?sort=&arch=any&arch=x86_64&q=&maintainer=" + username
					+ "&last_update=&flagged=Flagged&limit=all");
		} catch (MalformedURLException e) {
			// Log.v("ERROR", "MALFORMED URL EXCEPTION");
		}
		try {
			if (feedUrl != null) {
				urlConnection = feedUrl.openConnection();
			}
		} catch (IOException e) {
			// Log.v("ERROR", "Could not get package info from web");
			// e.printStackTrace();
		}
		try {
			if (urlConnection != null) {
				source = convertStreamToString(urlConnection.getInputStream());
			}
		} catch (IOException e) {
			// Log.v("ERROR", "Could not convert stream to string");
			// e.printStackTrace();
		}
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
				outputText += new Integer(numFlagged).toString();
			}
			outputText += " " + packages + " that " + isare + " flagged out of date.";
			return outputText;
		}
		return "No data from web";
	}

	private void populateSpinner() {
		Spinner spinner = (Spinner)findViewById(R.id.lstMaintainers);
		String[] maintainers = getMaintainersColonSep().split(";");
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, maintainers);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);

		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@SuppressWarnings("rawtypes")
			@Override
			public void onItemSelected(AdapterView adapter, View v, int i, long lng) {
				TextView tv = (TextView)findViewById(R.id.txtArchNews);
				String maintainer = (String)adapter.getAdapter().getItem(i);
				String outputText = getFlaggedPackageText(maintainer);
				if (spinnerCanChangeStuffYet) {
					tv.setText(outputText);
					scrollHome();
				} else {
					// This is to avoid the spinner from overwriting the news with the
					// initial default selection
					spinnerCanChangeStuffYet = true;
				}
			}

			@SuppressWarnings("rawtypes")
			@Override
			public void onNothingSelected(AdapterView arg0) {
				TextView tv = (TextView)findViewById(R.id.txtArchNews);
				String outputText = "";
				if (spinnerCanChangeStuffYet) {
					tv.setText(outputText);
					scrollHome();
				}
			}
		});
	}

	public void btnTest_clicked(View view) {
		TextView tv = (TextView)findViewById(R.id.txtArchNews);
		tv.setText("ok");
	}

	private void populateNews() {
		TextView tv = (TextView)findViewById(R.id.txtArchNews);
		String outputText = getNewsText();
		tv.setText(outputText);
		scrollHome();
	}

	public void btnNews_clicked(View view) {
		populateNews();
	}

	private void scrollHome() {
		TextView tv = (TextView)findViewById(R.id.txtArchNews);
		tv.scrollTo(0, 0);
	}

}