package com.lollipop.smarticonpack

import android.content.Context
import com.lollipop.iconcore.ui.IconHelper
import com.lollipop.iconcore.util.ExternalLinkManager
import com.lollipop.iconcore.util.MakerInfoManager
import com.lollipop.iconcore.util.UpdateInfoManager
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

            override fun createUpdateInfoProvider(context: Context):
                    UpdateInfoManager.UpdateInfoProvider? {
                return LIconKit.readUpdateInfoByXml(context, R.xml.updates)
            }

            override fun createLinkInfoProvider(context: Context):
                    ExternalLinkManager.ExternalLinkProvider? {
                return LIconKit.readLinkInfoByXml(context, R.xml.links)
            }

            override fun createMakerInfoProvider(context: Context): MakerInfoManager.MakerInfoProvider? {
                return object: MakerInfoManager.MakerInfoProvider {
                    override val icon = R.drawable.pikachu
                    override val name = R.string.maker_name
                    override val signature = R.string.maker_sign
                    override val mottoArray = R.array.maker_motto
                    override val background = R.drawable.header
                }
            }
        })
    }

}