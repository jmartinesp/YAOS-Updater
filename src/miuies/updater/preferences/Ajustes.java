package miuies.updater.preferences;

import miuies.updater.R;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;

public class Ajustes extends PreferenceActivity{
	
	private String TAG = "MIUIESUpdater-Preferences";
	private SharedPreferences sp;
	private Editor editor;
	
	@Override
	  protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    addPreferencesFromResource(R.xml.preferences);
	    CheckBoxPreference check_init = (CheckBoxPreference) findPreference("check_init");
	    sp = getSharedPreferences("variables", MODE_PRIVATE);
	    editor = sp.edit();
	    check_init.setDefaultValue(sp.getBoolean("check_init", true));
	    check_init.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if(preference.equals(findPreference("check_init"))){	
					if(newValue.equals(false)){
						editor.putBoolean("check_init", false);			
					}else{
						editor.putBoolean("check_init", true);
					}
					editor.commit();
					Log.d(TAG,"Check_init "+newValue.toString());
					return true;
				}
				return false;
			}
		});
	    CheckBoxPreference check_md5 = (CheckBoxPreference) findPreference("force_md5");
	    check_md5.setDefaultValue(sp.getBoolean("md5", true));
	    check_md5.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){

			@Override
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				if(newValue.equals(true)){
					editor.putBoolean("md5", true);
				}else{
					editor.putBoolean("md5", false);
				}
				editor.commit();
				return true;
			}
	    	
	    });
	  }


}
