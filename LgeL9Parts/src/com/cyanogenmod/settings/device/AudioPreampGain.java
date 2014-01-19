/*
 * Copyright (C) 2012 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cyanogenmod.settings.device;

import android.content.Context;
import android.util.AttributeSet;
import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.widget.Toast;
import android.util.Log;

public class AudioPreampGain extends ListPreference implements OnPreferenceChangeListener {

    private static final String FILE_GAIN = "/sys/class/misc/voodoo_sound/twl6040_mic_preamp_gain_";
    private static final String FILE_ATTN = "/sys/class/misc/voodoo_sound/twl6040_mic_preamp_attn_";

    private static Context mContext; 
    private String mSuffix;

    public static boolean isSupported() {
        return Utils.fileExists(FILE_GAIN + "main");
    }

    private static void writeFiles(String suffix, int value)
    {
        int gain, attn;
        
        if (value < 0) {
            // default
            gain = -1;
            attn = 0;
        } else {
            gain = value - 6; // gain range is 0..24dB
            attn = 0; // atteniaton is a -6dB toggle 

            // app gain range is 0..30 dB; this includes 24dB preamp gain and -6dB atteniation toggle
            
            if (value <= 12) { // start applying attenuatin from 12dB down
                attn = 1;
                gain += 6;
            }
        }
      
        Utils.writeValue(FILE_ATTN + suffix, Integer.toString(attn));
        Utils.writeValue(FILE_GAIN + suffix, Integer.toString(gain));
    }

    public static void restore(Context context, String suffix) {
        if (!isSupported()) {
            return;
        }

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String bootValue = sharedPrefs.getString(AudioFragment.KEY_AUDIO_PREAMP_GAIN + "_boot_" + suffix, "-1");
        writeFiles(suffix, Integer.parseInt(bootValue));

        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(AudioFragment.KEY_AUDIO_PREAMP_GAIN + "_" + suffix, bootValue);
        editor.commit();

        //Log.i("LgeL9Parts", "Restored preamp (" + suffix + ") bootValue: " + bootValue);
    }

    public AudioPreampGain(Context context, String suffix) {
        super(context);
        mContext = context;
        mSuffix = suffix;
    }

    public boolean onPreferenceChange(Preference preference, Object value) {
        
        int ivalue = Integer.parseInt(value.toString());
        writeFiles(mSuffix, ivalue);
  	if (ivalue > 6 || ivalue == -1) {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
            editor.putString(AudioFragment.KEY_AUDIO_PREAMP_GAIN + "_boot_" + mSuffix, value.toString());
            editor.commit();
            //Log.i("LgeL9Parts", "Saved preamp (" + mSuffix + ") bootValue: " + value);
  	} else if (ivalue >= 0) {
            Toast toast = Toast.makeText(mContext, R.string.audio_mic_preamp_noboot_toast, Toast.LENGTH_SHORT);
            toast.show();
  	}
        updateSummary((ListPreference) preference, ivalue);
        return true;
    }

    public static void updateSummary(ListPreference preference, int value) {
        final CharSequence[] entries = preference.getEntries();
        final CharSequence[] values = preference.getEntryValues();
        for (int i = 0; i < values.length; i++) {
            int curVal = Integer.parseInt(values[i].toString());
            if (value == curVal) {
                preference.setSummary(entries[i].toString());
                return;
            }
        }
        preference.setSummary("");
    }
}
