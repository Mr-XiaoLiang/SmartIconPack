package com.lollipop.iconcore.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.Xml
import com.lollipop.iconcore.util.findDrawableId
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
class IconHelper private constructor(
    private val flags: Int,
    private val customizeMap: DrawableMapProvider? = null) {

    companion object {

        const val FLAG_SUPPORTED_INFO = 1
        const val FLAG_UNSUPPORTED_INFO = 1 shl 1
        const val FLAG_ICON_PACK_INFO = 1 shl 2

        const val FLAG_ALL_INFO = 0xFFFFFF
        const val FLAG_FULL_APP_INFO = FLAG_SUPPORTED_INFO or FLAG_UNSUPPORTED_INFO

        const val CATEGORY = "category"
        const val ITEM = "item"
        const val CATEGORY_DEFAULT = "default"

        const val ATTR_NAME = "name"
        const val ATTR_TITLE = "title"
        const val ATTR_COMPONENT = "component"
        const val ATTR_DRAWABLE = "drawable"

        private val EMPTY_ICON_ID = IntArray(0)
        private val EMPTY_COMPONENT = ComponentName("", "")
        private val EMPTY_ICON = IconInfo("", EMPTY_COMPONENT, 0)
        private val EMPTY_APP_INFO = AppInfo("", EMPTY_COMPONENT, ColorDrawable(Color.BLACK), EMPTY_ICON_ID)

        fun supportedOnly(creator: (context: Context) -> DrawableMap?): IconHelper {
            return IconHelper(FLAG_SUPPORTED_INFO, DrawableMapProvider(creator))
        }

        fun unsupportedOnly(creator: (context: Context) -> DrawableMap?): IconHelper {
            return IconHelper(FLAG_UNSUPPORTED_INFO, DrawableMapProvider(creator))
        }

        fun iconPackOnly(creator: (context: Context) -> DrawableMap?): IconHelper {
            return IconHelper(FLAG_ICON_PACK_INFO, DrawableMapProvider(creator))
        }

        fun findDrawableId(context: Context, name: String): Int {
            return context.findDrawableId(name)
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

        fun newHelper(flags: Int, creator: (context: Context) -> DrawableMap?): IconHelper {
            return IconHelper(flags, DrawableMapProvider(creator))
        }

    }

    private val notSupportList = ArrayList<AppInfo>()
    private val supportedList = ArrayList<AppInfo>()
    private var iconList = ArrayList<IconInfo>()
    private var drawableMap: DrawableMap? = null

    private var supportedListSize = 0
    private var notSupportListSize = 0
    private var iconListSize = 0

    val allAppCount: Int
        get() {
            return supportedCount + notSupportCount
        }

    val notSupportCount: Int
        get() {
            if (flags and FLAG_UNSUPPORTED_INFO == 0) {
                return notSupportListSize
            }
            return notSupportList.size
        }

    val supportedCount: Int
        get() {
            if (flags and FLAG_SUPPORTED_INFO == 0) {
                return supportedListSize
            }
            return supportedList.size
        }

    val iconCount: Int
        get() {
            if (flags and FLAG_ICON_PACK_INFO == 0) {
                return iconListSize
            }
            return iconList.size
        }

    fun getIconInfo(index: Int): IconInfo {
        if (index < 0 || index >= iconCount || flags and FLAG_ICON_PACK_INFO == 0) {
            return EMPTY_ICON
        }
        return iconList[index]
    }

    fun getAppInfo(index: Int): AppInfo {
        if (index < 0 || index >= allAppCount) {
            return EMPTY_APP_INFO
        }
        if (index < supportedCount) {
            if (flags and FLAG_SUPPORTED_INFO == 0) {
                return EMPTY_APP_INFO
            }
            return getSupportedInfo(index)
        }
        if (flags and FLAG_UNSUPPORTED_INFO == 0) {
            return EMPTY_APP_INFO
        }
        return getNotSupportInfo(index - supportedCount)
    }

    fun getNotSupportInfo(index: Int): AppInfo {
        if (flags and FLAG_UNSUPPORTED_INFO == 0) {
            return EMPTY_APP_INFO
        }
        return notSupportList[index]
    }

    fun getSupportedInfo(index: Int): AppInfo {
        if (flags and FLAG_SUPPORTED_INFO == 0) {
            return EMPTY_APP_INFO
        }
        return supportedList[index]
    }

    fun loadAppInfo(context: Context) {
        if (drawableMap == null) {
            drawableMap = customizeMap?.getDrawableMap(context)
        }
        loadAppInfoOnly(context)
        loadIconPackInfoOnly(context)
    }

    private fun loadAppInfoOnly(context: Context) {
        supportedList.clear()
        notSupportList.clear()
        supportedListSize = 0
        notSupportListSize = 0
        val isLoadSupportedInfo = flags and FLAG_SUPPORTED_INFO != 0
        val isLoadUnsupportedInfo = flags and FLAG_UNSUPPORTED_INFO != 0
        if (!isLoadSupportedInfo && !isLoadUnsupportedInfo) {
            return
        }
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        val appList = pm.queryIntentActivities(mainIntent, 0)
        // 调用系统排序 ， 根据name排序
        Collections.sort(appList, ResolveInfo.DisplayNameComparator(pm))
        for (appInfo in appList){
            val pkgName = appInfo.activityInfo.packageName
            val clsName = appInfo.activityInfo.name.fullName(pkgName)
            val iconPack = getIconByPkg(context, pkgName, clsName)
            if (iconPack.isEmpty()) {
                if (isLoadUnsupportedInfo) {
                    notSupportList.add(AppInfo(
                        appInfo.loadLabel(pm), ComponentName(pkgName, clsName),
                        appInfo.loadIcon(pm), iconPack))
                } else {
                    notSupportListSize ++
                }
            } else {
                if (isLoadSupportedInfo) {
                    supportedList.add(AppInfo(
                        appInfo.loadLabel(pm), ComponentName(pkgName, clsName),
                        appInfo.loadIcon(pm), iconPack))
                } else {
                    supportedListSize ++
                }
            }
        }
    }

    private fun loadIconPackInfoOnly(context: Context) {
        iconList.clear()
        iconListSize = 0
        val map = drawableMap
        if (flags and FLAG_ICON_PACK_INFO != 0) {
            if (map == null) {
                val deduplicationList = ArrayList<Int>()
                for (app in supportedList) {
                    val iconPack = app.iconPack
                    if (iconPack.isEmpty()) {
                        continue
                    }
                    for (icon in iconPack) {
                        if (icon != 0 && deduplicationList.indexOf(icon) < 0) {
                            deduplicationList.add(icon)
                            iconList.add(IconInfo(app.name, app.pkg, icon))
                        }
                    }
                }
            } else {
                for (index in 0 until map.iconCount) {
                    iconList.add(map[index])
                }
            }
        } else {
            if (map != null) {
                iconListSize = map.iconCount
            } else if (supportedList.isNotEmpty()) {
                val deduplicationList = ArrayList<Int>()
                for (app in supportedList) {
                    val iconPack = app.iconPack
                    if (iconPack.isEmpty()) {
                        continue
                    }
                    for (icon in iconPack) {
                        if (icon != 0 && deduplicationList.indexOf(icon) < 0) {
                            deduplicationList.add(icon)
                            iconList.add(IconInfo(app.name, app.pkg, icon))
                        }
                    }
                }
            } else {
                iconListSize = supportedListSize
            }
        }
    }

    private fun getIconByPkg(context: Context, pkg: String, cls: String): IntArray {
        val customize = drawableMap
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

    class IconInfo(val name: CharSequence, val pkg: ComponentName, val resId: Int)

    class AppInfo(val name: CharSequence, val pkg: ComponentName,
                  val srcIcon: Drawable, val iconPack: IntArray)

    class DefaultXmlMap(context: Context, xml: XmlPullParser): DrawableMap {

        private val iconMap = HashMap<String, ArrayList<IconInfo>>()
        private val allIcon = ArrayList<IconInfo>()
        private val iconCache = HashMap<String, IntArray>()

        private val keys: Array<String> by lazy {
            iconMap.keys.toTypedArray()
        }

        companion object {

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