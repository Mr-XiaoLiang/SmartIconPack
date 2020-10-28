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
 *
 * 它提供了主要的Icon处理操作
 * @param flags 图标处理的一些标示，它可以优化一些性能以及内存使用
 * 请见 {@link #FLAG_SUPPORTED_INFO},
 * {@link #FLAG_UNSUPPORTED_INFO},
 * {@link #FLAG_ICON_PACK_INFO}
 * @param customizeMap 自定义图标包提供器
 * 它可能是必要的，如果没有设置，
 * 那么可能会导致找不到对应的图标而认为没有适配
 */
class IconHelper private constructor(
    private val flags: Int,
    private val customizeMap: DrawableMapProvider? = null) {

    companion object {

        /**
         * 只保留已适配应用的具体信息
         * 但是会保留未适配以及图标包的数量信息
         */
        const val FLAG_SUPPORTED_INFO = 1

        /**
         * 只保留未适配应用的具体信息
         * 但是会保留其他信息的数量
         */
        const val FLAG_UNSUPPORTED_INFO = 1 shl 1

        /**
         * 只保留图标包的信息
         * 这表示它不会记录系统应用的相关信息
         * 同时它会非常依赖{@link #customizeMap}
         */
        const val FLAG_ICON_PACK_INFO = 1 shl 2

        /**
         * 记录所有信息
         */
        const val FLAG_ALL_INFO = 0xFFFFFF

        /**
         * 记录所有应用信息
         */
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

        /**
         * 以只记录已适配应用的形式创建
         */
        fun supportedOnly(creator: (context: Context) -> DrawableMap?): IconHelper {
            return IconHelper(FLAG_SUPPORTED_INFO, DrawableMapProvider(creator))
        }

        /**
         * 以只记录未适配应用的形式创建
         */
        fun unsupportedOnly(creator: (context: Context) -> DrawableMap?): IconHelper {
            return IconHelper(FLAG_UNSUPPORTED_INFO, DrawableMapProvider(creator))
        }

        /**
         * 以只记录图标包的形式创建
         */
        fun iconPackOnly(creator: (context: Context) -> DrawableMap?): IconHelper {
            return IconHelper(FLAG_ICON_PACK_INFO, DrawableMapProvider(creator))
        }

        /**
         * 根据名字检索Drawable的id
         */
        fun findDrawableId(context: Context, name: String): Int {
            return context.findDrawableId(name)
        }

        /**
         * 解析Component信息
         */
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

        /**
         * 解析补全一个activity路径
         */
        fun String.fullName(pkg: String): String {
            return activityFullName(pkg, this)
        }

        /**
         * 自定义Flag的形式创建
         */
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

    /**
     * 所有应用的数量
     */
    val allAppCount: Int
        get() {
            return supportedCount + notSupportCount
        }

    /**
     * 未适配的应用数量
     */
    val notSupportCount: Int
        get() {
            if (flags and FLAG_UNSUPPORTED_INFO == 0) {
                return notSupportListSize
            }
            return notSupportList.size
        }

    /**
     * 已适配的应用数量
     */
    val supportedCount: Int
        get() {
            if (flags and FLAG_SUPPORTED_INFO == 0) {
                return supportedListSize
            }
            return supportedList.size
        }

    /**
     * 图标包的图标数量
     */
    val iconCount: Int
        get() {
            if (flags and FLAG_ICON_PACK_INFO == 0) {
                return iconListSize
            }
            return iconList.size
        }

    /**
     * 按照序号获取图标信息
     * 如果flag中未保留图标包信息，或找不到有效的图标
     * 那么会返回一个空的图标信息
     */
    fun getIconInfo(index: Int): IconInfo {
        if (index < 0 || index >= iconCount || flags and FLAG_ICON_PACK_INFO == 0) {
            return EMPTY_ICON
        }
        return iconList[index]
    }

    /**
     * 按照序号获取应用信息
     * 如果flag中未保留应用信息，或找不到有效的应用
     * 那么会返回一个空的应用信息
     */
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

    /**
     * 按照序号获取未适配的应用信息
     * 如果flag中未保留未适配的应用信息，或找不到有效的应用
     * 那么会返回一个空的应用信息
     */
    fun getNotSupportInfo(index: Int): AppInfo {
        if (flags and FLAG_UNSUPPORTED_INFO == 0) {
            return EMPTY_APP_INFO
        }
        return notSupportList[index]
    }

    /**
     * 按照序号获取已适配的应用信息
     * 如果flag中未保留已适配的应用信息，或找不到有效的应用
     * 那么会返回一个空的应用信息
     */
    fun getSupportedInfo(index: Int): AppInfo {
        if (flags and FLAG_SUPPORTED_INFO == 0) {
            return EMPTY_APP_INFO
        }
        return supportedList[index]
    }

    /**
     * 加载应用信息
     * 需要触发它来激活并获取图标信息
     * 如果没有调用，那么将会导致获取不到有效的信息
     * 如果图标信息发生了变更，那么需要重新触发初始化信息
     */
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

    /**
     * 图标包信息
     * @param name 图标名称
     * @param pkg 图标包对应的应用包名
     * @param resId 图标对应的drawable id
     */
    class IconInfo(val name: CharSequence, val pkg: ComponentName, val resId: Int)

    /**
     * 应用信息
     * @param name 应用名称
     * @param pkg 应用包名
     * @param srcIcon 应用原始的图标信息
     * @param iconPack 应用对应的图标包（允许一个应用对应多个图标）
     */
    class AppInfo(val name: CharSequence, val pkg: ComponentName,
                  val srcIcon: Drawable, val iconPack: IntArray) {
        /**
         * 通过包信息生成一个图标名称
         */
        val drawableName: String by lazy {
            pkg.className
                .fullName(pkg.packageName)
                .replace(".", "_")
                .toLowerCase()
        }
    }

    /**
     * 默认的图标包配置清单解析器
     */
    class DefaultXmlMap(context: Context, xml: XmlPullParser): DrawableMap {

        private val iconMap = HashMap<String, ArrayList<IconInfo>>()
        private val allIcon = ArrayList<IconInfo>()
        private val iconCache = HashMap<String, IntArray>()

        private val keys: Array<String> by lazy {
            iconMap.keys.toTypedArray()
        }

        companion object {

            /**
             * 从Assets中读取配置清单文件
             */
            fun readFromAssets(context: Context, name: String): DefaultXmlMap {
                val newPullParser = Xml.newPullParser()
                newPullParser.setInput(context.assets.open(name), "UTF-8")
                return DefaultXmlMap(context, newPullParser)
            }

            /**
             * 从res中读取配置清单文件
             */
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

    /**
     * 图标包提供者包装类
     */
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