package com.steve4448.jbitminingstats;

import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
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
        HttpClient client = new DefaultHttpClient();
        StringBuffer sb = new StringBuffer();
        String content = "";
        Toast.makeText(getBaseContext(), "Connecting to JSON supplier...", Toast.LENGTH_SHORT).show();
        try {
        	HttpEntity ent = client.execute(new HttpGet("https://mining.bitcoin.cz/stats/json/149845-8e60ac1e3ae4489add732c1d2b377965")).getEntity();
        	InputStreamReader reader = new InputStreamReader(ent.getContent());
        	char[] data = new char[1024];
        	while(reader.read(data) != -1)
        		sb.append(data);
        	Log.i("MiningStatisticsActivity", "Content length: " + ent.getContentLength() + ", fetched length: " + sb.length() + ".");
        	Toast.makeText(getBaseContext(), "Obtained data!", Toast.LENGTH_SHORT).show();
        } catch(Exception e) {
			e.printStackTrace();
			Toast.makeText(getBaseContext(), "Error recieving data!", Toast.LENGTH_LONG).show();
		}
        content = sb.toString();
        try {
        	Toast.makeText(getBaseContext(), "Parsing JSON content...", Toast.LENGTH_SHORT).show();
			JSONArray jsonContent = new JSONArray(content);
			Log.i("MiningStatisticsActivity", jsonContent.getJSONObject(0).toString());
			Toast.makeText(getBaseContext(), "Parsed!", Toast.LENGTH_SHORT).show();
		} catch (JSONException e) {
			e.printStackTrace();
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
