
package com.xyproto.archfriend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;


public class HTTPS {

    private HttpGet mRequest;
    private HttpClient mClient;
    private BufferedReader mReader;

    private StringBuffer mBuffer;
    private String mNewLine;

    // Thanks
    // http://argillander.wordpress.com/2011/11/23/get-web-page-source-code-in-android/
    public String wget(String url) throws ClientProtocolException, IOException, URISyntaxException {

        int bufsize = 2048;

        mRequest = new HttpGet();
        mClient = getNewHttpClient();
        mReader = null;

        mBuffer = new StringBuffer(bufsize);
        mNewLine = System.getProperty("line.separator");

        mBuffer.setLength(0);

        try {
            mRequest.setURI(new URI(url));
            HttpResponse response = mClient.execute(mRequest);

            mReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"), bufsize);

            String line = "";
            while ((line = mReader.readLine()) != null) {
                mBuffer.append(line);
                mBuffer.append(mNewLine);
            }
        } finally {
            closeReader();
        }

        return mBuffer.toString();
    }

    private void closeReader() {
        if (mReader == null)
            return;

        try {
            mReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HttpClient getNewHttpClient() {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

            return new DefaultHttpClient(ccm, params);
        } catch (Exception e) {
            return new DefaultHttpClient();
        }
    }

}
