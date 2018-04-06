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

public class LinnanmaaWeatherActivity extends Activity {
	private static final String WEATHER_URI = "http://weather.willab.fi/weather.xml";

	private TextView mTemperatureLabel;
	private Button mRefreshButton;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mTemperatureLabel = (TextView) findViewById(R.id.temperature_label);
        mRefreshButton = (Button) findViewById(R.id.refresh_button);
        new RefreshAsyncTask().execute();
    }
    public void onRefreshButtonClick(View view) {
        new RefreshAsyncTask().execute();    	
    }
    
    private String getStringRegion(String string, String before, String after) {
    	try {
	    	int start = string.indexOf(before);
	    	if (start == -1)
	    		return null;
	    	start += before.length();
	    	int end = string.indexOf(after, start);
	    	end -= start;
	    	if (end == -1)
	    		return null;
	    	return string.substring(start, end);
    	} catch (IndexOutOfBoundsException exception) {
    		return null;
    	}
    }
        
    private class RefreshAsyncTask extends AsyncTask<Void, Void, String> {
    	@Override
		protected void onPreExecute() {
    		mTemperatureLabel.setText(R.string.temperature_label);
    		mRefreshButton.setEnabled(false);
    	}
    	@Override
		protected String doInBackground(Void... params) {

			URL url = null;
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
						byte [] buffer = new byte[length];
						in.read(buffer, 0, length);
						String content = new String(buffer, 0, length, StandardCharsets.UTF_8);
						String temperature = getStringRegion(content, "<tempnow unit=\"C\">", "</tempnow>");
						if (temperature != null) {
							result = temperature + " °C";
						} else {
							result = "Parse error.";
						}
					}
				} else {
					result = "Server error: " + response;
				}

			} catch (MalformedURLException e) {
				e.printStackTrace();
				result = "URL error";
			} catch (IOException e) {
				e.printStackTrace();
				result = "Network error";
			} finally {
				urlConnection.disconnect();
			}
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			mTemperatureLabel.setText(result);
    		mRefreshButton.setEnabled(true);
		}
    	
    }
}