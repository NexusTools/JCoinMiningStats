package com.steve4448.jbitminingstats;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MoreMiningStatisticsActivity extends Activity {
	public static boolean active = false;
	public static TextView unconfirmedReward;
	public static TextView estimatedReward;
	public static TextView potentialReward;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mining_statistics_more);
		unconfirmedReward = ((TextView)findViewById(R.id.number_val_uncomfirmed_reward));
		estimatedReward = ((TextView)findViewById(R.id.number_val_estimated_reward));
		potentialReward = ((TextView)findViewById(R.id.number_val_potential_reward));
	}

	@Override
	protected void onResume() {
		super.onResume();
		active = true;
	}

	@Override
	protected void onStop() {
		super.onStop();
		active = false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.mining_statistics_more, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.id.action_back) {
			finish();
			return true;
		}
		return false;
	}
}
