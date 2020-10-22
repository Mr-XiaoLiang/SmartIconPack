package com.lollipop.iconcore.ui

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.content.res.XmlResourceParser
import android.graphics.drawable.Drawable
import android.widget.ImageView
import org.xmlpull.v1.XmlPullParser
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * @author lollipop
 * @date 10/22/20 02:32
 */
class IconHelper(private val context: Context, private val customizeMap: DrawableMap? = null) {

    companion object {
        private val EMPTY_ICON_ID = IntArray(0)
        private val EMPTY_ICON = IconInfo("", "", 0)
        fun findDrawableId(context: Context, name: String): Int {
            return context.resources.getIdentifier(
                name, "drawable", context.packageName)
        }
    }

    private val notSupportList = ArrayList<AppInfo>()
    private val supportedList = ArrayList<AppInfo>()

    val allAppCount: Int
        get() {
            return supportedList.size + notSupportList.size
        }

    val notSupportCount: Int
        get() {
            return notSupportList.size
        }

    val supportedCount: Int
        get() {
            return supportedList.size
        }

    val iconCount: Int
        get() {
            return customizeMap?.iconCount?:supportedCount
        }

    fun getAppInfo(index: Int): AppInfo {
        if (index < supportedCount) {
            return getSupportedInfo(index)
        }
        return getNotSupportInfo(index - supportedCount)
    }

    fun getNotSupportInfo(index: Int): AppInfo {
        return notSupportList[index]
    }

    fun getSupportedInfo(index: Int): AppInfo {
        return supportedList[index]
    }

    fun getIcon(index: Int): IconInfo {
        return customizeMap?.get(index)?: EMPTY_ICON
    }

    fun loadAppInfo() {
        supportedList.clear()
        notSupportList.clear()
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        val appList = pm.queryIntentActivities(mainIntent,0)
        // 调用系统排序 ， 根据name排序
        Collections.sort(appList, ResolveInfo.DisplayNameComparator(pm))
        for (appInfo in appList){
            val pkgName = appInfo.activityInfo.packageName
            val iconPack = getIconByPkg(pkgName)
            val app = AppInfo(
                appInfo.loadLabel(pm), pkgName,
                appInfo.loadIcon(pm), iconPack)
            if (iconPack.isEmpty()) {
                notSupportList.add(app)
            } else {
                supportedList.add(app)
            }
        }
    }

    fun load(imageView: ImageView, icon: IconInfo, def: Int = 0) {
        if (icon.resId == 0) {
            if (def != 0) {
                imageView.setImageResource(def)
            } else {
                imageView.setImageDrawable(null)
            }
            return
        }
        imageView.setImageResource(icon.resId)
    }

    fun load(imageView: ImageView, icon: AppInfo, iconIndex: Int = 0) {
        if (icon.iconPack.isEmpty()) {
            imageView.setImageDrawable(icon.srcIcon)
            return
        }
        imageView.setImageResource(icon.iconPack[iconIndex])
    }

    private fun getIconByPkg(pkg: String): IntArray {
        val customize = customizeMap
        if (customize != null) {
            return customize.getDrawableName(pkg)
        }
        val drawableName = pkg.replace(".", "_")
        val identifier = findDrawableId(context, drawableName)
        if (identifier == 0) {
            return EMPTY_ICON_ID
        }
        return intArrayOf(identifier)
    }

    /**
     * 自定义Drawable获取对象
     * 可以根据自己的定义来获取图标
     */
    interface DrawableMap {
        /**
         * 根据包名
         */
        fun getDrawableName(packageName: String): IntArray

        /**
         * 图标数量
         */
        val iconCount: Int

        /**
         * 返回一个图标对象
         */
        operator fun get(index: Int): IconInfo
    }

    class IconInfo(val name: String, val pkg: String, val resId: Int)

    class AppInfo(val name: CharSequence, val pkg: String,
                       val srcIcon: Drawable, val iconPack: IntArray)

    class XmlIconMap(context: Context, resId: Int): DrawableMap {

        private val iconMap = HashMap<String, ArrayList<IconInfo>>()
        private val allIcon = ArrayList<IconInfo>()
        private val iconCache = HashMap<String, IntArray>()

        private val keys: Array<String> by lazy {
            iconMap.keys.toTypedArray()
        }

        companion object {
            private const val CATEGORY = "category"
            private const val ITEM = "item"
            const val CATEGORY_DEFAULT = "default"

            private const val ATTR_NAME = "name"
            private const val ATTR_PKG = "pkg"
            private const val ATTR_ICON = "icon"

            private val EMPTY_ICON = IconInfo("", "", 0)
        }

        init {
            decodeFromXml(context.resources.getXml(resId), context)
        }

        private fun decodeFromXml(xml: XmlResourceParser, context: Context) {
            var eventType = xml.eventType
            val defGroup = ArrayList<IconInfo>()
            iconMap[CATEGORY_DEFAULT] = defGroup
            var iconGroup = defGroup
            while (eventType != XmlPullParser.END_DOCUMENT) {
                val tagName = xml.name
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (CATEGORY == tagName) {
                            iconGroup = iconMap[tagName] ?:ArrayList()
                            iconMap[tagName] = iconGroup
                        } else if (ITEM == tagName) {
                            val name = xml.getAttributeValue(null, ATTR_NAME)
                            val pkg = xml.getAttributeValue(null, ATTR_PKG)
                            val icon = xml.getAttributeValue(null, ATTR_ICON)
                            val info = IconInfo(name, pkg, findDrawableId(context, icon))
                            iconGroup.add(info)
                            allIcon.add(info)
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (CATEGORY == tagName) {
                            iconGroup = defGroup
                        }
                    }
                }
                eventType = xml.next()
            }
            if (defGroup.isEmpty()) {
                iconMap.remove(CATEGORY_DEFAULT)
            }
        }

        override fun getDrawableName(packageName: String): IntArray {
            val cache = iconCache[packageName]
            if (cache != null) {
                return cache
            }
            val icons = ArrayList<IconInfo>()
            for (icon in allIcon) {
                if (icon.pkg == packageName) {
                    icons.add(icon)
                }
            }
            val idArray = IntArray(icons.size) { index -> icons[index].resId }
            iconCache[packageName] = idArray
            return idArray
        }

        override val iconCount: Int
            get() {
                return allIcon.size
            }

        override fun get(index: Int): IconInfo {
            return allIcon[index]
        }

        val categoryCount: Int
            get() {
                return keys.size
            }

        fun getCategory(index: Int): String {
            return keys[index]
        }

        fun getIcon(category: String, index: Int): IconInfo {
            return iconMap[category]?.get(index)?:EMPTY_ICON
        }

        fun iconCountByCategory(category: String): Int {
            return iconMap[category]?.size?:0
        }

    }

}