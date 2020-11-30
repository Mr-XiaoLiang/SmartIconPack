package com.lollipop.iconcore.util

import android.content.Context
import android.text.TextUtils
import android.util.Xml
import com.lollipop.iconcore.ui.IconHelper
import org.json.JSONArray
import org.xmlpull.v1.XmlPullParser
import java.io.Closeable
import java.io.File
import java.io.FileInputStream

/**
 * @author lollipop
 * @date 11/30/20 13:15
 * AppFilter的合并辅助工具
 */
class AppFilterMergeHelper {

    companion object {
        const val ITEM = XmlBuilder.ITEM
        const val ATTR_NAME = IconHelper.ATTR_NAME
        const val ATTR_COMPONENT = IconHelper.ATTR_COMPONENT
        const val ATTR_DRAWABLE = IconHelper.ATTR_DRAWABLE
    }

    private val hashMap = HashMap<String, App>()

    /**
     * 合并多个来源的文件信息
     */
    fun marge(vararg files: FileDecoder) {
        for (file in files) {
            read(file)
        }
    }

    private fun read(decoder: FileDecoder) {
        for (index in 0 until decoder.count) {
            val app = decoder[index]
            if (!TextUtils.isEmpty(app.component)) {
                hashMap[app.component] = app
            }
        }
    }

    override fun toString(): String {
        val builder = XmlBuilder.create(XmlBuilder.RESOURCES)
        val values = hashMap.values
        for (app in values) {
            builder.addChild(ITEM)
                .addAttr(ATTR_NAME, app.name)
                .addAttr(ATTR_COMPONENT, app.component)
                .addAttr(ATTR_DRAWABLE, app.drawable)
        }
        return builder.toString()
    }

    class XmlDecoder(xml: XmlPullParser): FileDecoder {

        companion object {

            /**
             * 从Assets中读取配置清单文件
             */
            fun readFromAssets(context: Context, name: String): XmlDecoder {
                val newPullParser = Xml.newPullParser()
                newPullParser.setInput(context.assets.open(name), "UTF-8")
                return XmlDecoder(newPullParser)
            }

            /**
             * 从res中读取配置清单文件
             */
            fun readFromResource(context: Context, resId: Int): XmlDecoder {
                return XmlDecoder(context.resources.getXml(resId))
            }

            /**
             * 从文件中读取
             */
            fun readFromFile(file: File): XmlDecoder {
                val newPullParser = Xml.newPullParser()
                newPullParser.setInput(FileInputStream(file), "UTF-8")
                return XmlDecoder(newPullParser)
            }

        }

        private val appList = ArrayList<App>()

        init {
            decodeFromXml(xml)
        }

        override val count: Int
            get() {
                return appList.size
            }

        override fun get(index: Int): App {
            return appList[index]
        }

        private fun decodeFromXml(xml: XmlPullParser) {
            appList.clear()
            var eventType = xml.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                val tagName = xml.name
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (ITEM == tagName) {
                            val name = xml.getAttributeValue(null, ATTR_NAME) ?: ""
                            val pkg = xml.getAttributeValue(null, ATTR_COMPONENT) ?: ""
                            val icon = xml.getAttributeValue(null, ATTR_DRAWABLE) ?: ""
                            appList.add(App(name, pkg, icon))
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
        }

    }

    class JsonDecoder(json: JSONArray): FileDecoder {

        private val appList = ArrayList<App>()

        override val count: Int
            get() {
                return appList.size
            }

        override fun get(index: Int): App {
            return appList[index]
        }

        private fun decodeFromJson(json: JSONArray) {
            appList.clear()
            val length = json.length()
            for (index in 0 until length) {
                val obj = json.optJSONObject(index)?:continue
                appList.add(App(
                    obj.optString(ATTR_NAME) ?: "",
                    obj.optString(ATTR_COMPONENT) ?: "",
                    obj.optString(ATTR_DRAWABLE) ?: ""))
            }
        }

    }

    interface FileDecoder {
        val count: Int
        operator fun get(index: Int): App
    }

    class App(val name: String, val component: String, val drawable: String)

}