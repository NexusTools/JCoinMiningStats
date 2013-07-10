package net.nexustools.jbitminingstats.activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import net.nexustools.jbitminingstats.R;
import net.nexustools.jbitminingstats.util.BlockStub;
import net.nexustools.jbitminingstats.util.ContentGrabber;
import net.nexustools.jbitminingstats.util.MiningWorkerStub;
import net.nexustools.jbitminingstats.util.Settings;
import net.nexustools.jbitminingstats.view.FormattableNumberView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
	public static final int CONNECTION_DELAY_WARNING_CAP = 15000;
	public static final int TIME_STEP = 1000 / 20;
	public static final int JSON_FETCH_SUCCESS = 0, JSON_FETCH_PARSE_ERROR = 1, JSON_FETCH_INVALID_TOKEN = 2, JSON_FETCH_CONNECTION_ERROR = 3;
	
	public static Handler handler = new Handler();
	public static Timer workScheduler;
	public static TimerTask minerBlockFetchTask;
	public static TimerTask mtGoxFetchTask;
	public static boolean canContinue = true;
	
	public FormattableNumberView workerRate;
	public FormattableNumberView confirmedReward;
	public FormattableNumberView confirmedNamecoinReward;
	public FormattableNumberView unconfirmedReward;
	public FormattableNumberView estimatedReward;
	public FormattableNumberView potentialReward;
	public TableLayout workerTableHeader;
	public TableLayout workerTableEntries;
	public TableLayout blockTableHeader;
	public TableLayout blockTableEntries;
	public ProgressBar progressBar;
	
	public ArrayList<MiningWorkerStub> workers;
	public ArrayList<BlockStub> blocks;
	public ConcurrentHashMap<String, TableRow> createdMinerRows = new ConcurrentHashMap<String, TableRow>();
	public ConcurrentHashMap<String, TableRow> createdBlockRows = new ConcurrentHashMap<String, TableRow>();
	
	public int elapsedTime = 0;
	
	public static Settings settings;
	
	public static double hashRateVal, confirmedRewardVal, confirmedNamecoinRewardVal, unconfirmedRewardVal, estimatedRewardVal, potentialRewardVal;
	public static double mtGoxBTCToCurrencyVal;
	public static boolean mtGoxBTCTOCurrencySymbolSet;
	
	@Override
	public void onStart() {
		super.onStart();
		settings = new Settings(this);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mining_statistics);
		workerRate = ((FormattableNumberView)findViewById(R.id.number_val_worker_hash_rate));
		confirmedReward = ((FormattableNumberView)findViewById(R.id.number_val_confirmed_reward));
		confirmedReward.setFormatting("%.5f");
		confirmedNamecoinReward = ((FormattableNumberView)findViewById(R.id.number_val_confirmed_namecoin_reward));
		confirmedNamecoinReward.setFormatting("%.5f");
		unconfirmedReward = ((FormattableNumberView)findViewById(R.id.number_val_uncomfirmed_reward));
		unconfirmedReward.setFormatting("%.5f");
		estimatedReward = ((FormattableNumberView)findViewById(R.id.number_val_estimated_reward));
		estimatedReward.setFormatting("%.5f");
		potentialReward = ((FormattableNumberView)findViewById(R.id.number_val_potential_reward));
		potentialReward.setFormatting("%.5f");
		workerTableHeader = ((TableLayout)findViewById(R.id.worker_table_header));
		workerTableEntries = ((TableLayout)findViewById(R.id.worker_table_entries));
		blockTableHeader = ((TableLayout)findViewById(R.id.block_table_header));
		blockTableEntries = ((TableLayout)findViewById(R.id.block_table_entries));
		progressBar = ((ProgressBar)findViewById(R.id.progress_until_connection));
	}
	
	public void beginFetch() {
		final Context context = this;
		if(workScheduler == null)
			workScheduler = new Timer();
		if(minerBlockFetchTask != null)
			minerBlockFetchTask.cancel();
		progressBar.setProgress(0);
		progressBar.setMax(settings.getConnectionDelay());
		elapsedTime = settings.getConnectionDelay();
		canContinue = true;
		
		if(settings.getSlushsAPIKey() == null || settings.getSlushsAPIKey().trim().length() == 0) {
			Toast.makeText(this, R.string.problem_json_no_api_key_set, Toast.LENGTH_LONG).show();
			return;
		}
		workScheduler.schedule(minerBlockFetchTask = new TimerTask() {
			@Override
			public void run() {
				elapsedTime += TIME_STEP;
				progressBar.setProgress(elapsedTime > settings.getConnectionDelay() ? settings.getConnectionDelay() : elapsedTime);
				if(elapsedTime >= settings.getConnectionDelay()) {
					final int result = fetchMinerJSONData();
					
					final int result2 = settings.isShowingBlocks() ? fetchBlockJSONData() : 0;
					String pb = null;
					switch(result) {
						case JSON_FETCH_PARSE_ERROR:
							pb = getString(R.string.problem_json_parse_error_miners);
						break;
						case JSON_FETCH_INVALID_TOKEN:
							pb = getString(R.string.problem_json_invalid_token_miners);
						break;
						case JSON_FETCH_CONNECTION_ERROR:
							pb = getString(R.string.problem_json_connection_error_miners);
						break;
					}
					switch(result2) {
						case JSON_FETCH_PARSE_ERROR:
							pb = pb == null ? getString(R.string.problem_json_parse_error_blocks) : pb + "\n" + getString(R.string.problem_json_parse_error_blocks);
						break;
						case JSON_FETCH_INVALID_TOKEN:
							pb = pb == null ? getString(R.string.problem_json_invalid_token_blocks) : pb + "\n" + getString(R.string.problem_json_invalid_token_blocks);
						break;
						case JSON_FETCH_CONNECTION_ERROR:
							pb = pb == null ? getString(R.string.problem_json_connection_error_blocks) : pb + "\n" + getString(R.string.problem_json_connection_error_blocks);
						break;
					}
					final String problem = pb;
					canContinue = (pb == null ? settings.canAutoConnect() : false);
					handler.post(new Runnable() {
						public void run() {
							if(problem != null) {
								Toast.makeText(context, problem, Toast.LENGTH_SHORT).show();
								AlertDialog.Builder alert = new AlertDialog.Builder(context);
								alert.setTitle(R.string.problem_json);
								alert.setMessage(problem + "\n" + getString(R.string.problem_try_again));
								DialogInterface.OnClickListener handler = new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										switch(which) {
											case AlertDialog.BUTTON_POSITIVE:
												Toast.makeText(context, R.string.problem_json_trying_again, Toast.LENGTH_SHORT).show();
												beginFetch();
											break;
											case AlertDialog.BUTTON_NEGATIVE:
												Toast.makeText(context, R.string.problem_json_not_trying_again, Toast.LENGTH_SHORT).show();
											break;
										}
									}
								};
								alert.setPositiveButton(R.string.problem_json_positive, handler);
								alert.setNegativeButton(R.string.problem_json_negative, handler);
								alert.setCancelable(false);
								alert.setIcon(R.drawable.ic_launcher);
								alert.create().show();
							} else {
								workerRate.setValue(hashRateVal);
								confirmedReward.setValue(confirmedRewardVal);
								confirmedNamecoinReward.setValue(confirmedNamecoinRewardVal);
								unconfirmedReward.setValue(unconfirmedRewardVal);
								estimatedReward.setValue(estimatedRewardVal);
								potentialReward.setValue(potentialRewardVal);
								if(settings.isShowingBlocks()) {
									ArrayList<String> blocksFound = new ArrayList<String>();
									blocksFound.add(getString(R.string.label_block_table_header_block));
									for(BlockStub block : blocks) {
										if(createdBlockRows.containsKey(block.id)) {
											TableRow blockRow = createdBlockRows.get(block.id);
											
											FormattableNumberView blockConfirmation = (FormattableNumberView)blockRow.getChildAt(1);
											blockConfirmation.setValue(block.confirmations);
											
											FormattableNumberView blockReward = (FormattableNumberView)blockRow.getChildAt(2);
											blockReward.setValue(block.reward);
											
											FormattableNumberView blockNMCReward = (FormattableNumberView)blockRow.getChildAt(3);
											
											blockNMCReward.setValue(block.nmcReward);
											
											FormattableNumberView blockScore = (FormattableNumberView)blockRow.getChildAt(4);
											blockScore.setValue(block.score);
											
											FormattableNumberView blockShare = (FormattableNumberView)blockRow.getChildAt(5);
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
											if(settings.canMtGoxEffectBlockTable())
												blockReward.setMultiplier(mtGoxBTCToCurrencyVal);
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
											
											ImageView workerStatus = (ImageView)workerRow.getChildAt(0);
											workerStatus.setImageResource(worker.online ? R.drawable.accept : R.drawable.cross);
											
											FormattableNumberView workerRate = (FormattableNumberView)workerRow.getChildAt(2);
											workerRate.setValue(worker.hashRate);
											
											FormattableNumberView workerShare = (FormattableNumberView)workerRow.getChildAt(3);
											workerShare.setValue(worker.share);
											
											FormattableNumberView workerScore = (FormattableNumberView)workerRow.getChildAt(4);
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
								if(settings.canShowParseMessage())
									Toast.makeText(context, R.string.json_parsed, Toast.LENGTH_SHORT).show();
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
	
	public void fetchMtGoxCurrency() {
		// final Context context = this;
		if(workScheduler == null)
			workScheduler = new Timer();
		if(mtGoxFetchTask != null)
			mtGoxFetchTask.cancel();
		
		workScheduler.schedule(mtGoxFetchTask = new TimerTask() {
			@Override
			public void run() {
				final int returnCode = fetchMtGoxJSONData();
				switch(returnCode) {
				// TODO: Error handling...
				}
				handler.post(new Runnable() {
					public void run() {
						if(returnCode == JSON_FETCH_SUCCESS) {
							if(!mtGoxBTCTOCurrencySymbolSet) {
								if(settings.canTheMtGoxBTCTOCurrencySymbolPrefixOrAffix()) {
									confirmedReward.setPrefix(settings.getTheMtGoxBTCToCurrencySymbol());
									unconfirmedReward.setPrefix(settings.getTheMtGoxBTCToCurrencySymbol());
									estimatedReward.setPrefix(settings.getTheMtGoxBTCToCurrencySymbol());
									potentialReward.setPrefix(settings.getTheMtGoxBTCToCurrencySymbol());
									confirmedReward.setSuffix("");
									unconfirmedReward.setSuffix("");
									estimatedReward.setSuffix("");
									potentialReward.setSuffix("");
								} else {
									confirmedReward.setSuffix(settings.getTheMtGoxBTCToCurrencySymbol());
									unconfirmedReward.setSuffix(settings.getTheMtGoxBTCToCurrencySymbol());
									estimatedReward.setSuffix(settings.getTheMtGoxBTCToCurrencySymbol());
									potentialReward.setSuffix(settings.getTheMtGoxBTCToCurrencySymbol());
									confirmedReward.setPrefix("");
									unconfirmedReward.setPrefix("");
									estimatedReward.setPrefix("");
									potentialReward.setPrefix("");
								}
								if(settings.canMtGoxEffectBlockTable()) {
									((TextView)((TableRow)blockTableHeader.getChildAt(0)).getChildAt(2)).setText(getString(R.string.label_block_table_header_reward) + " (" + settings.getTheMtGoxBTCToCurrencySymbol() + ")");
									((TextView)((TableRow)blockTableEntries.getChildAt(0)).getChildAt(2)).setText(getString(R.string.label_block_table_header_reward) + " (" + settings.getTheMtGoxBTCToCurrencySymbol() + ")");
								}
								mtGoxBTCTOCurrencySymbolSet = true;
							}
							confirmedReward.setMultiplier(mtGoxBTCToCurrencyVal);
							unconfirmedReward.setMultiplier(mtGoxBTCToCurrencyVal);
							estimatedReward.setMultiplier(mtGoxBTCToCurrencyVal);
							potentialReward.setMultiplier(mtGoxBTCToCurrencyVal);
						}
					}
				});
			}
		}, 0, settings.getMtGoxFetchDelay());
	}
	
	public void stopFetch() {
		if(minerBlockFetchTask != null)
			minerBlockFetchTask.cancel();
		if(mtGoxFetchTask != null)
			mtGoxFetchTask.cancel();
		
		if(workScheduler != null) {
			workScheduler.cancel();
			workScheduler.purge();
			workScheduler = null;
		}
		
		progressBar.setProgress(0);
	}
	
	public int fetchMinerJSONData() {
		try {
			String content = ContentGrabber.fetch(settings.getSlushsAccountDomain() + settings.getSlushsAPIKey());
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
			String content = ContentGrabber.fetch(settings.getSlushsBlockDomain() + settings.getSlushsAPIKey());
			if(content.equals("Invalid token"))
				return JSON_FETCH_INVALID_TOKEN;
			try {
				JSONObject jsonContent = new JSONObject(content);
				JSONArray blockArray = jsonContent.getJSONObject("blocks").names();
				JSONObject block = jsonContent.getJSONObject("blocks");
				blocks = new ArrayList<BlockStub>();
				for(int i = 0; i < blockArray.length(); i++) {
					JSONObject blockEntry = block.getJSONObject(blockArray.getString(i));
					blocks.add(new BlockStub(blockArray.getString(i), blockEntry.getInt("confirmations"), noNaN(blockEntry.optDouble("reward")), noNaN(blockEntry.optDouble("nmc_reward")), blockEntry.getDouble("total_score"), blockEntry.getDouble("total_shares")));
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
	
	public int fetchMtGoxJSONData() {
		try {
			String content = ContentGrabber.fetch(settings.getMtGoxAPIDomain().replaceAll("~", settings.getMtGoxCurrencyType()));
			try {
				mtGoxBTCToCurrencyVal = new JSONObject(content).getJSONObject("return").getJSONObject("last_local").getDouble("value");
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
		settings.load();
		applySettings();
		beginFetch();
	}
	
	@Override
	public void onStop() {
		super.onStop();
		stopFetch();
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
		menu.findItem(R.id.action_show_blocks).setVisible(!settings.isShowingBlocks());
		menu.findItem(R.id.action_show_miners).setVisible(settings.isShowingBlocks());
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.action_settings:
				startActivity(new Intent(this, MiningStatisticsSettingsActivity.class));
				return true;
				
			case R.id.action_show_blocks:
				settings.setShowingBlocks(true);
				switchTable();
				return true;
				
			case R.id.action_show_miners:
				settings.setShowingBlocks(false);
				switchTable();
				return true;
				
			case R.id.action_connect_now:
				beginFetch();
				return true;
		}
		return false;
	}
	
	public void applySettings() {
		TextView rateColumn = ((TextView)((TableRow)workerTableHeader.getChildAt(0)).getChildAt(2));
		TextView rateColumnStub = ((TextView)((TableRow)workerTableEntries.getChildAt(0)).getChildAt(2));
		if(settings.canShowHashrateUnit()) {
			workerRate.setSuffix(getString(R.string.label_hashrate_suffix));
			rateColumn.setText(R.string.label_worker_table_header_rate_suffixed);
			rateColumnStub.setText(R.string.label_worker_table_header_rate_suffixed);
		} else {
			workerRate.setSuffix("");
			rateColumn.setText(R.string.label_worker_table_header_rate);
			rateColumnStub.setText(R.string.label_worker_table_header_rate);
		}
		
		((TextView)((TableRow)blockTableHeader.getChildAt(0)).getChildAt(2)).setText(R.string.label_block_table_header_reward);
		((TextView)((TableRow)blockTableEntries.getChildAt(0)).getChildAt(2)).setText(R.string.label_block_table_header_reward);
		confirmedReward.setPrefix("");
		unconfirmedReward.setPrefix("");
		estimatedReward.setPrefix("");
		potentialReward.setPrefix("");
		confirmedReward.setSuffix("");
		unconfirmedReward.setSuffix("");
		estimatedReward.setSuffix("");
		potentialReward.setSuffix("");
		confirmedReward.setMultiplier(0);
		unconfirmedReward.setMultiplier(0);
		estimatedReward.setMultiplier(0);
		potentialReward.setMultiplier(0);
		
		if(settings.canAutoConnect()) {
			if(settings.canCheckConnectionDelays() && settings.getConnectionDelay() < CONNECTION_DELAY_WARNING_CAP) {
				AlertDialog.Builder alert = new AlertDialog.Builder(this);
				alert.setTitle(R.string.problem_low_connection_delay);
				alert.setMessage(R.string.label_connection_rate_warning);
				alert.setIcon(R.drawable.ic_launcher);
				DialogInterface.OnClickListener handler = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch(which) {
							case AlertDialog.BUTTON_POSITIVE:
								settings.setConnectionDelay(Integer.parseInt(getString(R.string.default_option_connection_delay)));
								beginFetch();
							break;
							case AlertDialog.BUTTON_NEGATIVE:
								settings.setCheckConnectionDelays(false);
							break;
						}
					}
				};
				alert.setPositiveButton(R.string.problem_low_connection_delay_positive, handler);
				alert.setNeutralButton(R.string.problem_low_connection_delay_neutral, handler);
				alert.setNegativeButton(R.string.problem_low_connection_delay_negative, handler);
				alert.create().show();
			}
		}
		
		if(settings.isMtGoxFetchEnabled()) {
			if(settings.canCheckConnectionDelays() && settings.getMtGoxFetchDelay() < CONNECTION_DELAY_WARNING_CAP) {
				AlertDialog.Builder alert = new AlertDialog.Builder(this);
				alert.setTitle(R.string.problem_low_connection_delay);
				alert.setMessage(R.string.label_mtgox_connection_rate_warning);
				alert.setIcon(R.drawable.ic_launcher);
				DialogInterface.OnClickListener handler = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch(which) {
							case AlertDialog.BUTTON_POSITIVE:
								settings.setMtGoxFetchDelay(Integer.parseInt(getString(R.string.default_option_exchange_fetch_rate)));
								fetchMtGoxCurrency();
							break;
							case AlertDialog.BUTTON_NEGATIVE:
								settings.setCheckConnectionDelays(false);
							break;
						}
					}
				};
				alert.setPositiveButton(R.string.problem_low_connection_delay_positive, handler);
				alert.setNeutralButton(R.string.problem_low_connection_delay_neutral, handler);
				alert.setNegativeButton(R.string.problem_low_connection_delay_negative, handler);
				alert.create().show();
			}
			mtGoxBTCTOCurrencySymbolSet = false;
			fetchMtGoxCurrency();
		}
		
		switchTable();
	}
	
	public void switchTable() {
		for(String entry : createdMinerRows.keySet()) {
			workerTableEntries.removeView(createdMinerRows.get(entry));
			createdMinerRows.remove(entry);
		}
		for(String entry : createdBlockRows.keySet()) {
			blockTableEntries.removeView(createdBlockRows.get(entry));
			createdBlockRows.remove(entry);
		}
		if(workers != null)
			workers.clear();
		if(blocks != null)
			blocks.clear();
		createdMinerRows.clear();
		createdBlockRows.clear();
		if(settings.isShowingBlocks()) {
			((TableLayout)findViewById(R.id.worker_table_header)).setVisibility(View.GONE);
			((ScrollView)findViewById(R.id.worker_table_view)).setVisibility(View.GONE);
			((TableLayout)findViewById(R.id.block_table_header)).setVisibility(View.VISIBLE);
			((ScrollView)findViewById(R.id.block_table_view)).setVisibility(View.VISIBLE);
			((TextView)findViewById(R.id.tabel_label)).setText(R.string.label_block_list_title);
		} else {
			((TableLayout)findViewById(R.id.block_table_header)).setVisibility(View.GONE);
			((ScrollView)findViewById(R.id.block_table_view)).setVisibility(View.GONE);
			((TableLayout)findViewById(R.id.worker_table_header)).setVisibility(View.VISIBLE);
			((ScrollView)findViewById(R.id.worker_table_view)).setVisibility(View.VISIBLE);
			((TextView)findViewById(R.id.tabel_label)).setText(R.string.label_worker_list_title);
		}
		beginFetch();
	}
	
	public double noNaN(double d) {
		return Double.isNaN(d) ? 0D : d;
	}
}