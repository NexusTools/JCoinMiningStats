package net.nexustools.jbitminingstats.activity;

import net.nexustools.jbitminingstats.R;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class MiningStatisticsSettingsActivity extends PreferenceActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
	}
}
