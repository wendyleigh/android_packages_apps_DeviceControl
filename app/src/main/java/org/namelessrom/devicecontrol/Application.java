/*
 *  Copyright (C) 2013 - 2014 Alexander "Evisceration" Martinz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.namelessrom.devicecontrol;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;
import android.os.Handler;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import org.namelessrom.devicecontrol.configuration.DeviceConfiguration;
import org.namelessrom.devicecontrol.utils.Utils;

import java.io.File;

import uk.co.senab.bitmapcache.BitmapLruCache;

// XXX: DO NOT USE ROOT HERE! NEVER!
public class Application extends android.app.Application {
    public static final Handler HANDLER = new Handler();

    private static final String sExternalStoragePath = String.format("%s/%s",
            Environment.getExternalStorageDirectory(), "DeviceControl");
    private static final File sExternalStorageDir = new File(sExternalStoragePath);

    private static Application sInstance;

    private BitmapLruCache mCache;

    private RefWatcher mRefWatcher;

    public static Application get() {
        return Application.sInstance;
    }

    public static Application getApplication(Context context) {
        return (Application) context.getApplicationContext();
    }

    public static RefWatcher getRefWatcher(Context context) {
        return getApplication(context).mRefWatcher;
    }

    @Override public void onLowMemory() {
        super.onLowMemory();
        if (mCache != null) {
            mCache.trimMemory();
        }
    }

    @Override public void onCreate() {
        super.onCreate();
        mRefWatcher = LeakCanary.install(this);

        Application.sInstance = this;

        // force enable logger until we hit the user preference
        Logger.setEnabled(true);

        buildCache();

        DeviceConfiguration deviceConfiguration = DeviceConfiguration.get(this);

        if (deviceConfiguration.debugStrictMode) {
            Logger.setStrictModeEnabled(true);
        }

        Logger.setEnabled(deviceConfiguration.extensiveLogging);

        dumpInformation();

        boolean isNameless = Utils.isNameless(this);
        Logger.v(this, String.format("is nameless: %s", isNameless));

        if (isNameless) {
            ComponentName c = new ComponentName(getPackageName(), DummyLauncher.class.getName());
            Utils.toggleComponent(c, false);
            Logger.v(this, "Force toggled on launcher icon for backwards compatibility with n-2.0");
        }
    }

    private void buildCache() {
        final File cacheLocation;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            cacheLocation = new File(sExternalStorageDir, "bitmapCache");
        } else {
            cacheLocation = new File(getFilesDir(), "bitmapCache");
        }
        cacheLocation.mkdirs();

        BitmapLruCache.Builder builder = new BitmapLruCache.Builder(this);
        builder.setMemoryCacheEnabled(true).setMemoryCacheMaxSizeUsingHeapSize(0.25f);
        builder.setDiskCacheEnabled(true).setDiskCacheLocation(cacheLocation);

        mCache = builder.build();
    }

    private void dumpInformation() {
        if (!Logger.getEnabled()) {
            return;
        }

        final Resources res = getResources();
        final int gmsVersion = res.getInteger(R.integer.google_play_services_version);
        Logger.i(this, "Google Play Services -> %s", gmsVersion);
    }

    public BitmapLruCache getBitmapCache() {
        return mCache;
    }

    @SuppressLint("SdCardPath") public String getFilesDirectory() {
        final File tmp = getFilesDir();
        if (tmp != null && tmp.isDirectory()) {
            return tmp.getPath();
        } else {
            return "/data/data/" + Application.get().getPackageName();
        }
    }

    public int getColor(final int resId) {
        return getResources().getColor(resId);
    }

    public String[] getStringArray(final int resId) {
        return getResources().getStringArray(resId);
    }

}
