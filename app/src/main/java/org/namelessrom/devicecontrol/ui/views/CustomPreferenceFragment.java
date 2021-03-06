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
package org.namelessrom.devicecontrol.ui.views;

import android.content.Context;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.support.v4.preference.PreferenceFragment;

import com.squareup.leakcanary.RefWatcher;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.ui.preferences.AwesomeEditTextPreference;
import org.namelessrom.devicecontrol.ui.preferences.AwesomeTogglePreference;
import org.namelessrom.devicecontrol.ui.preferences.CustomPreference;

public abstract class CustomPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        if (preference instanceof AwesomeTogglePreference) {
            ((AwesomeTogglePreference) preference).writeValue((Boolean) o);
            return true;
        } else if (preference instanceof AwesomeEditTextPreference) {
            ((AwesomeEditTextPreference) preference).writeValue(String.valueOf(o));
            return true;
        }

        return false;
    }

    @Override public void onDestroy() {
        RefWatcher refWatcher = Application.getRefWatcher(getActivity());
        refWatcher.watch(this);
        super.onDestroy();
    }

    public void removeIfEmpty(final PreferenceScreen root, final PreferenceGroup preferenceGroup) {
        if (root != null && preferenceGroup.getPreferenceCount() == 0) {
            root.removePreference(preferenceGroup);
        }
    }

    protected void isSupported(final PreferenceScreen preferenceScreen, final Context context) {
        isSupported(preferenceScreen, context, R.string.no_tweaks_message);
    }

    protected void isSupported(final PreferenceScreen preferenceScreen, final Context context,
            final int sId) {
        if (preferenceScreen.getPreferenceCount() == 0) {
            preferenceScreen.addPreference(createPreference(context, sId));
        }
    }

    protected void isSupported(final PreferenceCategory preferenceCategory, final Context context) {
        isSupported(preferenceCategory, context, R.string.no_tweaks_message);
    }

    protected void isSupported(final PreferenceCategory preferenceCategory, final Context context,
            final int sId) {
        if (preferenceCategory.getPreferenceCount() == 0) {
            preferenceCategory.addPreference(createPreference(context, sId));
        }
    }

    private CustomPreference createPreference(final Context context, final int sId) {
        final CustomPreference pref = new CustomPreference(context);
        pref.setTitle(R.string.no_tweaks_available);
        pref.setSummary(sId);
        return pref;
    }

}
