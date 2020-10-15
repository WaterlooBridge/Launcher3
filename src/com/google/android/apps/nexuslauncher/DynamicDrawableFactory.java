package com.google.android.apps.nexuslauncher;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Process;
import android.os.UserHandle;

import com.android.launcher3.FastBitmapDrawable;
import com.android.launcher3.ItemInfoWithIcon;
import com.android.launcher3.Utilities;
import com.android.launcher3.graphics.DrawableFactory;
import com.android.launcher3.icons.BitmapInfo;
import com.google.android.apps.nexuslauncher.clock.DynamicClock;

public class DynamicDrawableFactory extends DrawableFactory {
    private final DynamicClock mDynamicClockDrawer;

    public DynamicDrawableFactory(Context context) {
        mDynamicClockDrawer = new DynamicClock(context);
    }

    @Override
    public FastBitmapDrawable newIcon(Context context, ItemInfoWithIcon info) {
        if (info == null || info.itemType != 0 ||
                !DynamicClock.DESK_CLOCK.equals(info.getTargetComponent()) ||
                !info.user.equals(Process.myUserHandle())) {
            return super.newIcon(context, info);
        }
        FastBitmapDrawable dVar = mDynamicClockDrawer.drawIcon(info.iconBitmap);
        dVar.setIsDisabled(info.isDisabled());
        return dVar;
    }

    @Override
    public FastBitmapDrawable newIcon(Context context, BitmapInfo info, ActivityInfo target) {
        if (DynamicClock.DESK_CLOCK.getPackageName().equals(target.packageName) &&
                (!Utilities.ATLEAST_OREO || UserHandle.getUserHandleForUid(target.applicationInfo.uid).equals(Process.myUserHandle()))) {
            return mDynamicClockDrawer.drawIcon(info.icon);
        }
        return super.newIcon(context, info, target);
    }
}
