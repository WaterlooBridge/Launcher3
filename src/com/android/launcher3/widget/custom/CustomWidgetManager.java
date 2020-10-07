/*
 * Copyright (C) 2019 The Android Open Source Project
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

package com.android.launcher3.widget.custom;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.launcher3.LauncherAppWidgetInfo;
import com.android.launcher3.LauncherAppWidgetProviderInfo;
import com.android.launcher3.util.MainThreadInitializedObject;
import com.android.launcher3.util.PackageUserKey;
import com.android.launcher3.widget.LauncherAppWidgetHostView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * CustomWidgetManager handles custom widgets implemented as a plugin.
 */
public class CustomWidgetManager {

    public static final MainThreadInitializedObject<CustomWidgetManager> INSTANCE =
            new MainThreadInitializedObject<>(CustomWidgetManager::new);

    /**
     * auto provider Id is an ever-increasing number that serves as the providerId whenever a new
     * custom widget has been connected.
     */
    private int mAutoProviderId = 0;
    private final SparseArray<WeakReference<Context>> mContexts;
    private final List<CustomAppWidgetProviderInfo> mCustomWidgets;
    private final SparseArray<ComponentName> mWidgetsIdMap;
    private Consumer<PackageUserKey> mWidgetRefreshCallback;

    private CustomWidgetManager(Context context) {
        mContexts = new SparseArray<>();
        mCustomWidgets = new ArrayList<>();
        mWidgetsIdMap = new SparseArray<>();
    }

    /**
     * Inject a callback function to refresh the widgets.
     */
    public void setWidgetRefreshCallback(Consumer<PackageUserKey> cb) {
        mWidgetRefreshCallback = cb;
    }

    /**
     * Callback method to inform a plugin it's corresponding widget has been created.
     */
    public void onViewCreated(LauncherAppWidgetHostView view) {
    }

    /**
     * Returns the list of custom widgets.
     */
    @NonNull
    public List<CustomAppWidgetProviderInfo> getCustomWidgets() {
        return mCustomWidgets;
    }

    /**
     * Returns the widget id for a specific provider.
     */
    public int getWidgetIdForCustomProvider(@NonNull ComponentName provider) {
        int index = mWidgetsIdMap.indexOfValue(provider);
        if (index >= 0) {
            return LauncherAppWidgetInfo.CUSTOM_WIDGET_ID - mWidgetsIdMap.keyAt(index);
        } else {
            return AppWidgetManager.INVALID_APPWIDGET_ID;
        }
    }

    /**
     * Returns the widget provider in respect to given widget id.
     */
    @Nullable
    public LauncherAppWidgetProviderInfo getWidgetProvider(int widgetId) {
        ComponentName cn = mWidgetsIdMap.get(LauncherAppWidgetInfo.CUSTOM_WIDGET_ID - widgetId);
        for (LauncherAppWidgetProviderInfo info : mCustomWidgets) {
            if (info.provider.equals(cn)) return info;
        }
        return null;
    }
}
