package com.lollipop.iconcore

import android.content.Context
import com.lollipop.iconcore.provider.MainPageProvider
import com.lollipop.iconcore.throwable.IconPackException
import com.lollipop.iconcore.ui.IconApplication

/**
 * @author lollipop
 * @date 10/22/20 01:34
 * 图标包核心库入口类
 */
object IconPackCore {

    private var isInit = false

    fun init(context: Context, provider: MainPageProvider) {
        if (isInit) {
            return
        }
        val applicationContext = context.applicationContext
        if (applicationContext !is IconApplication) {
            throw IconPackException("Application is not IconApplication, nor is it a child of IconApplication")
        }
        applicationContext.bindMainPageProvider(provider)
        isInit = true
    }

}