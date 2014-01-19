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
import android.content.SharedPreferences.Editor;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.util.Log;

public class AudioOutputGain extends DialogPreference implements OnClickListener {

    private static final int SEEKBAR_ID = R.id.audio_hpvol_seekbar;
    private static final int VALUE_DISPLAY_ID = R.id.audio_hpvol_value;

    private static final String FILE_HPVOL = "/sys/class/misc/voodoo_sound/twl6040_headphone_amplifier_level";

    private AudioHpvolSeekBar mSeekBar;

    private static final int MAX_VALUE = 30;
    private static final int DEFAULT_VALUE = 18;
    private static final int OFFSET_VALUE = 0;

    private static int sInstances = 0;

    private static Context mContext;

    public AudioOutputGain(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setDialogLayoutResource(R.layout.preference_dialog_audio_tuning);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        sInstances++;

        SeekBar seekBar = (SeekBar) view.findViewById(SEEKBAR_ID);
        TextView valueDisplay = (TextView) view.findViewById(VALUE_DISPLAY_ID);
        mSeekBar = new AudioHpvolSeekBar(seekBar, valueDisplay, FILE_HPVOL, OFFSET_VALUE, MAX_VALUE);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        Boolean restoreOnBoot = sharedPrefs.getBoolean(AudioFragment.KEY_AUDIO_HPVOL + "_boot", true);
        CheckBox bootCheckBox = (CheckBox) view.findViewById(R.id.cbAudioHpvolBoot);
        bootCheckBox.setChecked(restoreOnBoot); 

        SetupButtonClickListeners(view);
    }

    private void SetupButtonClickListeners(View view) {
            Button defaultButton = (Button) view.findViewById(R.id.btnAudioHpvolDefault);
            defaultButton.setOnClickListener(this);

            CheckBox bootCheckBox = (CheckBox) view.findViewById(R.id.cbAudioHpvolBoot);
            bootCheckBox.setOnClickListener(this);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        sInstances--;

        if (positiveResult) {
            mSeekBar.save();
        } else if (sInstances == 0) {
            mSeekBar.reset();
        }
    }

    public int getHpVolume() {
      String text = Utils.readOneLine(FILE_HPVOL);
      return Integer.parseInt(text);
    }

    public static void restore(Context context) {
        if (!isSupported()) {
            return;
        }

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        Boolean restoreOnBoot = sharedPrefs.getBoolean(AudioFragment.KEY_AUDIO_HPVOL + "_boot", true);

        if ( restoreOnBoot ) {
            Integer val = sharedPrefs.getInt(AudioFragment.KEY_AUDIO_HPVOL, DEFAULT_VALUE);
            Utils.writeValue(FILE_HPVOL, Integer.toString(val));
            //Log.i("LgeL9Parts", "Headphone amplifier gain restored: " + val);
        }
    }

    public static boolean isSupported() {
        return Utils.fileExists(FILE_HPVOL);
    }

    public static void updateSummary(Context context, Preference preference, int value) {
        String summary = context.getString(R.string.audio_hpvol_summary);
        preference.setSummary(String.format(summary, value-MAX_VALUE));
    }

    public void updateSummary(int value) {
        updateSummary(mContext, this, value);
    }

    public void updateSummary() {
        updateSummary(getHpVolume()); 
    }

    class AudioHpvolSeekBar implements SeekBar.OnSeekBarChangeListener {

        private String mFilePath;
        private int mOriginal;
        private SeekBar mSeekBar;
        private TextView mValueDisplay;
        private int iOffset;
        private int iMax;

        public AudioHpvolSeekBar(SeekBar seekBar, TextView valueDisplay, String filePath, Integer offsetValue, Integer maxValue) {

            int iValue;
            mSeekBar = seekBar;
            mValueDisplay = valueDisplay;
            mFilePath = filePath;
            iOffset = offsetValue;
            iMax = maxValue;

            SharedPreferences sharedPreferences = getSharedPreferences();

            if (Utils.fileExists(mFilePath)) {
                String sDefaultValue = Utils.readOneLine(mFilePath);
                iValue = Integer.valueOf(sDefaultValue);
            } else {
                iValue = iMax - iOffset;
            }
            mOriginal = iValue;

            mSeekBar.setMax(iMax);

            reset();
            mSeekBar.setOnSeekBarChangeListener(this);
        }

        public int getValue() {
            return mSeekBar.getProgress() - iOffset;          
        }

        public void reset() {
            int iValue;

            iValue = mOriginal + iOffset;
            mSeekBar.setProgress(iValue);
            updateValue(mOriginal);
        }

        public void save() {
            int iValue;

            iValue = mSeekBar.getProgress() - iOffset;
            Editor editor = getEditor();
            editor.putInt(AudioFragment.KEY_AUDIO_HPVOL, iValue);
            editor.commit();
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            int iValue;

            iValue = progress - iOffset;
            Utils.writeValue(mFilePath, String.valueOf(iValue));
            updateValue(iValue);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }

        private void updateValue(int progress) {
            mValueDisplay.setText(String.format("%d", progress-MAX_VALUE));
            updateSummary(progress);
        }

        public void setNewValue(int iValue) {
            mOriginal = iValue;
            reset();
        }
    }

    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btnAudioHpvolDefault:
                    setDefaultSettings();
                    break;
            case R.id.cbAudioHpvolBoot:
                    setOnBootSettings((CheckBox)v);
                    break;
        }
    }

    private void setDefaultSettings() {
        mSeekBar.setNewValue(DEFAULT_VALUE);
    }

    private void setOnBootSettings(CheckBox v) {
        Editor editor = getEditor();
        editor.putBoolean(AudioFragment.KEY_AUDIO_HPVOL + "_boot", v.isChecked());
        editor.commit();
    }
}
