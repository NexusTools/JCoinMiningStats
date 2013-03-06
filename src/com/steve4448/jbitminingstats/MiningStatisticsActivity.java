package com.steve4448.jbitminingstats;

import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
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
        	HttpEntity ent = client.execute(new HttpPost("https://mining.bitcoin.cz/accounts/profile/json/149845-8e60ac1e3ae4489add732c1d2b377965")).getEntity();
        	InputStreamReader reader = new InputStreamReader(ent.getContent());
        	char[] data = new char[512];
        	int read = -1;
        	while((read = reader.read(data)) != -1)
        		sb.append(data, 0, read);
        	content = sb.toString();
        	Toast.makeText(getBaseContext(), "Obtained " + content.length() + " bytes of data!", Toast.LENGTH_SHORT).show();
        	try {
            	Toast.makeText(getBaseContext(), "Parsing JSON content...", Toast.LENGTH_SHORT).show();
    			JSONObject jsonContent = new JSONObject(content);
    			JSONArray workerNames = jsonContent.getJSONObject("workers").names();
    			JSONObject workerList = jsonContent.getJSONObject("workers");
    			LinearLayout workerTable = (LinearLayout)findViewById(R.id.worker_table_layout);
    			for(int i = 0; i < workerNames.length(); i++) {
    				TableRow workerRow = new TableRow(this);
    				
    				JSONObject worker = workerList.getJSONObject(workerNames.getString(i));
    				
    				TextView workerName = new TextView(this);
    				workerName.setText(workerNames.getString(i));
    				workerRow.addView(workerName);
    				
    				TextView workerRate = new TextView(this);
    				workerRate.setText(worker.getString("hashrate") + "mh/s");
    				workerRow.addView(workerRate);
    				
    				TextView workerAlive = new TextView(this);
    				workerAlive.setText(worker.getString("alive"));
    				workerRow.addView(workerAlive);
    				
    				workerTable.addView(workerRow);
    			}
    			
    			Toast.makeText(getBaseContext(), "Parsed!", Toast.LENGTH_SHORT).show();
    		} catch (JSONException e) {
    			e.printStackTrace();
    			Toast.makeText(getBaseContext(), "Error parsing JSON content!", Toast.LENGTH_LONG).show();
    		}
        } catch(Exception e) {
			e.printStackTrace();
			Toast.makeText(getBaseContext(), "Error recieving data!", Toast.LENGTH_LONG).show();
		}
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.mining_statistics, menu);
        return true;
    }
    
}
