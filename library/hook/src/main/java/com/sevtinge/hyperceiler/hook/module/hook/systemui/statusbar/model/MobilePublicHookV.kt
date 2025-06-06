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
package com.sevtinge.hyperceiler.hook.module.hook.systemui.statusbar.model

import android.telephony.SubscriptionManager
import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import com.sevtinge.hyperceiler.hook.module.hook.systemui.base.api.Dependency
import com.sevtinge.hyperceiler.hook.module.hook.systemui.base.api.MiuiStub
import com.sevtinge.hyperceiler.hook.module.hook.systemui.base.statusbar.icon.MobileClass.miuiCellularIconVM
import com.sevtinge.hyperceiler.hook.module.hook.systemui.base.statusbar.icon.MobilePrefs.card1
import com.sevtinge.hyperceiler.hook.module.hook.systemui.base.statusbar.icon.MobilePrefs.card2
import com.sevtinge.hyperceiler.hook.module.hook.systemui.base.statusbar.icon.MobilePrefs.hideIndicator
import com.sevtinge.hyperceiler.hook.module.hook.systemui.base.statusbar.icon.MobilePrefs.hideRoaming
import com.sevtinge.hyperceiler.hook.module.hook.systemui.base.statusbar.icon.MobilePrefs.isEnableDouble
import com.sevtinge.hyperceiler.hook.utils.MethodHookParam
import com.sevtinge.hyperceiler.hook.utils.StateFlowHelper.newReadonlyStateFlow
import com.sevtinge.hyperceiler.hook.utils.StateFlowHelper.setStateFlowValue
import com.sevtinge.hyperceiler.hook.utils.callMethod
import com.sevtinge.hyperceiler.hook.utils.devicesdk.isMoreSmallVersion
import com.sevtinge.hyperceiler.hook.utils.getObjectField
import com.sevtinge.hyperceiler.hook.utils.getObjectFieldAs
import com.sevtinge.hyperceiler.hook.utils.setObjectField
import java.util.function.Consumer

class MobilePublicHookV : BaseHook() {
    override fun init() {
        // findClass(
        //     "com.android.systemui.statusbar.connectivity.NetworkControllerImpl"
        // ).setStaticObjectField("DEBUG", true)

        hookAllConstructors(miuiCellularIconVM, object : MethodHook() {
            override fun after(param: MethodHookParam) {
                val cellularIcon = param.thisObject
                val mobileIconInteractor = param.args[2]
                val subId = mobileIconInteractor.getObjectFieldAs<Int>("subId")

                // 双排信号
                if (isEnableDouble) {
                    val isVisible = newReadonlyStateFlow(false)
                    cellularIcon.setObjectField("isVisible", isVisible)
                    if (!hideRoaming) {
                        cellularIcon.setObjectField("smallRoamVisible", newReadonlyStateFlow(false))
                    }

                    Dependency.miuiLegacyDependency
                        ?.getObjectField("mOperatorCustomizedPolicy")
                        ?.callMethod("get")
                        ?.getObjectField("mobileIcons")
                        ?.getObjectField("activeMobileDataSubscriptionId")
                        ?.let { activeSubId ->
                            MiuiStub.javaAdapter.alwaysCollectFlow(
                                activeSubId,
                                Consumer<Int> {
                                    setStateFlowValue(isVisible, subId == it)
                                }
                            )
                        }
                } else {
                    val getSlotIndex = SubscriptionManager.getSlotIndex(subId)
                    if ((card1 && getSlotIndex == 0) || (card2 && getSlotIndex == 1)) {
                        cellularIcon.setObjectField("isVisible", newReadonlyStateFlow(false))
                    }
                }

                if (hideIndicator) {
                    cellularIcon.setObjectField("inOutVisible", newReadonlyStateFlow(false))
                }
                if (hideRoaming) {
                    cellularIcon.setObjectField("smallRoamVisible", newReadonlyStateFlow(false))
                    cellularIcon.setObjectField("mobileRoamVisible", newReadonlyStateFlow(false))
                }
                // 隐藏 hd
                if (!isMoreSmallVersion(200, 2f)) {
                    updateIconState(param, "smallHdVisible", "system_ui_status_bar_icon_small_hd")
                    updateIconState(param, "volteVisibleCn", "system_ui_status_bar_icon_big_hd")
                    updateIconState(param, "volteVisibleGlobal", "system_ui_status_bar_icon_big_hd")
                }
            }
        })
    }

    private fun updateIconState(param: MethodHookParam, fieldName: String, key: String) {
        val opt = mPrefsMap.getStringAsInt(key, 0)
        if (opt != 0) {
            val value = when (opt) {
                1 -> true
                else -> false
            }
            param.thisObject.setObjectField(fieldName, newReadonlyStateFlow(value))
        }
    }
}
