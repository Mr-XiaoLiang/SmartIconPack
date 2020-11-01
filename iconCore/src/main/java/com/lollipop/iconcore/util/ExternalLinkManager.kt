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

        /**
         * 打开应用商店
         */
        const val LINK_TYPE_STORE = -1

        /**
         * 打开指定的应用
         */
        const val LINK_TYPE_APP = 0

        /**
         * 打开一个网页
         */
        const val LINK_TYPE_WEB = 1

        /**
         * 未知操作
         * 这表示它不会被处理
         */
        const val LINK_TYPE_UNKNOWN = -2

        /**
         * 网页外链的地址
         */
        const val ARG_WEB_URL = "webUrl"

        /**
         * 链接标识
         */
        const val ARG_TAG = "linkTag"

        /**
         * 链接类型
         */
        const val KEY_LINK_TYPE = "linkType"

        /**
         * 空的外链
         */
        val EMPTY_INFO = LinkInfo("", "", 0, Intent(), "", "")

        /**
         * 已经解析的外链集合
         */
        private val externalLinkMap = HashMap<String, Array<LinkInfo>>()

        /**
         * 从意图中获取链接类型
         */
        fun getLinkType(intent: Intent): Int {
            return intent.getIntExtra(KEY_LINK_TYPE, LINK_TYPE_UNKNOWN)
        }

        /**
         * 尝试从意图中获取网页的链接
         */
        fun getWebUrl(intent: Intent): String {
            return intent.getStringExtra(ARG_WEB_URL)?:""
        }

        /**
         * 获取原始的配置中的url信息
         */
        fun getLinkUrl(intent: Intent): String {
            return intent.getStringExtra(ARG_TAG)?:""
        }

        /**
         * 尝试寻找一个外部链接的集合
         */
        fun optExternalLink(token: String): Array<LinkInfo>? {
            val array = externalLinkMap[token]?:return null
            return Array(array.size) { array[it] }
        }

        /**
         * 放置一个外部链接的信息
         */
        private fun putExternalLink(token: String, links: List<LinkInfo>) {
            externalLinkMap[token] = Array(links.size) { links[it] }
        }

        fun readFromAssets(context: Context, name: String): ExternalLinkProvider {
            val token = getTokenByAssets(name)
            val optExternalLink = optExternalLink(token)
            if (optExternalLink != null && optExternalLink.isNotEmpty()) {
                return SimpleInfoProvider(optExternalLink)
            }
            return DefXmlInfoProvider.readFromAssets(context, name)
        }

        fun readFromResource(context: Context, resId: Int): ExternalLinkProvider {
            val token = getTokenByResource(resId)
            val optExternalLink = optExternalLink(token)
            if (optExternalLink != null && optExternalLink.isNotEmpty()) {
                return SimpleInfoProvider(optExternalLink)
            }
            return DefXmlInfoProvider.readFromResource(context, resId)
        }

        private fun getTokenByResource(id: Int): String {
            return "Resource$id"
        }

        private fun getTokenByAssets(name: String): String {
            return "Assets$name"
        }

    }

    /**
     * 外部链接的数量
     */
    val linkCount: Int
        get() {
            return linkProvider?.linkCount?:0
        }

    /**
     * 通过序号获取一个链接信息
     */
    fun getLink(index: Int): LinkInfo {
        return linkProvider?.getLink(index)?: EMPTY_INFO
    }

    /**
     * 链接信息提供者的接口
     */
    interface ExternalLinkProvider {

        val linkCount: Int

        fun getLink(index: Int): LinkInfo

    }

    /**
     * 一个开放的基础的链接信息提供者
     * 它提供了一些标准的链接信息关键字
     */
    open class BaseDefInfoProvider: ExternalLinkProvider {

        protected val linkList = ArrayList<LinkInfo>()

        companion object {
            const val TAG_LINK = "link"
            const val TAG_ARG = "arg"

            const val ATTR_TITLE = "title"
            const val ATTR_SUMMARY = "summary"
            const val ATTR_ICON = "icon"
            const val ATTR_URL = "url"
            const val ATTR_OTHER1 = "attr1"
            const val ATTR_OTHER2 = "attr2"

            const val TYPE_ACTION = "action"
            const val TYPE_HTTP = "http"
            const val TYPE_COMPONENT = "ComponentInfo"
            const val TYPE_STORE = "store"

            const val URL_DATA = "data"

            const val ARG_NAME = "name"
            const val ARG_TYPE = "type"

            fun decodeUrl(intent: Intent, url: String) {
                intent.putExtra(ARG_TAG, url)
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
                    else -> {
                        intent.putExtra(KEY_LINK_TYPE, LINK_TYPE_UNKNOWN)
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

    /**
     * 默认的解析基于xml的外部链接提供者
     * <links>
        <link
        title=""
        summary=""
        icon=""
        url="store"/>

        <link
        title=""
        summary=""
        icon=""
        url="https://lollipoppp.com/"/>

        <link
        title=""
        summary=""
        icon=""
        url="action(android.settings.APPLICATION_DETAILS_SETTINGS)">
        <arg type="data">package:com.lollipop.smarticonpack</arg>
        </link>

        <link
        title=""
        summary=""
        icon=""
        url="ComponentInfo{com.google.android.apps.messaging/com.google.android.apps.messaging.ui.ConversationListActivity}">
        <arg name="argName1">arg value</arg>
        <arg name="argName2">arg value</arg>
        </link>

        </links>
     */
    class DefXmlInfoProvider(xml: XmlPullParser, context: Context, token: String): BaseDefInfoProvider() {

        companion object {
            fun readFromAssets(context: Context, name: String): DefXmlInfoProvider {
                val newPullParser = Xml.newPullParser()
                newPullParser.setInput(context.assets.open(name), "UTF-8")
                return DefXmlInfoProvider(
                    newPullParser, context, getTokenByAssets(name))
            }

            fun readFromResource(context: Context, resId: Int): DefXmlInfoProvider {
                return DefXmlInfoProvider(
                    context.resources.getXml(resId), context, getTokenByResource(resId))
            }
        }

        init {
            decodeFromXml(xml, context)
            putExternalLink(token, linkList)
        }

        private fun decodeFromXml(xml: XmlPullParser, context: Context) {
            linkList.clear()
            try {
                var eventType = xml.eventType
                var title = ""
                var summary = ""
                var icon = 0
                var url = Intent()
                var itemInfo = false
                var argType = ""
                var argName = ""
                var other1 = ""
                var other2 = ""
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    val tagName = xml.name
                    when (eventType) {
                        XmlPullParser.START_TAG -> {
                            if (TAG_LINK == tagName) {
                                title = xml.getAttributeValue(null, ATTR_TITLE)?:""
                                summary = xml.getAttributeValue(null, ATTR_SUMMARY)?:""
                                other1 = xml.getAttributeValue(null, ATTR_OTHER1)?:""
                                other2 = xml.getAttributeValue(null, ATTR_OTHER2)?:""
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
                                linkList.add(LinkInfo(title, summary, icon, url, other1, other2))
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

    private class SimpleInfoProvider(links: Array<LinkInfo>): BaseDefInfoProvider() {
        init {
            linkList.clear()
            for (link in links) {
                linkList.add(link)
            }
        }
    }

    data class LinkInfo(
        /** 链接信息的标题 **/
        val title: String,
        /** 链接信息的描述内容 **/
        val summary: String,
        /** 链接信息的配置图标 **/
        val icon: Int,
        /**
         *  链接信息的地址，它是一个组合的信息
         *  可以通过{@link ExternalLinkManager#getLinkType}
         *  获取链接信息的类型，包括：
         *  {@link ExternalLinkManager#LINK_TYPE_STORE}
         *  {@link ExternalLinkManager#LINK_TYPE_APP}
         *  {@link ExternalLinkManager#LINK_TYPE_WEB}
         *  {@link ExternalLinkManager#LINK_TYPE_UNKNOWN}
         *  等返回值
         *
         *  可以通过{@link ExternalLinkManager#getWebUrl}获取网页跳转的链接
         *  可以通过{@link ExternalLinkManager#getLinkUrl}获取链接信息中配置的原始信息
         **/
        val url: Intent,
        /** 额外的配置参数1，它没有明确的意义，
         * 仅仅作为扩展参数，在不同的地方有不同的意义 **/
        val attr1: String,
        /** 额外的配置参数2，它没有明确的意义，
         * 仅仅作为扩展参数，在不同的地方有不同的意义 **/
        val attr2: String)

}