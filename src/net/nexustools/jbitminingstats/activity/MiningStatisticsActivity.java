package net.nexustools.jbitminingstats.activity;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import net.nexustools.jbitminingstats.R;
import net.nexustools.jbitminingstats.util.BlockStub;
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
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class MiningStatisticsActivity extends Activity {
	public TableLayout workerTableHeader;
	public TableLayout workerTableEntries;
	public TableLayout blockTableHeader;
	public TableLayout blockTableEntries;
	public FormattableNumberView workerRate;
	public FormattableNumberView confirmedReward;
	public FormattableNumberView confirmedNamecoinReward;
	public FormattableNumberView unconfirmedReward;
	public FormattableNumberView estimatedReward;
	public FormattableNumberView potentialReward;
	
	public ProgressBar progressBar;
	public int connectionDelay;
	public String slushsAccountDomain;
	public String slushsBlockDomain;
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
	public ArrayList<MiningWorkerStub> workers;
	public ArrayList<BlockStub> blocks;
	public ConcurrentHashMap<String, TableRow> createdMinerRows = new ConcurrentHashMap<String, TableRow>();
	public ConcurrentHashMap<String, TableRow> createdBlockRows = new ConcurrentHashMap<String, TableRow>();
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
		blockTableHeader = ((TableLayout) findViewById(R.id.block_table_header));
		blockTableEntries = ((TableLayout) findViewById(R.id.block_table_entries));
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
					final int result = fetchMinerJSONData();
					final int result2 = showingBlocks ? fetchBlockJSONData() : 0;
					String pb = null;
					switch(result) {
						case JSON_FETCH_SUCCESS:
						break;
						case JSON_FETCH_PARSE_ERROR:
							pb = "Error parsing JSON content for miners.";
						break;
						case JSON_FETCH_INVALID_TOKEN:
							pb = "Invalid API key for miners.";
						break;
						case JSON_FETCH_CONNECTION_ERROR:
							pb = "Error connecting to JSON supplier for miners.";
						break;
					}
					switch(result2) {
						case JSON_FETCH_SUCCESS:
						break;
						case JSON_FETCH_PARSE_ERROR:
							pb = "Error parsing JSON content for blocks.";
						break;
						case JSON_FETCH_INVALID_TOKEN:
							pb = "Invalid API key for blocks.";
						break;
						case JSON_FETCH_CONNECTION_ERROR:
							pb = "Error connecting to JSON supplier for blocks.";
						break;
					}
					final String problem = pb;
					canContinue = (pb == null ? autoConnect : false);
					handler.post(new Runnable() {
						public void run() {
							if(problem != null) {
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
								workerRate.setValue(hashRateVal);
								confirmedReward.setValue(confirmedRewardVal);
								confirmedNamecoinReward.setValue(confirmedNamecoinRewardVal);
								unconfirmedReward.setValue(unconfirmedRewardVal);
								estimatedReward.setValue(estimatedRewardVal);
								potentialReward.setValue(potentialRewardVal);
								if(showingBlocks) {
									ArrayList<String> blocksFound = new ArrayList<String>();
									blocksFound.add(getString(R.string.label_block_table_header_block));
									for(BlockStub block : blocks) {
										if(createdBlockRows.containsKey(block.id)) {
											TableRow blockRow = createdBlockRows.get(block.id);
											
											FormattableNumberView blockConfirmation = (FormattableNumberView) blockRow.getChildAt(1);
											blockConfirmation.setValue(block.confirmations);
											
											FormattableNumberView blockReward = (FormattableNumberView) blockRow.getChildAt(2);
											blockReward.setValue(block.reward);
											
											FormattableNumberView blockNMCReward = (FormattableNumberView) blockRow.getChildAt(3);
											blockNMCReward.setValue(block.nmcReward);
											
											FormattableNumberView blockScore = (FormattableNumberView) blockRow.getChildAt(4);
											blockScore.setValue(block.score);
											
											FormattableNumberView blockShare = (FormattableNumberView) blockRow.getChildAt(5);
											blockShare.setValue(block.share);
										} else {
											TableRow blockRow = new TableRow(context);
											blockRow.setLayoutParams(new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
											
											TextView blockName = new TextView(context);
											blockName.setLayoutParams(new TableRow.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
											blockName.setText(block.id);
											blockRow.addView(blockName);
											
											FormattableNumberView blockConfirmations = new FormattableNumberView(context);
											blockConfirmations.setLayoutParams(new TableRow.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
											blockConfirmations.setValue(block.confirmations);
											blockRow.addView(blockConfirmations);
											
											FormattableNumberView blockReward = new FormattableNumberView(context);
											blockReward.setLayoutParams(new TableRow.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
											blockReward.setValue(block.reward);
											blockRow.addView(blockReward);
											
											FormattableNumberView blockNMCReward = new FormattableNumberView(context);
											blockNMCReward.setLayoutParams(new TableRow.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
											blockNMCReward.setValue(block.nmcReward);
											blockRow.addView(blockNMCReward);
											
											FormattableNumberView blockScore = new FormattableNumberView(context);
											blockScore.setLayoutParams(new TableRow.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
											blockScore.setValue(block.score);
											blockRow.addView(blockScore);
											
											FormattableNumberView blockShare = new FormattableNumberView(context);
											blockShare.setLayoutParams(new TableRow.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
											blockShare.setValue(block.share);
											blockRow.addView(blockShare);
											
											blockTableEntries.addView(blockRow);
											createdBlockRows.put(block.id, blockRow);
										}
										blocksFound.add(block.id);
									}
									for(String entry : createdBlockRows.keySet()) {
										if(!blocksFound.contains(entry)) {
											blockTableEntries.removeView(createdBlockRows.get(entry));
											createdBlockRows.remove(entry);
										}
									}
								} else {
									ArrayList<String> workersFound = new ArrayList<String>();
									workersFound.add(getString(R.string.label_worker_table_header_name));
									for(MiningWorkerStub worker : workers) {
										if(createdMinerRows.containsKey(worker.name)) {
											TableRow workerRow = createdMinerRows.get(worker.name);
											
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
											createdMinerRows.put(worker.name, workerRow);
										}
										workersFound.add(worker.name);
									}
									for(String entry : createdMinerRows.keySet()) {
										if(!workersFound.contains(entry)) {
											workerTableEntries.removeView(createdMinerRows.get(entry));
											createdMinerRows.remove(entry);
										}
									}
								}
								if(showParseMessage)
									Toast.makeText(context, "Parsed!", Toast.LENGTH_SHORT).show();
							}
						}
					});
					if(!canContinue) {
						stopFetch();
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
		progressBar.setProgress(0);
	}
	
	public int fetchMinerJSONData() {
		try {
			StringBuffer sb = new StringBuffer();
			HttpEntity ent = new DefaultHttpClient().execute(new HttpPost(slushsAccountDomain + slushsAPIKey)).getEntity();
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
	
	public int fetchBlockJSONData() {
		try {
			StringBuffer sb = new StringBuffer();
			HttpEntity ent = new DefaultHttpClient().execute(new HttpPost(slushsBlockDomain + slushsAPIKey)).getEntity();
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
				JSONArray blockArray = jsonContent.getJSONObject("blocks").names();
				JSONObject block = jsonContent.getJSONObject("blocks");
				blocks = new ArrayList<BlockStub>();
				for(int i = 0; i < blockArray.length(); i++) {
					JSONObject blockEntry = block.getJSONObject(blockArray.getString(i));
					blocks.add(new BlockStub(blockArray.getString(i), blockEntry.getInt("confirmations"), blockEntry.getDouble("reward"), blockEntry.getDouble("nmc_reward"), blockEntry.getDouble("total_score"), blockEntry.getDouble("total_shares")));
				}
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
		SharedPreferences.Editor editor = prefs.edit();
		{
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
				switchTable();
				return true;
				
			case R.id.action_show_miners:
				showingBlocks = false;
				switchTable();
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
			workerRate.setAffix(getString(R.string.label_hashrate_affix));
			rateColumn.setText(R.string.label_worker_table_header_rate_affixed);
			rateColumnStub.setText(R.string.label_worker_table_header_rate_affixed);
		} else {
			workerRate.setAffix("");
			rateColumn.setText(R.string.label_worker_table_header_rate);
			rateColumnStub.setText(R.string.label_worker_table_header_rate);
		}
		connectionDelay = Integer.parseInt(prefs.getString("settings_connect_delay", getString(R.string.default_option_connection_delay)));
		slushsAccountDomain = prefs.getString("settings_slushs_account_api_domain", getString(R.string.default_option_slushs_miner_domain));
		slushsBlockDomain = prefs.getString("settings_slushs_block_api_domain", getString(R.string.default_option_slushs_miner_domain));
		slushsAPIKey = prefs.getString("settings_slushs_api_key", getString(R.string.default_option_slushs_api_key));
		progressBar.setMax(connectionDelay);
		progressBar.setProgress(connectionDelay);
		switchTable();
	}
	
	public void switchTable() {
		if(showingBlocks) {
			((TableLayout) findViewById(R.id.worker_table_header)).setVisibility(View.GONE);
			((ScrollView) findViewById(R.id.worker_table_view)).setVisibility(View.GONE);
			for(String entry : createdMinerRows.keySet()) {
				workerTableEntries.removeView(createdMinerRows.get(entry));
				createdMinerRows.remove(entry);
			}
			((TableLayout) findViewById(R.id.block_table_header)).setVisibility(View.VISIBLE);
			((ScrollView) findViewById(R.id.block_table_view)).setVisibility(View.VISIBLE);
			((TextView) findViewById(R.id.tabel_label)).setText(R.string.label_block_list_title);
			if(workers != null)
				workers.clear();
			createdMinerRows.clear();
		} else {
			((TableLayout) findViewById(R.id.block_table_header)).setVisibility(View.GONE);
			((ScrollView) findViewById(R.id.block_table_view)).setVisibility(View.GONE);
			for(String entry : createdBlockRows.keySet()) {
				blockTableEntries.removeView(createdBlockRows.get(entry));
				createdBlockRows.remove(entry);
			}
			((TableLayout) findViewById(R.id.worker_table_header)).setVisibility(View.VISIBLE);
			((ScrollView) findViewById(R.id.worker_table_view)).setVisibility(View.VISIBLE);
			((TextView) findViewById(R.id.tabel_label)).setText(R.string.label_worker_list_title);
			if(blocks != null)
				blocks.clear();
			createdBlockRows.clear();
		}
		beginFetch();
	}
}