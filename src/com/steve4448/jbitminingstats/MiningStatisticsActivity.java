package com.steve4448.jbitminingstats;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

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
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class MiningStatisticsActivity extends Activity {
	public LinearLayout workerTable;
	public NumberVal workerRate;
	public NumberVal confirmedReward;
	public NumberVal confirmedNamecoinReward;
	public boolean hasInitialized = false;
	public static Thread workerThread;
	public static Handler handler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mining_statistics);
		workerRate = ((NumberVal)findViewById(R.id.number_val_worker_hash_rate));
		confirmedReward = ((NumberVal)findViewById(R.id.number_val_confirmed_reward));
		confirmedReward.setFormatting("%.5f");
		confirmedNamecoinReward = ((NumberVal)findViewById(R.id.number_val_confirmed_namecoin_reward));
		confirmedNamecoinReward.setFormatting("%.5f");
		workerTable = ((LinearLayout)findViewById(R.id.worker_table_layout));
		final Context context = this;
		if(savedInstanceState == null || !savedInstanceState.getBoolean("hasInitialized"))
			workerThread = new Thread() {
			@Override
			public void run() {
				Looper.prepare();
				try {
					while(true) {
						StringBuffer sb = new StringBuffer();
						HttpEntity ent = new DefaultHttpClient().execute(new HttpPost("https://mining.bitcoin.cz/accounts/profile/json/149845-8e60ac1e3ae4489add732c1d2b377965")).getEntity();
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
							final ArrayList<TableRow> createdRows = new ArrayList<TableRow>();
							for(int i = 0; i < workerNames.length(); i++) {
								TableRow workerRow = new TableRow(context);
								JSONObject worker = workerList.getJSONObject(workerNames.getString(i));

								ImageView workerStatus = new ImageView(context);
								workerStatus.setImageResource(worker.getBoolean("alive") ? R.drawable.worker_online : R.drawable.worker_offline);
								workerRow.addView(workerStatus);

								TextView workerName = new TextView(context);
								workerName.setText(workerNames.getString(i));
								workerRow.addView(workerName);

								NumberVal workerRate = new NumberVal(context);
								workerRate.setValue(worker.getDouble("hashrate"));
								workerRow.addView(workerRate);

								NumberVal workerShares = new NumberVal(context);
								workerShares.setValue(worker.getDouble("shares"));
								workerRow.addView(workerShares);

								createdRows.add(workerRow);
							}
							final double hashRateVal = jsonContent.getDouble("hashrate");
							final double confirmedRewardVal = jsonContent.getDouble("confirmed_reward");
							final double confirmedNamecoinRewardVal = jsonContent.getDouble("confirmed_nmc_reward");
							final double unconfirmedRewardVal = jsonContent.getDouble("unconfirmed_reward");
							final double estimatedRewardVal = jsonContent.getDouble("estimated_reward");
							final double potentialRewardVal = confirmedRewardVal + unconfirmedRewardVal + estimatedRewardVal;
							handler.post(new Runnable() {
								public void run() {
									workerRate.setValue(hashRateVal);
									confirmedReward.setValue(confirmedRewardVal);
									confirmedNamecoinReward.setValue(confirmedNamecoinRewardVal);
									if(MoreMiningStatisticsActivity.active) {
										MoreMiningStatisticsActivity.unconfirmedReward.setValue(unconfirmedRewardVal);
										MoreMiningStatisticsActivity.estimatedReward.setValue(estimatedRewardVal);
										MoreMiningStatisticsActivity.potentialReward.setValue(potentialRewardVal);
									}
									for(TableRow nR : createdRows)
										workerTable.addView(nR);
									Toast.makeText(context, "Parsed!", Toast.LENGTH_SHORT).show();
								}
							});
							hasInitialized = true;
						} catch (JSONException e) {
							e.printStackTrace();
							handler.post(new Runnable() {
								@Override
								public void run() {
									Toast.makeText(context, "Error parsing JSON content!", Toast.LENGTH_LONG).show();
								}
							});
						}
						Thread.sleep(5000);
					}
				} catch (InterruptedException e) {
				} catch (IOException e) {
					handler.post(new Runnable() {
						@Override
						public void run() {
							AlertDialog.Builder alert = new AlertDialog.Builder(context);
							alert.setTitle("Connection Error");
							alert.setMessage("There's been some error while retriving the JSON data...\nWould you like to try connecting again?");
							alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									
								}
							});
							alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									
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
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		workerThread.interrupt();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.mining_statistics, menu);
		return true;
	}

	@Override
	protected void onSaveInstanceState(Bundle saveState) {
		super.onSaveInstanceState(saveState);
		saveState.putBoolean("hasInitialized", hasInitialized);
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