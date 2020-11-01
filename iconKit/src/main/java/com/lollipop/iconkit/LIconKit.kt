package com.lollipop.iconkit

import android.content.Context
import com.lollipop.iconcore.ui.IconHelper
import com.lollipop.iconcore.util.ExternalLinkManager
import com.lollipop.iconcore.util.MakerInfoManager
import com.lollipop.iconcore.util.UpdateInfoManager

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

    fun createUpdateInfoProvider(context: Context): UpdateInfoManager.UpdateInfoProvider? {
        return iconMapCreator?.createUpdateInfoProvider(context)
    }

    fun createLinkInfoProvider(context: Context): ExternalLinkManager.ExternalLinkProvider? {
        return iconMapCreator?.createLinkInfoProvider(context)
    }

    fun createMakerInfoProvider(context: Context): MakerInfoManager.MakerInfoProvider? {
        return iconMapCreator?.createMakerInfoProvider(context)
    }

    interface IconMapCreator {
        fun createHomePageMap(context: Context): IconHelper.DrawableMap
        fun createAppsPageMap(context: Context): IconHelper.DrawableMap
        fun createRequestPageMap(context: Context): IconHelper.DrawableMap
        fun createUpdateInfoProvider(context: Context): UpdateInfoManager.UpdateInfoProvider?
        fun createLinkInfoProvider(context: Context): ExternalLinkManager.ExternalLinkProvider?
        fun createMakerInfoProvider(context: Context): MakerInfoManager.MakerInfoProvider?
    }

    fun createDefXmlMapFromResource(context: Context, resId: Int): IconHelper.DrawableMap {
        return IconHelper.DefaultXmlMap.readFromResource(context, resId)
    }

    fun createDefXmlMapFromAssets(context: Context, fileName: String): IconHelper.DrawableMap {
        return IconHelper.DefaultXmlMap.readFromAssets(context, fileName)
    }

    fun readUpdateInfoByXml(context: Context, resId: Int): UpdateInfoManager.UpdateInfoProvider {
        return UpdateInfoManager.DefXmlInfoProvider.readFromResource(context, resId)
    }

    fun readUpdateInfoByJson(json: String): UpdateInfoManager.UpdateInfoProvider {
        return UpdateInfoManager.DefJsonInfoProvider(json)
    }

    fun readLinkInfoByXml(context: Context, resId: Int): ExternalLinkManager.ExternalLinkProvider {
        return ExternalLinkManager.readFromResource(context, resId)
    }

    fun readLinkInfoByXmlFromAssets(context: Context, name: String): ExternalLinkManager.ExternalLinkProvider {
        return ExternalLinkManager.readFromAssets(context, name)
    }

}