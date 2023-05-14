package com.sevtinge.cemiuiler.module.home.widget

import android.view.View
import com.github.kyuubiran.ezxhelper.ClassUtils.loadClass
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHook
import com.github.kyuubiran.ezxhelper.finders.MethodFinder.`-Static`.methodFinder
import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.hookAfter
import com.github.kyuubiran.ezxhelper.utils.invokeMethodAuto
import com.github.kyuubiran.ezxhelper.utils.loadClass
import com.sevtinge.cemiuiler.module.base.BaseHook
import com.sevtinge.cemiuiler.utils.hookAfterMethod
import java.util.function.Predicate

object HideWidgetTitles : BaseHook() {
    override fun init() {

        val maMlWidgetInfo = loadClass("com.miui.home.launcher.maml.MaMlWidgetInfo")
        loadClass("com.miui.home.launcher.LauncherAppWidgetHost").methodFinder().first {
            name == "createLauncherWidgetView" && parameterCount == 4
        }.createHook {
            after {
                val view = it.result as Any
                view.invokeMethodAuto("getTitleView")?.invokeMethodAuto("setVisibility", View.GONE)
            }
        }
        "com.miui.home.launcher.Launcher".hookAfterMethod(
            "addMaMl", maMlWidgetInfo, Boolean::class.java, Predicate::class.java
        ) {
            val view = it.result as Any
            view.invokeMethodAuto("getTitleView")?.invokeMethodAuto("setVisibility", View.GONE)
        }

    }
}