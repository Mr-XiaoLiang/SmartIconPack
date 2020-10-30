package com.lollipop.iconcore.util

import android.content.Context
import android.text.TextUtils
import com.lollipop.iconcore.ui.IconHelper
import java.io.*
import java.lang.StringBuilder
import java.nio.charset.Charset

/**
 * @author lollipop
 * @date 10/27/20 23:52
 * Xml的构造器
 */
class XmlBuilder private constructor(private val tag: String) {

    companion object {
        private const val RETURN = "\r\n"
        private const val SPACE = " "

        /**
         * 默认的res节点
         */
        const val RESOURCES = "resources"

        /**
         * 默认的item节点
         */
        const val ITEM = "item"

        /**
         * 设置根节点并且创建一个xml
         */
        fun create(root: String): XmlBuilder {
            return XmlBuilder(root)
        }

        /**
         * 创建一个appInfo的xml配置信息
         */
        fun create(context: Context, infoList: List<IconHelper.AppInfo>): XmlBuilder {
            return create(context, infoList.size) { infoList[it] }
        }

        /**
         * 从自定义来源创建一个标准的app信息的配置文件
         */
        fun create(context: Context, count: Int, infoProvider: (Int) -> IconHelper.AppInfo): XmlBuilder {
            val builder = create(RESOURCES)
            for (index in 0 until count) {
                val info = infoProvider(index)
                builder.addChild(ITEM)
                    .addAttr(IconHelper.ATTR_NAME, info.getLabel(context).toString())
                    .addAttr(IconHelper.ATTR_COMPONENT, info.pkg.toString())
                    .addAttr(IconHelper.ATTR_DRAWABLE, info.drawableName)
            }
            return builder
        }
    }

    private var parent: XmlBuilder? = null
    private val children = ArrayList<XmlBuilder>()
    private val attributeList = ArrayList<Attribute>()
    private var text = ""

    private val commentList = ArrayList<String>()

    /**
     * 以名字添加一个子节点
     * 并且返回它
     * @return 主要注意的是，此处返回的是直接点，而非当前节点
     */
    fun addChild(tag: String): XmlBuilder {
        val xml = XmlBuilder(tag)
        addChild(xml)
        return xml
    }

    /**
     * 直接添加一个节点作为子节点
     */
    fun addChild(xml: XmlBuilder) {
        xml.parent = this
        children.add(xml)
    }

    /**
     * 添加属性信息
     */
    fun addAttr(name: String, value: String): XmlBuilder {
        attributeList.add(Attribute(name, value))
        return this
    }

    /**
     * 添加文本信息
     */
    fun setText(value: String): XmlBuilder {
        this.text = value
        return this
    }

    /**
     * 添加备注信息
     */
    fun addComment(value: String): XmlBuilder {
        commentList.add(value)
        return this
    }

    /**
     * 返回上一个节点
     * 如果没有上层节点，那么返回本身
     */
    fun up(): XmlBuilder {
        return parent?:this
    }

    /**
     * 返回根节点
     */
    fun upToRoot(): XmlBuilder {
        return parent?.upToRoot()?:this
    }

    /**
     * 是否存在父节点
     */
    val hasParent: Boolean
        get() {
            return parent != null
        }

    /**
     * 将当前节点及其子节点转换为字符串
     */
    override fun toString(): String {
        val builder = StringBuilder()
        if (!hasParent) {
            builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
            builder.append(RETURN)
        }
        for (comment in commentList) {
            // <!--  -->
            builder.append("<!-- ")
            builder.append(comment)
            builder.append(" -->")
            builder.append(RETURN)
        }
        builder.append("<")
        builder.append(tag)
        builder.append(SPACE)
        for (attr in attributeList) {
            builder.append(attr.name)
            builder.append("=\"")
            builder.append(attr.value)
            builder.append("\"")
            builder.append(SPACE)
        }
        if (children.isEmpty() && TextUtils.isEmpty(text)) {
            builder.append("/>")
            return builder.toString()
        }
        builder.append(">")
        builder.append(text)
        if (children.isNotEmpty()) {
            builder.append(RETURN)
            for (child in children) {
                builder.append(child.toString())
                builder.append(RETURN)
            }
        }
        builder.append("</")
        builder.append(tag)
        builder.append(">")
        return builder.toString()
    }

    /**
     * 直接将当前节点及其子节点写入文件中
     */
    fun writeTo(file: File) {
        try {
            if (file.exists()) {
                file.delete()
            } else {
                file.parentFile?.mkdirs()
            }
            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null
            val buffer = ByteArray(2048)
            try {
                inputStream = ByteArrayInputStream(toString().toByteArray(Charsets.UTF_8))
                outputStream = FileOutputStream(file)
                var length = inputStream.read(buffer)
                while (length >= 0) {
                    outputStream.write(buffer, 0, length)
                    length = inputStream.read(buffer)
                }
                outputStream.flush()
            } catch (ee: Throwable) {
                ee.printStackTrace()
            } finally {
                inputStream?.close()
                outputStream?.close()
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private data class Attribute(val name: String, val value: String)

}