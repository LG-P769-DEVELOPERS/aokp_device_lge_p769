/*
 * Copyright (C) 2011 The CyanogenMod Project
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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.ListPreference;
import android.util.Log;
import com.cyanogenmod.settings.device.R;

public class AudioFragment extends PreferenceFragment {

    public static final String KEY_AUDIO_HPVOL = "audio_hpvol";
    public static final String KEY_AUDIO_PREAMP_GAIN = "audio_mic_preamp_gain";

    private static final String MIC_MAIN_SUFFIX = "main";
    private static final String MIC_SUB_SUFFIX = "sub";

    private AudioOutputGain mAudioOutputGain;
    private ListPreference mAudioInputGain;
    

    public static boolean isSupported() {
        return AudioOutputGain.isSupported() || AudioPreampGain.isSupported();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!AudioOutputGain.isSupported())
            return; 

        addPreferencesFromResource(R.xml.audio);

        mAudioOutputGain = (AudioOutputGain) findPreference(AudioFragment.KEY_AUDIO_HPVOL);
        //mAudioOutputGain.setEnabled(AudioOutputGain.isSupported());
        mAudioOutputGain.updateSummary();
                                          
        mAudioInputGain = (ListPreference) findPreference(KEY_AUDIO_PREAMP_GAIN + "_" + MIC_MAIN_SUFFIX);
        //mAudioInputGain.setEnabled(mAudioInputGain.isSupported());
        mAudioInputGain.setOnPreferenceChangeListener(new AudioPreampGain(getActivity(), MIC_MAIN_SUFFIX));
        AudioPreampGain.updateSummary(mAudioInputGain, Integer.parseInt(mAudioInputGain.getValue()));

        mAudioInputGain = (ListPreference) findPreference(KEY_AUDIO_PREAMP_GAIN + "_" + MIC_SUB_SUFFIX);
        //mAudioInputGain.setEnabled(mAudioInputGain.isSupported());
        mAudioInputGain.setOnPreferenceChangeListener(new AudioPreampGain(getActivity(), MIC_SUB_SUFFIX));
        AudioPreampGain.updateSummary(mAudioInputGain, Integer.parseInt(mAudioInputGain.getValue()));

    }

    public static void restore(Context context) {
        AudioOutputGain.restore(context);
        AudioPreampGain.restore(context, MIC_MAIN_SUFFIX);
        AudioPreampGain.restore(context, MIC_SUB_SUFFIX);
    }


}

