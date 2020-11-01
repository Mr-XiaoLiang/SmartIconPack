package com.lollipop.iconcore.util

import android.content.Context
import android.util.Xml
import org.json.JSONArray
import org.xmlpull.v1.XmlPullParser
import java.io.Closeable

/**
 * @author lollipop
 * @date 10/25/20 23:02
 * 更新内容的管理器
 */
class UpdateInfoManager(private val provider: UpdateInfoProvider?) {

    companion object {
        private val EMPTY_INFO = VersionInfo("", 0, arrayOf())

        private val updateInfoCache = HashMap<String, Array<VersionInfo>>()

        private fun getTokenByResource(id: Int): String {
            return "Resource$id"
        }

        private fun getTokenByAssets(name: String): String {
            return "Assets$name"
        }

        /**
         * 尝试寻找一个外部链接的集合
         */
        private fun optVersionInfo(token: String): Array<VersionInfo>? {
            val array = updateInfoCache[token]?:return null
            return Array(array.size) { array[it] }
        }

        /**
         * 放置一个外部链接的信息
         */
        private fun putVersionInfo(token: String, infoList: List<VersionInfo>) {
            updateInfoCache[token] = Array(infoList.size) { infoList[it] }
        }

        /**
         * 从Assets解析更新内容
         */
        fun readXmlFromAssets(context: Context, name: String): UpdateInfoProvider {
            val versionInfo = optVersionInfo(getTokenByAssets(name))
            if (versionInfo != null && versionInfo.isNotEmpty()) {
                return SimpleProvider(versionInfo)
            }
            return DefXmlInfoProvider.readFromAssets(context, name)
        }

        /**
         * 从res解析更新内容
         */
        fun readXmlFromResource(context: Context, resId: Int): UpdateInfoProvider {
            val versionInfo = optVersionInfo(getTokenByResource(resId))
            if (versionInfo != null && versionInfo.isNotEmpty()) {
                return SimpleProvider(versionInfo)
            }
            return DefXmlInfoProvider.readFromResource(context, resId)
        }

    }

    /**
     * 更新记录的数量（版本数量）
     */
    val infoCount: Int
        get() {
            return provider?.infoCount?:0
        }

    /**
     * 根据序号获取版本信息
     */
    fun getVersionInfo(index: Int): VersionInfo {
        return provider?.getVersionInfo(index)?: EMPTY_INFO
    }

    /**
     * 通过版本号获取对应的版本更新信息
     * 如果找不到，那么将会返回一个空的对象
     */
    fun findVersionInfo(code: Int): VersionInfo {
        return provider?.findVersionInfo(code)?: EMPTY_INFO
    }

    interface UpdateInfoProvider {

        /**
         * 更新记录的数量
         */
        val infoCount: Int

        /**
         * 根据序号获取版本信息
         */
        fun getVersionInfo(index: Int): VersionInfo

        /**
         * 通过版本号获取对应的版本更新信息
         */
        fun findVersionInfo(code: Int): VersionInfo

    }

    /**
     * 基础的版本信息提供类
     */
    open class BaseDefInfoProvider: UpdateInfoProvider {

        companion object {
            const val VERSION = "version"
            const val ITEM = "item"

            const val ATTR_NAME = "name"
            const val ATTR_CODE = "code"
        }

        protected val versionInfoList = ArrayList<VersionInfo>()

        override val infoCount: Int
            get() = versionInfoList.size

        override fun getVersionInfo(index: Int): VersionInfo {
            if (index < 0 || index >= infoCount) {
                return EMPTY_INFO
            }
            return versionInfoList[index]
        }

        override fun findVersionInfo(code: Int): VersionInfo {
            for (info in versionInfoList) {
                if (info.code == code) {
                    return info
                }
            }
            return EMPTY_INFO
        }

    }

    /**
     * 默认的基于xml的版本更新信息解析类
     *
    <updates >

        <!--  版本  -->
        <version name="1.0" code="5">
            <!--  更新内容，数量不限   -->
            <item>增加基础的图标</item>
            <item>完成基础的图标包模板</item>
            <item>性能优化</item>
            <item>开发者QQ：1982568737</item>
        </version>

        <version name="0.9" code="4">
            <item>移除不必要的Java代码</item>
            <item>增加主题色配置</item>
            <item>核心代码精简</item>
            <item>开发者QQ：1982568737</item>
        </version>

    </updates>
     *
     */
    class DefXmlInfoProvider(xml: XmlPullParser, token: String): BaseDefInfoProvider() {

        companion object {
            /**
             * 从Assets解析更新内容
             */
            fun readFromAssets(context: Context, name: String): DefXmlInfoProvider {
                val newPullParser = Xml.newPullParser()
                newPullParser.setInput(context.assets.open(name), "UTF-8")
                return DefXmlInfoProvider(
                    newPullParser, getTokenByAssets(name))
            }

            /**
             * 从res解析更新内容
             */
            fun readFromResource(context: Context, resId: Int): DefXmlInfoProvider {
                return DefXmlInfoProvider(
                    context.resources.getXml(resId), getTokenByResource(resId))
            }
        }

        init {
            decodeFromXml(xml)
            putVersionInfo(token, versionInfoList)
        }

        private fun decodeFromXml(xml: XmlPullParser) {
            versionInfoList.clear()
            try {
                var eventType = xml.eventType
                val infoArray = ArrayList<String>()
                var name = ""
                var code = 0
                var itemInfo = false
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    val tagName = xml.name
                    when (eventType) {
                        XmlPullParser.START_TAG -> {
                            if (VERSION == tagName) {
                                name = xml.getAttributeValue(null, ATTR_NAME)
                                code = xml.getAttributeValue(null, ATTR_CODE).toInt()
                                infoArray.clear()
                            } else if (ITEM == tagName) {
                                itemInfo = true
                            }
                        }
                        XmlPullParser.TEXT -> {
                            if (itemInfo) {
                                infoArray.add(xml.text?:"")
                            }
                        }
                        XmlPullParser.END_TAG -> {
                            if (VERSION == tagName) {
                                versionInfoList.add(VersionInfo(name, code,
                                    infoArray.toArray(EMPTY_INFO.info)))
                            } else if (ITEM == tagName) {
                                itemInfo = false
                            }
                        }
                    }
                    eventType = xml.next()
                }
                if (xml is AutoCloseable) {
                    xml.close()
                } else if (xml is Closeable) {
                    xml.close()
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

    }

    /**
     * 默认的基于json的版本更新信息解析类
     *
        [
            {
                "name": "1.0",
                "code": 5,
                "item": [
                    "AAAA",
                    "BBBB"
                ]
            },
            {
                "name": "0.9",
                "code": 4,
                "item": [
                    "CCCC",
                    "DDDD"
                ]
            }
        ]
     *
     */
    class DefJsonInfoProvider(json: String): BaseDefInfoProvider() {

        init {
            decodeFromJson(json)
        }

        private fun decodeFromJson(json: String) {
            versionInfoList.clear()
            try {
                val jsonArray = JSONArray(json)
                for (index in 0 until jsonArray.length()) {
                    val obj = jsonArray.optJSONObject(index)?:continue
                    val name = obj.optString(ATTR_NAME) ?: ""
                    val code = obj.optInt(ATTR_CODE, 0)
                    val info = obj.optJSONArray(ITEM) ?: continue
                    val infoArray = Array(info.length()){ info.optString(it)?:"" }
                    versionInfoList.add(VersionInfo(name, code, infoArray))
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }


    }

    /**
     * 一个简单的内容提供者包装类
     */
    private class SimpleProvider(array: Array<VersionInfo>): BaseDefInfoProvider() {
        init {
            versionInfoList.clear()
            for (info in array) {
                versionInfoList.add(info)
            }
        }
    }

    /**
     * 版本信息
     * @param name 版本名 versionName
     * @param code 版本号 versionCode
     * @param info 更新内容
     */
    class VersionInfo(val name: String, val code: Int, val info: Array<String>)

}