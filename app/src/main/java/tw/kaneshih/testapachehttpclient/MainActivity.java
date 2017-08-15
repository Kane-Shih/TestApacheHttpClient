package tw.kaneshih.testapachehttpclient;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    // SSL Server Test: https://www.ssllabs.com/ssltest/analyze.html
    private static final String URL = "https://www.sandbox.paypal.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void[] params) {
                BufferedReader reader = null;
                try {
                    // Purpose: To enable TLSv1.1 and TLSv1.2
                    // Prerequisite: This example uses legacy apache http library
                    //               in build.gradle => useLibrary 'org.apache.http.legacy'
                    // Explanation: SSLSocketFactory.getSocketFactory()
                    //                  -> SSLSocketFactory.NoPreloadHolder.DEFAULT_FACTORY
                    //                    -> uses HttpsURLConnection.getDefaultSSLSocketFactory()
                    TLSSocketFactory tlsSocketFactory = new TLSSocketFactory();
                    HttpsURLConnection.setDefaultSSLSocketFactory(tlsSocketFactory);
                    SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();

                    HttpClient client = new DefaultHttpClient();
                    client.getConnectionManager().getSchemeRegistry().register(new Scheme("https", socketFactory, 443));

                    // normal HTTP result process
                    HttpGet httpGet = new HttpGet(URL);
                    HttpResponse response = client.execute(httpGet);
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        reader = new BufferedReader(new InputStreamReader(entity.getContent()));
                        StringBuilder sb = new StringBuilder();
                        sb.append("HTTP STATUS:" + response.getStatusLine().getStatusCode()).append("\n\n");
                        String line = null;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line).append("\n");
                        }
                        return sb.toString();
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                ((TextView) findViewById(R.id.text)).setText("SDK INT: " + Build.VERSION.SDK_INT + "\nRESULT: " + s);
            }
        }.execute((Void[]) null);
    }
}
