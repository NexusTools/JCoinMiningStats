package net.nexustools.jbitminingstats;

import net.nexustools.jbitminingstats.R;

import android.os.Bundle;
import android.preference.PreferenceActivity;


public class MiningStatisticsSettingsActivity extends PreferenceActivity {

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
	}
}
