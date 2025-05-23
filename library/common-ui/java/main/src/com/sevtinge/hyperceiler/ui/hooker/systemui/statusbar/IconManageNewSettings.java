/*
  * This file is part of HyperCeiler.

  * HyperCeiler is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License.

  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.

  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <https://www.gnu.org/licenses/>.

  * Copyright (C) 2023-2025 HyperCeiler Contributions
*/
package com.sevtinge.hyperceiler.ui.hooker.systemui.statusbar;

import static com.sevtinge.hyperceiler.hook.utils.devicesdk.SystemSDKKt.isMoreSmallVersion;

import androidx.preference.SwitchPreference;

import com.sevtinge.hyperceiler.ui.R;
import com.sevtinge.hyperceiler.dashboard.DashboardFragment;
import com.sevtinge.hyperceiler.hook.utils.prefs.PrefsUtils;

import fan.preference.DropDownPreference;
import fan.preference.SeekBarPreferenceCompat;

public class IconManageNewSettings extends DashboardFragment {
    DropDownPreference mNewHD;
    DropDownPreference mSmallHD;
    DropDownPreference mBigHD;

    DropDownPreference mAlarmClockIcon;
    SeekBarPreferenceCompat mAlarmClockIconN;
    SeekBarPreferenceCompat mNotificationIconMaximum;
    SwitchPreference mBatteryNumber;
    SwitchPreference mBatteryPercentage;

    @Override
    public int getPreferenceScreenResId() {
        return R.xml.system_ui_status_bar_icon_manage_new;
    }

    @Override
    public void initPrefs() {
        mSmallHD = findPreference("prefs_key_system_ui_status_bar_icon_small_hd");
        mBigHD = findPreference("prefs_key_system_ui_status_bar_icon_big_hd");
        mNewHD = findPreference("prefs_key_system_ui_status_bar_icon_new_hd");

        mAlarmClockIcon = findPreference("prefs_key_system_ui_status_bar_icon_alarm_clock");
        mAlarmClockIconN = findPreference("prefs_key_system_ui_status_bar_icon_alarm_clock_n");
        mNotificationIconMaximum = findPreference("prefs_key_system_ui_status_bar_notification_icon_maximum");

        mBatteryNumber = findPreference("prefs_key_system_ui_status_bar_battery_percent");
        mBatteryPercentage = findPreference("prefs_key_system_ui_status_bar_battery_percent_mark");

        mAlarmClockIconN.setVisible(Integer.parseInt(PrefsUtils.mSharedPreferences.getString("prefs_key_system_ui_status_bar_icon_alarm_clock", "0")) == 3);

        if (isMoreSmallVersion(200, 2f)) {
            mSmallHD.setVisible(false);
            mBigHD.setVisible(false);
            mNewHD.setVisible(false);
        }

        mAlarmClockIcon.setOnPreferenceChangeListener((preference, o) -> {
            mAlarmClockIconN.setVisible(Integer.parseInt((String) o) == 3);
            return true;
        });

        mNotificationIconMaximum.setOnPreferenceChangeListener((preference, o) -> {
            if ((int) o == 16) {
                mNotificationIconMaximum.setValue(R.string.unlimited);
            }
            return true;
        });

        mBatteryNumber.setOnPreferenceChangeListener((preference, o) -> {
            if (!(boolean) o) {
                mBatteryPercentage.setChecked(false);
            }
            return true;
        });
    }
}
