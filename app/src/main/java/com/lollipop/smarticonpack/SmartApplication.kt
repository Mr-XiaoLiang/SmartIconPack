package com.lollipop.smarticonpack

import android.content.Context
import com.lollipop.iconcore.ui.IconHelper
import com.lollipop.iconkit.LApplication
import com.lollipop.iconkit.LIconKit

/**
 * @author lollipop
 * @date 10/25/20 21:23
 * 图标包对应的解析文件提供器
 */
class SmartApplication: LApplication() {

    override fun onCreate() {
        super.onCreate()
        LIconKit.init(object : LIconKit.IconMapCreator{
            override fun createHomePageMap(context: Context): IconHelper.DrawableMap {
                return LIconKit.createDefXmlMapFromResource(context, R.xml.appfilter)
            }

            override fun createAppsPageMap(context: Context): IconHelper.DrawableMap {
                return LIconKit.createDefXmlMapFromResource(context, R.xml.drawable)
            }

            override fun createRequestPageMap(context: Context): IconHelper.DrawableMap {
                return LIconKit.createDefXmlMapFromResource(context, R.xml.drawable)
            }
        })
    }

}