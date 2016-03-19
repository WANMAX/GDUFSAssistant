/**
 * 
 */
package com.ace.gdufsassistant.interfacing.specific.module.personal.center;

import com.ace.gdufsassistant.R;
import com.ace.gdufsassistant.util.ReloadApplication;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;

/**
 * @author WIN8
 *
 */
public class SettingActivity extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.setting);
		((CheckBoxPreference)findPreference("display_photo")).setOnPreferenceChangeListener(
				new OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference arg0, Object arg1) {
						ReloadApplication.getInstance("photo").reload();
						return true;
					}
				});
	}
}
