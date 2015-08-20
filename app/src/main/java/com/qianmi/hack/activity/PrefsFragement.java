package com.qianmi.hack.activity;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.qianmi.hack.R;

/**
 * Created by wv on 2015/8/20.
 */
public class PrefsFragement extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);
    }
}
