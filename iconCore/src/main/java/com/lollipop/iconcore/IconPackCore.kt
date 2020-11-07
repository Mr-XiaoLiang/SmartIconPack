package com.lollipop.iconcore

import android.content.Context
import com.lollipop.iconcore.provider.MainPageProvider
import com.lollipop.iconcore.throwable.IconPackException
import com.lollipop.iconcore.ui.IconApplication
import com.lollipop.iconcore.util.AppInfoCore
import com.lollipop.iconcore.util.CrashHandler
import java.io.File

/**
 * @author lollipop
 * @date 10/22/20 01:34
 * 图标包核心库入口类
 */
class IconPackCore(private val context: Context) {

    companion object {
        private var isInit = false
        /**
         * Core核心类的初始化方法，
         * 如果没有调用他，那么可能无法成功展示页面，
         * 建议继承{@link com.lollipop.iconcore.ui.IconApplication}
         * 并在onCreate中初始化
         */
        fun init(context: Context,
                 provider: MainPageProvider = (context as MainPageProvider)): IconPackCore {
            val applicationContext = context.applicationContext
            if (applicationContext !is IconApplication) {
                throw IconPackException(
                    "Application is not IconApplication, nor is it a child of IconApplication")
            }
            val iconPackCore = IconPackCore(applicationContext)
            if (isInit) {
                return iconPackCore
            }
            applicationContext.bindMainPageProvider(provider)
            AppInfoCore.init(context, null)
            isInit = true
            return iconPackCore
        }
    }

    fun initCrashHandler(onAsync: Boolean = true,
                         uploadCallback: ((File) -> Boolean)?): IconPackCore {
        CrashHandler.init(context, onAsync, uploadCallback)
        return this
    }

}