package com.steve4448.jbitminingstats;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

public class MiningStatisticsSettingsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mining_statistics_settings);
		SharedPreferences prefs = getSharedPreferences(MiningStatisticsActivity.PREFERENCES_TAG, Activity.MODE_PRIVATE);
		((CheckBox)findViewById(R.id.button_auto_connect_check_box)).setChecked(prefs.getBoolean("autoConnect", true));
		((CheckBox)findViewById(R.id.button_use_hashrate_affix)).setChecked(prefs.getBoolean("showHashrateUnit", true));
		((TextView)findViewById(R.id.option_connection_delay_text)).setText("" + prefs.getInt("connectionDelay", 5000));
		((TextView)findViewById(R.id.text_option_slushs_domain)).setText(prefs.getString("slushsDomain", "https://mining.bitcoin.cz/accounts/profile/json/"));
		((TextView)findViewById(R.id.text_option_slushs_api_key)).setText(prefs.getString("slushsAPIKey", ""));
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}
	
	@Override
	public void onBackPressed() {
		final Context context = this;
		AlertDialog.Builder alert = new AlertDialog.Builder(context);
		alert.setTitle("Save?");
		alert.setMessage("Would you like to apply your settings?");
		alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				SharedPreferences.Editor prefs = getSharedPreferences(MiningStatisticsActivity.PREFERENCES_TAG, Activity.MODE_PRIVATE).edit();
				prefs.putBoolean("autoConnect", ((CheckBox)findViewById(R.id.button_auto_connect_check_box)).isChecked());
				prefs.putBoolean("showHashrateUnit", ((CheckBox)findViewById(R.id.button_use_hashrate_affix)).isChecked());
				prefs.putInt("connectionDelay", Integer.parseInt(((TextView)findViewById(R.id.option_connection_delay_text)).getText().toString()));
				prefs.putString("slushsDomain", ((TextView)findViewById(R.id.text_option_slushs_domain)).getText().toString());
				prefs.putString("slushsAPIKey", ((TextView)findViewById(R.id.text_option_slushs_api_key)).getText().toString());
				prefs.commit();
				Toast.makeText(context, "Settings saved.", Toast.LENGTH_SHORT).show();
				finish();
			}
		});
		alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});
		alert.setIcon(R.drawable.ic_launcher);
		alert.create().show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.mining_statistics_settings, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.id.action_back) {
			onBackPressed();
			return true;
		}
		return false;
	}
}
