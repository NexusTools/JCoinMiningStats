package com.steve4448.jbitminingstats;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MoreMiningStatisticsActivity extends Activity {
	public static boolean active = false;
	public static NumberVal unconfirmedReward;
	public static NumberVal estimatedReward;
	public static NumberVal potentialReward;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mining_statistics_more);
		unconfirmedReward = ((NumberVal)findViewById(R.id.number_val_uncomfirmed_reward));
		unconfirmedReward.setFormatting("%.5f");
		estimatedReward = ((NumberVal)findViewById(R.id.number_val_estimated_reward));
		estimatedReward.setFormatting("%.5f");
		potentialReward = ((NumberVal)findViewById(R.id.number_val_potential_reward));
		potentialReward.setFormatting("%.5f");
		updateValues();
	}
	
	public static void updateValues() {
		unconfirmedReward.setValue(MiningStatisticsActivity.unconfirmedRewardVal);
		estimatedReward.setValue(MiningStatisticsActivity.estimatedRewardVal);
		potentialReward.setValue(MiningStatisticsActivity.potentialRewardVal);
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
