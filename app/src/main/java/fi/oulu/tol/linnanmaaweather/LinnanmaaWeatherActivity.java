package fi.oulu.tol.linnanmaaweather;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class LinnanmaaWeatherActivity extends Activity {
   private static final String WEATHER_URI = "http://weather.willab.fi/weather.json";

   private Button mRefreshButton;
   private double temperature;
   private int humidity;
   private double airPressure;
   private String timeStamp;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main);
      mRefreshButton = findViewById(R.id.refresh_button);
      new RefreshAsyncTask().execute();
   }

   public void onRefreshButtonClick(View view) {
      new RefreshAsyncTask().execute();
   }

   private class RefreshAsyncTask extends AsyncTask<Void, Void, String> {
      @Override
      protected void onPreExecute() {
         TextView timeStampView = findViewById(R.id.timeStampView);
         timeStampView.setText(R.string.loading_placeholder);
         mRefreshButton.setEnabled(false);
      }

      @Override
      protected String doInBackground(Void... params) {

         URL url;
         HttpURLConnection urlConnection = null;
         String result = "";
         try {
            url = new URL(WEATHER_URI);
            urlConnection = (HttpURLConnection) url.openConnection();
            int response = urlConnection.getResponseCode();
            if (response == HttpURLConnection.HTTP_OK) {
               InputStream in = new BufferedInputStream(urlConnection.getInputStream());
               int length = urlConnection.getContentLength();
               if (length > 0) {
                  byte[] buffer = new byte[length];
                  in.read(buffer, 0, length);
                  String content = new String(buffer, 0, length, StandardCharsets.UTF_8);
                  JSONObject weatherObject = new JSONObject(content);
                  temperature = weatherObject.getDouble("tempnow");
                  humidity = weatherObject.getInt("humidity");
                  airPressure = weatherObject.getDouble("airpressure");
                  timeStamp = weatherObject.getString("timestamp");
               }
            } else {
               result = "Server error: " + response;
            }
         } catch (MalformedURLException e) {
            e.printStackTrace();
            result = "URL error: " + e.getLocalizedMessage();
         } catch (IOException e) {
            e.printStackTrace();
            result = "Network error: " + e.getLocalizedMessage();
         } catch (JSONException e) {
            e.printStackTrace();
            result = "Server send invalid data: " + e.getLocalizedMessage();
         } finally {
            // If URL is malformed, urlConnection is null so must check this.
            // Currently URL is hardcoded but in the future, if user is able to change
            // the URL, it could be malformed so it is a good idea to check this.
            if (null != urlConnection) {
               urlConnection.disconnect();
            }
         }
         return result;
      }

      @Override
      protected void onPostExecute(String result) {
         // If result has content, an error happened.
         TextView timeStampView = findViewById(R.id.timeStampView);
         if (result.length() > 0) {
            timeStampView.setText(result);
            return;
         } else {
            timeStampView.setText(timeStamp);
            TextView temperatureView = findViewById(R.id.temperatureValue);
            temperatureView.setText(Double.toString(temperature));
            TextView humidityView = findViewById(R.id.humidityValue);
            humidityView.setText(Integer.toString(humidity));
            TextView airPressureView = findViewById(R.id.airPressureValue);
            airPressureView.setText(Double.toString(airPressure));
         }
         mRefreshButton.setEnabled(true);
      }

   }
}
