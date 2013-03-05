package com.steve4448.jbitminingstats;

import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;
import org.json.JSONException;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

public class MiningStatisticsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mining_statistics);
        HttpClient client = new DefaultHttpClient(new BasicHttpParams());
        StringBuffer sb = new StringBuffer();
        String content = "";
        Toast.makeText(getBaseContext(), "Connecting to JSON supplier...", Toast.LENGTH_SHORT).show();
        try {
        	HttpEntity ent = client.execute(new HttpPost("http://api.geonames.org/postalCodeSearchJSON?formatted=true&postalcode=9791&maxRows=10&username=chris&style=full")).getEntity();
        	InputStreamReader reader = new InputStreamReader(ent.getContent());
        	int len = 0;
        	int curLen = 0;
        	char[] data = new char[512];
        	while((curLen=reader.read(data)) != -1) {
        		String constructedString = new String(data);
        		sb.append(constructedString);
        		len += curLen;
        		Log.i("MiningStatisticsActivity", "Read: " + curLen + ",\n" + constructedString);
        	}
        	Log.i("MiningStatisticsActivity", "Content length: " + ent.getContentLength() + ", fetched length: " + sb.length() + ". " + len);
        	Toast.makeText(getBaseContext(), "Obtained data!", Toast.LENGTH_SHORT).show();
        } catch(Exception e) {
			e.printStackTrace();
			Toast.makeText(getBaseContext(), "Error recieving data!", Toast.LENGTH_LONG).show();
		}
        content = sb.toString();
        try {
        	Toast.makeText(getBaseContext(), "Parsing JSON content...", Toast.LENGTH_SHORT).show();
			JSONArray jsonContent = new JSONArray(content);
			Toast.makeText(getBaseContext(), "Parsed!", Toast.LENGTH_SHORT).show();
		} catch (JSONException e) {
			e.printStackTrace();
			Toast.makeText(getBaseContext(), "Error parsing JSON content!", Toast.LENGTH_LONG).show();
		}
        Log.i("MiningStatisticsActivity", content);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.mining_statistics, menu);
        return true;
    }
    
}
