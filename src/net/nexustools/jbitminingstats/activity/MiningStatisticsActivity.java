package net.nexustools.jbitminingstats.activity;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import net.nexustools.jbitminingstats.R;
import net.nexustools.jbitminingstats.util.MiningWorkerStub;
import net.nexustools.jbitminingstats.view.FormattableNumberView;

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
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class MiningStatisticsActivity extends Activity {
	public TableLayout workerTableHeader;
	public TableLayout workerTableEntries;
	public FormattableNumberView workerRate;
	public FormattableNumberView confirmedReward;
	public FormattableNumberView confirmedNamecoinReward;
	public FormattableNumberView unconfirmedReward;
	public FormattableNumberView estimatedReward;
	public FormattableNumberView potentialReward;
	public ArrayList<MiningWorkerStub> workers;
	public ProgressBar progressBar;
	public int connectionDelay;
	public String slushsDomain;
	public String slushsAPIKey;
	public boolean showingBlocks;
	public boolean autoConnect;
	public boolean showHashrateUnit;
	public boolean showParseMessage;
	public static boolean canContinue = true;
	public int elapsedTime = 0;
	public static Handler handler = new Handler();
	public static Timer workScheduler;
	public static TimerTask currentTask;
	public ConcurrentHashMap<String, TableRow> createdRows = new ConcurrentHashMap<String, TableRow>();
	public static final int TIME_STEP = 1000 / 20;
	public static final int JSON_FETCH_SUCCESS = 0, JSON_FETCH_PARSE_ERROR = 1, JSON_FETCH_INVALID_TOKEN = 2, JSON_FETCH_CONNECTION_ERROR = 3;
	
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
		workerTableHeader = ((TableLayout) findViewById(R.id.worker_table_header));
		workerTableEntries = ((TableLayout) findViewById(R.id.worker_table_entries));
		progressBar = ((ProgressBar) findViewById(R.id.progress_until_connection));
	}
	
	public void beginFetch() {
		final Context context = this;
		if(workScheduler == null)
			workScheduler = new Timer();
		if(currentTask != null)
			currentTask.cancel();
		
		elapsedTime = connectionDelay;
		canContinue = true;
		
		if(slushsAPIKey == null || slushsAPIKey.toString().trim().length() == 0) {
			Toast.makeText(this, "Slush's API key has not been set, it's required to fetch JSON data. Please set the API key in the settings.", Toast.LENGTH_LONG).show();
			return;
		}
		workScheduler.schedule(currentTask = new TimerTask() {
			@Override
			public void run() {
				elapsedTime += TIME_STEP;
				progressBar.setProgress(elapsedTime > connectionDelay ? connectionDelay : elapsedTime);
				if(elapsedTime >= connectionDelay) {
					final int result = fetchJSONData();
					handler.post(new Runnable() {
						public void run() {
							String problem = null;
							switch(result) {
								case JSON_FETCH_SUCCESS:
								break;
								case JSON_FETCH_PARSE_ERROR:
									problem = "Error parsing JSON content!";
								break;
								case JSON_FETCH_INVALID_TOKEN:
									problem = "Invalid API key!";
								break;
								case JSON_FETCH_CONNECTION_ERROR:
									problem = "Error connecting to JSON supplier!";
								break;
							}
							if(problem != null) {
								canContinue = false;
								Toast.makeText(context, problem, Toast.LENGTH_SHORT).show();
								AlertDialog.Builder alert = new AlertDialog.Builder(context);
								alert.setTitle("Connection Error");
								alert.setMessage(problem + "\nWould you like to try connecting again?");
								alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										Toast.makeText(context, "Trying to connect again...", Toast.LENGTH_SHORT).show();
										beginFetch();
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
							} else {
								canContinue = autoConnect;
								workerRate.setValue(hashRateVal);
								confirmedReward.setValue(confirmedRewardVal);
								confirmedNamecoinReward.setValue(confirmedNamecoinRewardVal);
								unconfirmedReward.setValue(unconfirmedRewardVal);
								estimatedReward.setValue(estimatedRewardVal);
								potentialReward.setValue(potentialRewardVal);
								ArrayList<String> workersFound = new ArrayList<String>();
								workersFound.add(getString(R.string.label_worker_table_header_name));
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
										workerRow.setLayoutParams(new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
										
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
										
										workerTableEntries.addView(workerRow);
										createdRows.put(worker.name, workerRow);
									}
									workersFound.add(worker.name);
								}
								for(String entry : createdRows.keySet()) {
									if(!workersFound.contains(entry)) {
										workerTableEntries.removeView(createdRows.get(entry));
										createdRows.remove(entry);
									}
								}
								if(showParseMessage)
									Toast.makeText(context, "Parsed!", Toast.LENGTH_SHORT).show();
							}
						}
					});
					if(!canContinue) {
						this.cancel();
						currentTask = null;
					} else
						elapsedTime = 0;
					return;
				}
			}
		}, 0, TIME_STEP);
	}
	
	public void stopFetch() {
		if(workScheduler != null) {
			workScheduler.cancel();
			workScheduler.purge();
			workScheduler = null;
		}
	}
	
	public int fetchJSONData() {
		try {
			StringBuffer sb = new StringBuffer();
			HttpEntity ent = new DefaultHttpClient().execute(new HttpPost(slushsDomain + slushsAPIKey)).getEntity();
			InputStreamReader reader = new InputStreamReader(ent.getContent());
			char[] data = new char[512];
			int read = -1;
			while((read = reader.read(data)) != -1)
				sb.append(data, 0, read);
			String content = sb.toString();
			if(content.equals("Invalid token"))
				return JSON_FETCH_INVALID_TOKEN;
			try {
				JSONObject jsonContent = new JSONObject(content);
				JSONArray workerNames = jsonContent.getJSONObject("workers").names();
				JSONObject workerList = jsonContent.getJSONObject("workers");
				workers = new ArrayList<MiningWorkerStub>();
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
				return JSON_FETCH_SUCCESS;
			} catch(JSONException e) {
				e.printStackTrace();
				return JSON_FETCH_PARSE_ERROR;
			}
		} catch(IOException e) {
			e.printStackTrace();
			return JSON_FETCH_CONNECTION_ERROR;
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		loadSettings();
		beginFetch();
	}
	
	@Override
	public void onStart() {
		super.onStart();
	}
	
	@Override
	public void onStop() {
		super.onStop();
		stopFetch();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
	    SharedPreferences.Editor editor = prefs.edit(); {
	    	editor.putBoolean("showing_blocks", showingBlocks);
	    }
	    editor.commit();
	}
	
	
	@Override
	public void onPause() {
		super.onPause();
		stopFetch();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		stopFetch();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.mining_statistics, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.action_show_blocks).setVisible(!showingBlocks);
		menu.findItem(R.id.action_show_miners).setVisible(showingBlocks);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.action_settings:
				startActivity(new Intent(this, MiningStatisticsSettingsActivity.class));
				return true;
				
			case R.id.action_show_blocks:
				showingBlocks = true;
				return true;
				
			case R.id.action_show_miners:
				showingBlocks = false;
				return true;
				
			case R.id.action_connect_now:
				beginFetch();
				return true;
		}
		return false;
	}
	
	public void loadSettings() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		showingBlocks = prefs.getBoolean("showing_blocks", true);
		autoConnect = prefs.getBoolean("settings_auto_connect", true);
		showHashrateUnit = prefs.getBoolean("settings_show_hashrates", true);
		showParseMessage = prefs.getBoolean("settings_show_messages_when_parsed", false);
		TextView rateColumn = ((TextView) ((TableRow) workerTableHeader.getChildAt(0)).getChildAt(2));
		TextView rateColumnStub = ((TextView) ((TableRow) workerTableEntries.getChildAt(0)).getChildAt(2));
		if(showHashrateUnit) {
			if(workerRate.getAffix().equals(""))
				workerRate.setAffix("mh/s");
			if(!rateColumn.getText().toString().endsWith("(mh/s)")) {
				rateColumn.setText("Rate (mh/s)");
				rateColumnStub.setText("Rate (mh/s)");
			}
		} else {
			if(!workerRate.getAffix().equals(""))
				workerRate.setAffix("");
			if(rateColumn.getText().toString().endsWith("(mh/s)")) {
				rateColumn.setText("Rate");
				rateColumnStub.setText("Rate");
			}
		}
		connectionDelay = Integer.parseInt(prefs.getString("settings_connect_delay", getString(R.string.default_option_connection_delay)));
		slushsDomain = prefs.getString("settings_slushs_account_api_domain", getString(R.string.default_option_slushs_miner_domain));
		slushsAPIKey = prefs.getString("settings_slushs_api_key", getString(R.string.default_option_slushs_api_key));
		progressBar.setMax(connectionDelay);
		progressBar.setProgress(connectionDelay);
	}
}