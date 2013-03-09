package com.steve4448.jbitminingstats;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class MiningStatisticsActivity extends Activity {
	public static final String PREFERENCES_TAG = "MiningStatisticsSettings";
	public TableLayout workerTable;
	public NumberVal workerRate;
	public NumberVal confirmedReward;
	public NumberVal confirmedNamecoinReward;
	public int connectionDelay;
	public String slushsDomain;
	public String slushsAPIKey;
	public boolean autoConnect;
	public boolean showMHSAffix;
	public static Thread workerThread;
	public static Handler handler = new Handler();
	public HashMap<String, TableRow> createdRows = new HashMap<String, TableRow>();

	public static double hashRateVal, confirmedRewardVal, confirmedNamecoinRewardVal, unconfirmedRewardVal, estimatedRewardVal, potentialRewardVal;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mining_statistics);
		workerRate = ((NumberVal)findViewById(R.id.number_val_worker_hash_rate));
		confirmedReward = ((NumberVal)findViewById(R.id.number_val_confirmed_reward));
		confirmedReward.setFormatting("%.5f");
		confirmedNamecoinReward = ((NumberVal)findViewById(R.id.number_val_confirmed_namecoin_reward));
		confirmedNamecoinReward.setFormatting("%.5f");
		workerTable = ((TableLayout)findViewById(R.id.worker_table));
		startJSONFetching();
	}
	
	public void startJSONFetching() {
		loadSettings();
		if(workerThread != null && workerThread.isAlive())
			workerThread.interrupt();
		if(slushsAPIKey.toString().trim().length() == 0) {
			Toast.makeText(this, "Slush's API key has not been set, it's required to fetch JSON data. Please set the API key in the settings.", Toast.LENGTH_LONG).show();
			return;
		}
		final Context context = this;
		workerThread = new Thread() {
			@Override
			public void run() {
				Looper.prepare();
				try {
					while(true) {
						StringBuffer sb = new StringBuffer();
						HttpEntity ent = new DefaultHttpClient().execute(new HttpPost(slushsDomain + slushsAPIKey)).getEntity();
						InputStreamReader reader = new InputStreamReader(ent.getContent());
						char[] data = new char[512];
						int read = -1;
						while((read = reader.read(data)) != -1)
							sb.append(data, 0, read);
						String content = sb.toString();
						try {
							JSONObject jsonContent = new JSONObject(content);
							JSONArray workerNames = jsonContent.getJSONObject("workers").names();
							JSONObject workerList = jsonContent.getJSONObject("workers");
							final ArrayList<MiningWorkerStub> workers = new ArrayList<MiningWorkerStub>();
							for(int i = 0; i < workerNames.length(); i++) {
								JSONObject worker = workerList.getJSONObject(workerNames.getString(i));
								workers.add(new MiningWorkerStub(workerNames.getString(i), worker.getBoolean("alive"), worker.getDouble("hashrate"), worker.getDouble("shares"), worker.getDouble("score")));
							}
							hashRateVal = jsonContent.getDouble("hashrate");
							confirmedRewardVal = jsonContent.getDouble("confirmed_reward");
							confirmedNamecoinRewardVal = jsonContent.getDouble("confirmed_nmc_reward");
							unconfirmedRewardVal = jsonContent.getDouble("unconfirmed_reward");
							estimatedRewardVal = jsonContent.getDouble("estimated_reward");
							potentialRewardVal = confirmedRewardVal + unconfirmedRewardVal + estimatedRewardVal;
							handler.post(new Runnable() {
								public void run() {
									workerRate.setValue(hashRateVal);
									confirmedReward.setValue(confirmedRewardVal);
									confirmedNamecoinReward.setValue(confirmedNamecoinRewardVal);
									if(MoreMiningStatisticsActivity.active)
										MoreMiningStatisticsActivity.updateValues();
									for(MiningWorkerStub worker : workers) {
										if(createdRows.containsKey(worker.name)) {
											TableRow workerRow = createdRows.get(worker.name);
											
											ImageView workerStatus = (ImageView)workerRow.getChildAt(0);
											workerStatus.setImageResource(worker.online ? R.drawable.worker_online : R.drawable.worker_offline);
										
											NumberVal workerRate = (NumberVal)workerRow.getChildAt(2);
											workerRate.setValue(worker.hashRate);
											
											NumberVal workerShare = (NumberVal)workerRow.getChildAt(3);
											workerShare.setValue(worker.share);
											
											NumberVal workerScore = (NumberVal)workerRow.getChildAt(4);
											workerScore.setValue(worker.score);
										} else {
											TableRow workerRow = new TableRow(context);
											ImageView workerStatus = new ImageView(context);
											workerStatus.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f));
											workerStatus.setImageResource(worker.online ? R.drawable.worker_online : R.drawable.worker_offline);
											workerRow.addView(workerStatus);
											
											TextView workerName = new TextView(context);
											workerName.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f));
											workerName.setText(worker.name);
											workerRow.addView(workerName);
											
											NumberVal workerRate = new NumberVal(context);
											workerRate.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f));
											workerRate.setValue(worker.hashRate);
											workerRow.addView(workerRate);
											
											NumberVal workerShare = new NumberVal(context);
											workerShare.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f));
											workerShare.setValue(worker.share);
											workerRow.addView(workerShare);
											
											NumberVal workerScore = new NumberVal(context);
											workerScore.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f));
											workerScore.setValue(worker.score);
											workerRow.addView(workerScore);
											
											workerTable.addView(workerRow);
											createdRows.put(worker.name, workerRow);
										}
									}
									Toast.makeText(context, "Parsed!", Toast.LENGTH_SHORT).show();
								}
							});
						} catch (JSONException e) {
							e.printStackTrace();
							handler.post(new Runnable() {
								@Override
								public void run() {
									Toast.makeText(context, "Error parsing JSON content!", Toast.LENGTH_LONG).show();
								}
							});
						}
						if(autoConnect)
							Thread.sleep(connectionDelay);
						else
							break;
					}
				} catch (InterruptedException e) {
				} catch (IOException e) {
					handler.post(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(context, "Failed to connect.", Toast.LENGTH_SHORT).show();
							AlertDialog.Builder alert = new AlertDialog.Builder(context);
							alert.setTitle("Connection Error");
							alert.setMessage("There's been some error while connecting to the JSON supplier...\nWould you like to try connecting again?");
							alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									startJSONFetching();
									Toast.makeText(context, "Trying to connect again...", Toast.LENGTH_SHORT).show();
								}
							});
							alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									Toast.makeText(context, "No more attempts to connect will be made.", Toast.LENGTH_LONG).show();
								}
							});
							alert.setOnCancelListener(new OnCancelListener() {
								@Override
								public void onCancel(DialogInterface dialog) {
									Toast.makeText(context, "No more attempts to connect will be made.", Toast.LENGTH_LONG).show();
								}
							});
							alert.setIcon(R.drawable.ic_launcher);
							alert.create().show();
						}
					});
				}
			}
		};
		workerThread.start();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		startJSONFetching();
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if(workerThread != null) workerThread.interrupt();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.mining_statistics, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.action_settings:
				startActivity(new Intent(this, MiningStatisticsSettingsActivity.class));
			return true;
			
			case R.id.action_more_statistics:
				startActivity(new Intent(this, MoreMiningStatisticsActivity.class));
			return true;
			
			case R.id.action_connect_now:
				startJSONFetching();
			return true;
		}
		return false;
	}
	
	public void loadSettings() {
		SharedPreferences prefs = getSharedPreferences(PREFERENCES_TAG, Activity.MODE_PRIVATE);
		autoConnect = prefs.getBoolean("autoConnect", true);
		showMHSAffix = prefs.getBoolean("showMHSAffix", true);
		TextView rateColumn = ((TextView)((TableRow)workerTable.getChildAt(0)).getChildAt(2));
		if(showMHSAffix) {
			if(workerRate.getAffix().equals(""))
				workerRate.setAffix("mh/s");
			if(!rateColumn.getText().toString().endsWith("(mh/s)"))
				rateColumn.setText("Rate\n(mh/s)");
		} else {
			if(!workerRate.getAffix().equals(""))
				workerRate.setAffix("");
			if(rateColumn.getText().toString().endsWith("(mh/s)"))
				rateColumn.setText("Rate");
		}
		connectionDelay = prefs.getInt("connectionDelay", 5000);
		slushsDomain = prefs.getString("slushsDomain", "https://mining.bitcoin.cz/accounts/profile/json/");
		slushsAPIKey = prefs.getString("slushsAPIKey", "");
	}
}