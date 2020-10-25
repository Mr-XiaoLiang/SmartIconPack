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
    }

    val infoCount: Int
        get() {
            return provider?.infoCount?:0
        }

    fun getVersionInfo(index: Int): VersionInfo {
        return provider?.getVersionInfo(index)?: EMPTY_INFO
    }

    fun findVersionInfo(code: Int): VersionInfo {
        return provider?.findVersionInfo(code)?: EMPTY_INFO
    }

    interface UpdateInfoProvider {

        val infoCount: Int

        fun getVersionInfo(index: Int): VersionInfo

        fun findVersionInfo(code: Int): VersionInfo

    }

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

    class DefXmlInfoProvider(xml: XmlPullParser): BaseDefInfoProvider() {

        companion object {
            fun readFromAssets(context: Context, name: String): DefXmlInfoProvider {
                val newPullParser = Xml.newPullParser()
                newPullParser.setInput(context.assets.open(name), "UTF-8")
                return DefXmlInfoProvider(newPullParser)
            }

            fun readFromResource(context: Context, resId: Int): DefXmlInfoProvider {
                return DefXmlInfoProvider(context.resources.getXml(resId))
            }
        }

        init {
            decodeFromXml(xml)
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
                    val code = obj.optInt(ATTR_CODE) ?: 0
                    val info = obj.optJSONArray(ITEM) ?: continue
                    val infoArray = Array(info.length()){ it ->
                        info.optString(it)?:""
                    }
                    versionInfoList.add(VersionInfo(name, code, infoArray))
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }


    }

    class VersionInfo(val name: String, val code: Int, val info: Array<String>)

}