package com.lollipop.iconkit

import android.content.Context
import com.lollipop.iconcore.ui.IconHelper

/**
 * @author lollipop
 * @date 10/25/20 21:14
 */
object LIconKit {

    private var iconMapCreator: IconMapCreator? = null

    fun init(mapCreator: IconMapCreator) {
        iconMapCreator = mapCreator
    }

    fun createHomePageMap(context: Context): IconHelper.DrawableMap? {
        return iconMapCreator?.createHomePageMap(context)
    }

    fun createAppsPageMap(context: Context): IconHelper.DrawableMap? {
        return iconMapCreator?.createAppsPageMap(context)
    }

    fun createRequestPageMap(context: Context): IconHelper.DrawableMap? {
        return iconMapCreator?.createRequestPageMap(context)
    }

    interface IconMapCreator {
        fun createHomePageMap(context: Context): IconHelper.DrawableMap
        fun createAppsPageMap(context: Context): IconHelper.DrawableMap
        fun createRequestPageMap(context: Context): IconHelper.DrawableMap
    }

    fun createDefXmlMapFromResource(context: Context, resId: Int): IconHelper.DrawableMap {
        return IconHelper.DefaultXmlMap.readFromResource(context, resId)
    }

    fun createDefXmlMapFromAssets(context: Context, fileName: String): IconHelper.DrawableMap {
        return IconHelper.DefaultXmlMap.readFromAssets(context, fileName)
    }

}