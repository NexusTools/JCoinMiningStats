package net.nexustools.jbitminingstats;

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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class MiningStatisticsActivity extends Activity {
	public TableLayout workerTable;
	public FormattableNumberView workerRate;
	public FormattableNumberView confirmedReward;
	public FormattableNumberView confirmedNamecoinReward;
	public FormattableNumberView unconfirmedReward;
	public FormattableNumberView estimatedReward;
	public FormattableNumberView potentialReward;
	public int connectionDelay;
	public String slushsDomain;
	public String slushsAPIKey;
	public boolean autoConnect;
	public boolean showHashrateUnit;
	public boolean showParseMessage;
	public static Thread workerThread;
	public static Handler handler = new Handler();
	public HashMap<String, TableRow> createdRows = new HashMap<String, TableRow>();
	
	public static double hashRateVal, confirmedRewardVal, confirmedNamecoinRewardVal, unconfirmedRewardVal, estimatedRewardVal, potentialRewardVal;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mining_statistics);
		workerRate = ((FormattableNumberView) findViewById(R.id.number_val_worker_hash_rate));
		confirmedReward = ((FormattableNumberView) findViewById(R.id.number_val_confirmed_reward));
		confirmedReward.setFormatting("%.5f");
		confirmedNamecoinReward = ((FormattableNumberView) findViewById(R.id.number_val_confirmed_namecoin_reward));
		confirmedNamecoinReward.setFormatting("%.5f");
		unconfirmedReward = ((FormattableNumberView) findViewById(R.id.number_val_uncomfirmed_reward));
		unconfirmedReward.setFormatting("%.5f");
		estimatedReward = ((FormattableNumberView) findViewById(R.id.number_val_estimated_reward));
		estimatedReward.setFormatting("%.5f");
		potentialReward = ((FormattableNumberView) findViewById(R.id.number_val_potential_reward));
		potentialReward.setFormatting("%.5f");
		workerTable = ((TableLayout) findViewById(R.id.worker_table));
	}
	
	public void startJSONFetching() {
		if(workerThread != null && workerThread.isAlive())
			workerThread.interrupt();
		if(slushsAPIKey == null || slushsAPIKey.toString().trim().length() == 0) {
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
						if(content.equals("Invalid token")) {
							handler.post(new Runnable() {
								public void run() {
									Toast.makeText(context, "Invalid API key!", Toast.LENGTH_LONG).show();
								}
							});
							break;
						}
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
								@Override
								public void run() {
									workerRate.setValue(hashRateVal);
									confirmedReward.setValue(confirmedRewardVal);
									confirmedNamecoinReward.setValue(confirmedNamecoinRewardVal);
									unconfirmedReward.setValue(unconfirmedRewardVal);
									estimatedReward.setValue(estimatedRewardVal);
									potentialReward.setValue(potentialRewardVal);
									for(MiningWorkerStub worker : workers) {
										if(createdRows.containsKey(worker.name)) {
											TableRow workerRow = createdRows.get(worker.name);
											
											ImageView workerStatus = (ImageView) workerRow.getChildAt(0);
											workerStatus.setImageResource(worker.online ? R.drawable.accept : R.drawable.cross);
											
											FormattableNumberView workerRate = (FormattableNumberView) workerRow.getChildAt(2);
											workerRate.setValue(worker.hashRate);
											
											FormattableNumberView workerShare = (FormattableNumberView) workerRow.getChildAt(3);
											workerShare.setValue(worker.share);
											
											FormattableNumberView workerScore = (FormattableNumberView) workerRow.getChildAt(4);
											workerScore.setValue(worker.score);
										} else {
											TableRow workerRow = new TableRow(context);
											ImageView workerStatus = new ImageView(context);
											workerStatus.setLayoutParams(new TableRow.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
											workerStatus.setImageResource(worker.online ? R.drawable.accept : R.drawable.cross);
											workerRow.addView(workerStatus);
											
											TextView workerName = new TextView(context);
											workerName.setLayoutParams(new TableRow.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
											workerName.setText(worker.name);
											workerRow.addView(workerName);
											
											FormattableNumberView workerRate = new FormattableNumberView(context);
											workerRate.setLayoutParams(new TableRow.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
											workerRate.setValue(worker.hashRate);
											workerRow.addView(workerRate);
											
											FormattableNumberView workerShare = new FormattableNumberView(context);
											workerShare.setLayoutParams(new TableRow.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
											workerShare.setValue(worker.share);
											workerRow.addView(workerShare);
											
											FormattableNumberView workerScore = new FormattableNumberView(context);
											workerScore.setLayoutParams(new TableRow.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
											workerScore.setValue(worker.score);
											workerScore.setFormatting("%.1f");
											workerRow.addView(workerScore);
											
											workerTable.addView(workerRow);
											createdRows.put(worker.name, workerRow);
										}
									}
									if(showParseMessage)
										Toast.makeText(context, "Parsed!", Toast.LENGTH_SHORT).show();
								}
							});
						} catch(JSONException e) {
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
				} catch(InterruptedException e) {} catch(IOException e) {
					e.printStackTrace();
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
		loadSettings();
		startJSONFetching();
	}
	
	@Override
	public void onStart() {
		super.onStart();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(workerThread != null)
			workerThread.interrupt();
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
				
			case R.id.action_show_blocks:
				startActivity(new Intent(this, MiningBlockStatisticsActivity.class));
				return true;
				
			case R.id.action_connect_now:
				startJSONFetching();
				return true;
		}
		return false;
	}
	
	public void loadSettings() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		autoConnect = prefs.getBoolean("setting_auto_connect", false);
		showHashrateUnit = prefs.getBoolean("settings_show_hashrates", false);
		showParseMessage = prefs.getBoolean("settings_show_messages_when_parsed", false);
		TextView rateColumn = ((TextView) ((TableRow) workerTable.getChildAt(0)).getChildAt(2));
		if(showHashrateUnit) {
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
		connectionDelay = Integer.parseInt(prefs.getString("setting_connect_delay", "0"));
		slushsDomain = prefs.getString("settings_slushs_api_domain", null);
		slushsAPIKey = prefs.getString("settings_slushs_api_key", null);
	}
}