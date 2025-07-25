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
package com.sevtinge.hyperceiler.hook.module.hook.mediaeditor

import com.sevtinge.hyperceiler.hook.module.base.BaseHook
import com.sevtinge.hyperceiler.hook.module.base.dexkit.DexKit
import io.github.kyuubiran.ezxhelper.core.finder.MethodFinder.`-Static`.methodFinder
import io.github.kyuubiran.ezxhelper.xposed.dsl.HookFactory.`-Static`.createHook
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier

object UnlockDisney : BaseHook() {
    private val mickey by lazy<Method> {
        DexKit.findMember("UnlockDisneyMickey") {
            it.findMethod {
                matcher {
                    addCaller {
                        usingStrings("magic_recycler_matting_0", "magic_recycler_clear_icon")
                        // returnType = "java.util.List" // 你米 1.6.5.10.2 改成了 java.util.ArrayList，所以找不到
                        paramCount = 0
                    }
                    modifiers = Modifier.STATIC
                    returnType = "boolean"
                    paramCount = 0
                }
            }.single()
        }
    }

    private val bear by lazy<Method> {
        DexKit.findMember("UnlockDisneyBear") {
            it.findMethod {
                matcher {
                    declaredClass = mickey.declaringClass.name
                    modifiers = Modifier.STATIC
                    returnType = "boolean"
                    paramCount = 0
                }
            }.last()
        }
    }

    private val princess by lazy<Field> {
        // 2.0 改成了内联，只能拿字段去解析了
        // .field public static final n:Lak/a$i;
        // 里面的 method public final c(Ljava/lang/Object;)Ljava/lang/Object; 返回 true 即可解锁
        DexKit.findMember("UnlockDisneyPrincess") {
            it.findField {
                matcher {
                    declaredClass = mickey.declaringClass.name
                    modifiers = Modifier.STATIC or Modifier.FINAL
                }
            }.last()
        }
    }

    private val isHookType by lazy {
        mPrefsMap.getStringAsInt("mediaeditor_hook_type", 0)
    }
    private val isType by lazy {
        mPrefsMap.getStringAsInt("mediaeditor_unlock_disney_some_func", 0)
    }

    private val isMickey by lazy {
        mPrefsMap.getBoolean("mediaeditor_unlock_mickey_some_func")
    }
    private val isBear by lazy {
        mPrefsMap.getBoolean("mediaeditor_unlock_bear_some_func")
    }
    private val isPrincess by lazy {
        mPrefsMap.getBoolean("mediaeditor_unlock_princess_some_func")
    }

    override fun init() {
        if (isHookType == 1) {
            when (isType) {
                1 -> {
                    isHook(mickey, true)
                    isHook(bear, false)
                    isHook(
                        princess.type.methodFinder().filterByReturnType(Object::class.java).first(),
                        false
                    )
                }

                2 -> {
                    isHook(mickey, false)
                    isHook(bear, true)
                    isHook(
                        princess.type.methodFinder().filterByReturnType(Object::class.java).first(),
                        false
                    )
                }

                3 -> {
                    isHook(mickey, false)
                    isHook(bear, false)
                    isHook(
                        princess.type.methodFinder().filterByReturnType(Object::class.java).first(),
                        true
                    )
                }
            }
        } else if (isHookType == 2) {
            isHook(mickey, isMickey)
            isHook(bear, isBear)
            isHook(
                princess.type.methodFinder().filterByReturnType(Object::class.java).first(),
                isPrincess
            )
        }
    }

    private fun isHook(method: Method, bool: Boolean) {
        method.createHook {
            returnConstant(bool)
        }
    }
}
