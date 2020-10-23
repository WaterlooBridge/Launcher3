/*
 * Copyright (C) 2018 The Android Open Source Project
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
package com.android.launcher3.settings;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.util.ArraySet;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.android.launcher3.config.FeatureFlags;
import com.android.launcher3.config.FlagTogglerPrefUi;

/**
 * Dev-build only UI allowing developers to toggle flag settings and plugins.
 * See {@link FeatureFlags}.
 */
@TargetApi(Build.VERSION_CODES.O)
public class DeveloperOptionsFragment extends PreferenceFragmentCompat {

    private static final String ACTION_PLUGIN_SETTINGS = "com.android.systemui.action.PLUGIN_SETTINGS";
    private static final String PLUGIN_PERMISSION = "com.android.systemui.permission.PLUGIN";

    private PreferenceScreen mPreferenceScreen;

    private FlagTogglerPrefUi mFlagTogglerPrefUi;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        mPreferenceScreen = getPreferenceManager().createPreferenceScreen(getContext());
        setPreferenceScreen(mPreferenceScreen);

        initFlags();
    }

    private PreferenceCategory newCategory(String title) {
        PreferenceCategory category = new PreferenceCategory(getContext());
        category.setOrder(Preference.DEFAULT_ORDER);
        category.setTitle(title);
        mPreferenceScreen.addPreference(category);
        return category;
    }

    private void initFlags() {
        if (!FeatureFlags.showFlagTogglerUi(getContext())) {
            return;
        }

        mFlagTogglerPrefUi = new FlagTogglerPrefUi(this);
        mFlagTogglerPrefUi.applyTo(newCategory("Feature flags"));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mFlagTogglerPrefUi != null) {
            mFlagTogglerPrefUi.onCreateOptionsMenu(menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mFlagTogglerPrefUi != null) {
            mFlagTogglerPrefUi.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStop() {
        if (mFlagTogglerPrefUi != null) {
            mFlagTogglerPrefUi.onStop();
        }
        super.onStop();
    }

    private String toString(ArraySet<String> plugins) {
        StringBuilder b = new StringBuilder();
        for (String string : plugins) {
            if (b.length() != 0) {
                b.append(", ");
            }
            b.append(string);
        }
        return b.toString();
    }

    private String toName(String action) {
        String str = action.replace("com.android.systemui.action.PLUGIN_", "");
        StringBuilder b = new StringBuilder();
        for (String s : str.split("_")) {
            if (b.length() != 0) {
                b.append(' ');
            }
            b.append(s.substring(0, 1));
            b.append(s.substring(1).toLowerCase());
        }
        return b.toString();
    }
}
