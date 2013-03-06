package com.steve4448.jbitminingstats;

import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class MiningStatisticsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mining_statistics);
        
        try {
        	StringBuffer sb = new StringBuffer();
        	String content = "";
        	Toast.makeText(getBaseContext(), "Connecting to JSON supplier...", Toast.LENGTH_SHORT).show();
        	HttpEntity ent = new DefaultHttpClient().execute(new HttpPost("https://mining.bitcoin.cz/accounts/profile/json/149845-8e60ac1e3ae4489add732c1d2b377965")).getEntity();
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
    				
    				ImageView workerStatus = new ImageView(this);
    				workerStatus.setImageResource(worker.getBoolean("alive") ? R.drawable.worker_online : R.drawable.worker_offline);
    				workerRow.addView(workerStatus);
    				
    				TextView workerName = new TextView(this);
    				workerName.setText(workerNames.getString(i));
    				workerRow.addView(workerName);
    				
    				TextView workerRate = new TextView(this);
    				workerRate.setText(worker.getString("hashrate") + "mh/s");
    				workerRow.addView(workerRate);
    				
    				TextView workerShares = new TextView(this);
    				workerShares.setText(worker.getString("shares"));
    				workerRow.addView(workerShares);
    				
    				workerTable.addView(workerRow);
    			}
    			((TextView)findViewById(R.id.number_val_confirmed_reward)).setText(String.format("%.5f", jsonContent.getDouble("confirmed_reward")));
    			((TextView)findViewById(R.id.number_val_confirmed_namecoin_reward)).setText(String.format("%.5f", jsonContent.getDouble("confirmed_nmc_reward")));
    			//((TextView)findViewById(R.id.number_val_uncomfirmed_reward)).setText(String.format("%.5f", jsonContent.getDouble("unconfirmed_reward")));
    			//((TextView)findViewById(R.id.number_val_estimated_reward)).setText(String.format("%.5f", jsonContent.getDouble("estimated_reward")));
    			//((TextView)findViewById(R.id.number_val_potential_reward)).setText(String.format("%.5f", (jsonContent.getDouble("confirmed_reward") + jsonContent.getDouble("unconfirmed_reward") + jsonContent.getDouble("estimated_reward"))));
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
        getMenuInflater().inflate(R.menu.mining_statistics, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	if(item.getItemId() == R.id.action_more_statistics) {
    		startActivity(new Intent(this, MoreMiningStatisticsActivity.class));
    		return true;
    	}
    	return false;
    }
    
}
