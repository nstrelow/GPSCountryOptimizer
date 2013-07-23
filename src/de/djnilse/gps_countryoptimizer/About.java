package de.djnilse.gps_countryoptimizer;

import android.preference.PreferenceActivity;
import android.os.Bundle;

public class About extends PreferenceActivity {

	/** Called when the activity is first created. */
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.about);
	}
}