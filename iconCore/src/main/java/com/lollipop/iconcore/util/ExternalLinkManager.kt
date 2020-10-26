package com.lollipop.iconcore.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.text.TextUtils
import android.util.Xml
import com.lollipop.iconcore.ui.IconHelper
import org.xmlpull.v1.XmlPullParser
import java.io.Closeable

/**
 * @author lollipop
 * @date 10/26/20 15:35
 * 外部链接管理器
 */
class ExternalLinkManager(private val linkProvider: ExternalLinkProvider?) {

    companion object {

        const val LINK_TYPE_STORE = -1
        const val LINK_TYPE_APP = 0
        const val LINK_TYPE_WEB = 1
        const val LINK_TYPE_UNKNOWN = -2

        const val ARG_WEB_URL = "webUrl"

        const val KEY_LINK_TYPE = "linkType"

        val EMPTY_INFO = LinkInfo("", "", 0, Intent())

        fun getLinkType(intent: Intent): Int {
            return intent.getIntExtra(KEY_LINK_TYPE, LINK_TYPE_UNKNOWN)
        }

        fun getWebUrl(intent: Intent): String {
            return intent.getStringExtra(ARG_WEB_URL)?:""
        }

    }

    val linkCount: Int
        get() {
            return linkProvider?.linkCount?:0
        }

    fun getLink(index: Int): LinkInfo {
        return linkProvider?.getLink(index)?: EMPTY_INFO
    }

    interface ExternalLinkProvider {

        val linkCount: Int

        fun getLink(index: Int): LinkInfo

    }

    open class BaseDefInfoProvider: ExternalLinkProvider {

        protected val linkList = ArrayList<LinkInfo>()

        companion object {
            const val TAG_LINK = "link"
            const val TAG_ARG = "arg"

            const val ATTR_TITLE = "title"
            const val ATTR_SUMMARY = "summary"
            const val ATTR_ICON = "icon"
            const val ATTR_URL = "url"

            const val TYPE_ACTION = "action"
            const val TYPE_HTTP = "http"
            const val TYPE_COMPONENT = "ComponentInfo"
            const val TYPE_STORE = "store"

            const val URL_DATA = "data"

            const val ARG_NAME = "name"
            const val ARG_TYPE = "type"
            const val ARG_VALUE = "value"

            fun decodeUrl(intent: Intent, url: String) {
                when {
                    url.startsWith(TYPE_ACTION, true) -> {
                        val start = url.indexOf("(") + 1
                        val end = url.lastIndexOf(")")
                        if (start in 0 until end) {
                            intent.action = url.substring(start, end)
                            intent.putExtra(KEY_LINK_TYPE, LINK_TYPE_APP)
                        }
                    }
                    url.startsWith(TYPE_HTTP, true) -> {
                        intent.putExtra(ARG_WEB_URL, url)
                        intent.putExtra(KEY_LINK_TYPE, LINK_TYPE_WEB)
                    }
                    url.startsWith(TYPE_COMPONENT, true) -> {
                        intent.component = IconHelper.parseComponent(url)
                        intent.putExtra(KEY_LINK_TYPE, LINK_TYPE_APP)
                    }
                    url == TYPE_STORE -> {
                        intent.putExtra(KEY_LINK_TYPE, LINK_TYPE_STORE)
                    }
                }
            }

        }

        override val linkCount: Int
            get() = linkList.size

        override fun getLink(index: Int): LinkInfo {
            if (index < 0 || index >= linkCount) {
                return EMPTY_INFO
            }
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            return linkList[index]
        }

    }

    class DefXmlInfoProvider(xml: XmlPullParser, context: Context): BaseDefInfoProvider() {

        companion object {
            fun readFromAssets(context: Context, name: String): DefXmlInfoProvider {
                val newPullParser = Xml.newPullParser()
                newPullParser.setInput(context.assets.open(name), "UTF-8")
                return DefXmlInfoProvider(newPullParser, context)
            }

            fun readFromResource(context: Context, resId: Int): DefXmlInfoProvider {
                return DefXmlInfoProvider(context.resources.getXml(resId), context)
            }
        }

        init {
            decodeFromXml(xml, context)
        }

        private fun decodeFromXml(xml: XmlPullParser, context: Context) {
            linkList.clear()
            try {
                var eventType = xml.eventType
                var title = ""
                var summary = ""
                var icon = 0
                var url: Intent = Intent()
                var itemInfo = false
                var argType = ""
                var argName = ""
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    val tagName = xml.name
                    when (eventType) {
                        XmlPullParser.START_TAG -> {
                            if (TAG_LINK == tagName) {
                                title = xml.getAttributeValue(null, ATTR_TITLE)?:""
                                summary = xml.getAttributeValue(null, ATTR_SUMMARY)?:""
                                icon = context.findDrawableId(
                                    xml.getAttributeValue(null, ATTR_ICON))
                                url = Intent()
                                decodeUrl(url,
                                    xml.getAttributeValue(null, ATTR_URL) ?: "")
                            } else if (TAG_ARG == tagName) {
                                itemInfo = true
                                argType = xml.getAttributeValue(null, ARG_TYPE) ?: ""
                                argName = xml.getAttributeValue(null, ARG_NAME) ?: ""
                            }
                        }
                        XmlPullParser.TEXT -> {
                            if (itemInfo) {
                                if (URL_DATA == argType) {
                                    url.data = Uri.parse(xml.text?:"")
                                } else if (!TextUtils.isEmpty(argName)) {
                                    url.putExtra(argName, xml.text?:"")
                                }
                            }
                        }
                        XmlPullParser.END_TAG -> {
                            if (TAG_LINK == tagName) {
                                linkList.add(LinkInfo(title, summary, icon, url))
                            } else if (TAG_ARG == tagName) {
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

    data class LinkInfo(val title: String, val summary: String, val icon: Int, val url: Intent)

}