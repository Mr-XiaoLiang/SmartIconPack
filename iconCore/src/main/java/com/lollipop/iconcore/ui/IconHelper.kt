package com.lollipop.iconcore.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.Closeable
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * @author lollipop
 * @date 10/22/20 02:32
 * 图标计算辅助类
 */
class IconHelper(private val customizeMap: DrawableMapProvider? = null) {

    companion object {
        private val EMPTY_ICON_ID = IntArray(0)
        private val EMPTY_COMPONENT = ComponentName("", "")
        private val EMPTY_ICON = IconInfo("", EMPTY_COMPONENT, 0)
        fun findDrawableId(context: Context, name: String): Int {
            return context.resources.getIdentifier(
                    name, "drawable", context.packageName)
        }

        fun parseComponent(info: String): ComponentName {
            if (!info.startsWith("ComponentInfo")) {
                return EMPTY_COMPONENT
            }
            val start = info.indexOf("{") + 1
            val end = info.indexOf("}")
            if (start > end || start == end) {
                return EMPTY_COMPONENT
            }
            val infoContent = info.substring(start, end)
            val split = infoContent.split("/")
            if (split.size < 2) {
                return EMPTY_COMPONENT
            }
            val pkg = split[0]
            val cls = split[1]
            val fullName = if (cls[0] == '.') { pkg + cls } else { cls }
            return ComponentName(pkg, fullName)
        }

        private fun activityFullName(pkg: String, cls: String): String {
            return if (cls[0] == '.') { pkg + cls } else { cls }
        }

        fun String.fullName(pkg: String): String {
            return activityFullName(pkg, this)
        }

        fun newHelper(creator: (context: Context) -> DrawableMap?): IconHelper {
            return IconHelper(DrawableMapProvider(creator))
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

    fun loadAppInfo(context: Context) {
        supportedList.clear()
        notSupportList.clear()
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        val appList = pm.queryIntentActivities(mainIntent, 0)
        // 调用系统排序 ， 根据name排序
        Collections.sort(appList, ResolveInfo.DisplayNameComparator(pm))
        for (appInfo in appList){
            val pkgName = appInfo.activityInfo.packageName
            val clsName = appInfo.activityInfo.name
            val iconPack = getIconByPkg(context, pkgName, clsName.fullName(pkgName))
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

    private fun getIconByPkg(context: Context, pkg: String, cls: String): IntArray {
        val customize = customizeMap?.getDrawableMap(context)
        if (customize != null) {
            return customize.getDrawableName(pkg, cls)
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
        fun getDrawableName(packageName: String, clsName: String): IntArray

        /**
         * 图标数量
         */
        val iconCount: Int

        /**
         * 返回一个图标对象
         */
        operator fun get(index: Int): IconInfo
    }

    class IconInfo(val name: String, val pkg: ComponentName, val resId: Int)

    class AppInfo(val name: CharSequence, val pkg: String,
                  val srcIcon: Drawable, val iconPack: IntArray)

    class DefaultXmlMap(context: Context, xml: XmlPullParser): DrawableMap {

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
            private const val ATTR_TITLE = "title"
            private const val ATTR_COMPONENT = "component"
            private const val ATTR_DRAWABLE = "drawable"

            fun readFromAssets(context: Context, name: String): DefaultXmlMap {
                val newPullParser = Xml.newPullParser()
                newPullParser.setInput(context.assets.open(name), "UTF-8")
                return DefaultXmlMap(context, newPullParser)
            }

            fun readFromResource(context: Context, resId: Int): DefaultXmlMap {
                return DefaultXmlMap(context, context.resources.getXml(resId))
            }
        }

        init {
            decodeFromXml(xml, context)
        }

        private fun decodeFromXml(xml: XmlPullParser, context: Context) {
            var eventType = xml.eventType
            val defGroup = ArrayList<IconInfo>()
            iconMap[CATEGORY_DEFAULT] = defGroup
            var iconGroup = defGroup
            while (eventType != XmlPullParser.END_DOCUMENT) {
                val tagName = xml.name
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (CATEGORY == tagName) {
                            val name = xml.getAttributeValue(null, ATTR_TITLE)
                            iconGroup = iconMap[name] ?: ArrayList()
                            iconMap[name] = iconGroup
                        } else if (ITEM == tagName) {
                            val name = xml.getAttributeValue(null, ATTR_NAME) ?: ""
                            val pkg = xml.getAttributeValue(null, ATTR_COMPONENT) ?: ""
                            val icon = xml.getAttributeValue(null, ATTR_DRAWABLE) ?: ""
                            val info = IconInfo(name, parseComponent(pkg), findDrawableId(context, icon))
                            iconGroup.add(info)
                            allIcon.add(info)
                        }
                    }
                }
                eventType = xml.next()
            }
            if (defGroup.isEmpty()) {
                iconMap.remove(CATEGORY_DEFAULT)
            }
            if (xml is AutoCloseable) {
                xml.close()
            } else if (xml is Closeable) {
                xml.close()
            }
        }

        override fun getDrawableName(packageName: String, clsName: String): IntArray {
            val cache = iconCache[packageName]
            if (cache != null) {
                return cache
            }
            val icons = ArrayList<IconInfo>()
            for (icon in allIcon) {
                if (icon.pkg.packageName == packageName && icon.pkg.className == clsName) {
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

    class DrawableMapProvider(private val creator: (context: Context) -> DrawableMap?) {

        private var drawableMap: DrawableMap? = null
        private var isInit = false

        fun getDrawableMap(context: Context): DrawableMap? {
            if (!isInit) {
                drawableMap = creator(context)
                isInit = true
            }
            return drawableMap
        }
    }

}