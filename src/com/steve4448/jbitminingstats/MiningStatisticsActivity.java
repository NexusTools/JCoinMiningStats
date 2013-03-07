package com.steve4448.jbitminingstats;

import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class MiningStatisticsActivity extends Activity {
	public LinearLayout workerTable;
	public TextView workerRate;
	public TextView confirmedReward;
	public TextView confirmedNamecoinReward;
	public boolean hasInitialized = false;
	public static Thread workerThread;
	public Handler handler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mining_statistics);
		workerRate = ((TextView)findViewById(R.id.number_val_worker_hash_rate));
		confirmedReward = ((TextView)findViewById(R.id.number_val_confirmed_reward));
		confirmedNamecoinReward = ((TextView)findViewById(R.id.number_val_confirmed_namecoin_reward));
		workerTable = ((LinearLayout)findViewById(R.id.worker_table_layout));
		if(savedInstanceState == null || !savedInstanceState.getBoolean("hasInitialized"))
			workerThread = new Thread() {
			@Override
			public void run() {
				try {
					while(true) {
						handler.post(new Runnable() {
							@Override
							public void run() {
								Context context = workerTable.getContext();
								try {
									StringBuffer sb = new StringBuffer();
									String content = "";
									HttpEntity ent = new DefaultHttpClient().execute(new HttpPost("https://mining.bitcoin.cz/accounts/profile/json/149845-8e60ac1e3ae4489add732c1d2b377965")).getEntity();
									InputStreamReader reader = new InputStreamReader(ent.getContent());
									char[] data = new char[512];
									int read = -1;
									while((read = reader.read(data)) != -1)
										sb.append(data, 0, read);
									content = sb.toString();
									try {
										JSONObject jsonContent = new JSONObject(content);
										JSONArray workerNames = jsonContent.getJSONObject("workers").names();
										JSONObject workerList = jsonContent.getJSONObject("workers");
										for(int i = 0; i < workerNames.length(); i++) {
											TableRow workerRow = new TableRow(context);

											JSONObject worker = workerList.getJSONObject(workerNames.getString(i));

											ImageView workerStatus = new ImageView(context);
											workerStatus.setImageResource(worker.getBoolean("alive") ? R.drawable.worker_online : R.drawable.worker_offline);
											workerRow.addView(workerStatus);

											TextView workerName = new TextView(context);
											workerName.setText(workerNames.getString(i));
											workerRow.addView(workerName);

											TextView workerRate = new TextView(context);
											workerRate.setText(worker.getString("hashrate") + "mh/s");
											workerRow.addView(workerRate);

											TextView workerShares = new TextView(context);
											workerShares.setText(worker.getString("shares"));
											workerRow.addView(workerShares);

											workerTable.addView(workerRow);
										}
										workerRate.setText("" + jsonContent.getDouble("hashrate"));
										confirmedReward.setText(String.format("%.5f", jsonContent.getDouble("confirmed_reward")));
										confirmedNamecoinReward.setText(String.format("%.5f", jsonContent.getDouble("confirmed_nmc_reward")));
										if(MoreMiningStatisticsActivity.active) {
											MoreMiningStatisticsActivity.unconfirmedReward.setText(String.format("%.5f", jsonContent.getDouble("unconfirmed_reward")));
											MoreMiningStatisticsActivity.estimatedReward.setText(String.format("%.5f", jsonContent.getDouble("estimated_reward")));
											MoreMiningStatisticsActivity.potentialReward.setText(String.format("%.5f", (jsonContent.getDouble("confirmed_reward") + jsonContent.getDouble("unconfirmed_reward") + jsonContent.getDouble("estimated_reward"))));
										}
										hasInitialized = true;
										Log.i("La", "la.");
										Toast.makeText(context, "Parsed!", Toast.LENGTH_SHORT).show();
									} catch (JSONException e) {
										e.printStackTrace();
										Toast.makeText(context, "Error parsing JSON content!", Toast.LENGTH_LONG).show();
									}
								} catch(Exception e) {
									e.printStackTrace();
									Toast.makeText(context, "Error recieving data!", Toast.LENGTH_LONG).show();
								}
							}
						});
						Thread.sleep(5000);
					}
				} catch (InterruptedException e) {}
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