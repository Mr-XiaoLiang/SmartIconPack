package com.lollipop.iconkit

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.lollipop.iconcore.ui.IconHelper
import com.lollipop.iconcore.util.ExternalLinkManager
import com.lollipop.iconcore.util.MakerInfoManager
import com.lollipop.iconcore.util.UpdateInfoManager
import java.io.File

/**
 * @author lollipop
 * @date 10/25/20 21:14
 */
object LIconKit {

    private var iconMapCreator: IconMapCreator? = null

    fun init(mapCreator: IconMapCreator) {
        iconMapCreator = mapCreator
    }

    fun createHomePageMap(context: Context): IconHelper.DrawableMap? {
        return iconMapCreator?.createHomePageMap(context)
    }

    fun createAppsPageMap(context: Context): IconHelper.DrawableMap? {
        return iconMapCreator?.createAppsPageMap(context)
    }

    fun createRequestPageMap(context: Context): IconHelper.DrawableMap? {
        return iconMapCreator?.createRequestPageMap(context)
    }

    fun createUpdateInfoProvider(context: Context): UpdateInfoManager.UpdateInfoProvider? {
        return iconMapCreator?.createUpdateInfoProvider(context)
    }

    fun createLinkInfoProvider(context: Context): ExternalLinkManager.ExternalLinkProvider? {
        return iconMapCreator?.createLinkInfoProvider(context)
    }

    fun createMakerInfoProvider(context: Context): MakerInfoManager.MakerInfoProvider? {
        return iconMapCreator?.createMakerInfoProvider(context)
    }

    interface IconMapCreator {
        fun createHomePageMap(context: Context): IconHelper.DrawableMap
        fun createAppsPageMap(context: Context): IconHelper.DrawableMap
        fun createRequestPageMap(context: Context): IconHelper.DrawableMap
        fun createUpdateInfoProvider(context: Context): UpdateInfoManager.UpdateInfoProvider?
        fun createLinkInfoProvider(context: Context): ExternalLinkManager.ExternalLinkProvider?
        fun createMakerInfoProvider(context: Context): MakerInfoManager.MakerInfoProvider?
    }

    fun createDefXmlMapFromResource(context: Context, resId: Int): IconHelper.DrawableMap {
        return IconHelper.DefaultXmlMap.readFromResource(context, resId)
    }

    fun createDefXmlMapFromAssets(context: Context, fileName: String): IconHelper.DrawableMap {
        return IconHelper.DefaultXmlMap.readFromAssets(context, fileName)
    }

    fun readUpdateXmlFromResource(context: Context, resId: Int): UpdateInfoManager.UpdateInfoProvider {
        return UpdateInfoManager.readXmlFromResource(context, resId)
    }

    fun readUpdateXmlFromAssets(context: Context, name: String): UpdateInfoManager.UpdateInfoProvider {
        return UpdateInfoManager.readXmlFromAssets(context, name)
    }

    fun readUpdateInfoByJson(json: String): UpdateInfoManager.UpdateInfoProvider {
        return UpdateInfoManager.DefJsonInfoProvider(json)
    }

    fun readLinkInfoByXml(context: Context, resId: Int): ExternalLinkManager.ExternalLinkProvider {
        return ExternalLinkManager.readFromResource(context, resId)
    }

    fun readLinkInfoByXmlFromAssets(context: Context, name: String): ExternalLinkManager.ExternalLinkProvider {
        return ExternalLinkManager.readFromAssets(context, name)
    }

    /**
     * 发送电子邮件
     * @param context 上下文，用于获取必要的数据资源
     * @param chooseDialogTitle 选择发送软件时的title，并非邮件标题
     * @param subject 主题，邮件的主题（等同于邮件的标题）
     * @param text 邮件内容
     * @param file 附件，邮件的附件
     */
    fun mailTo(context: Context, chooseDialogTitle: Int,
               subject: Int, text: Int, file: File?) {
        // 必须明确使用mailto前缀来修饰邮件地址,如果使用
        // intent.putExtra(Intent.EXTRA_EMAIL, email)，结果将匹配不到任何应用
        val emailId = createMakerInfoProvider(context)?.email?:0
        val email = context.getString(emailId)
        val uri = Uri.parse("mailto:$email")
        val intent = Intent(Intent.ACTION_SEND, uri)
        intent.type = "application/octet-stream"
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        if (subject != 0) {
            intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(subject)) // 主题
        }
        if (text != 0) {
            intent.putExtra(Intent.EXTRA_TEXT, context.getString(text)) // 正文
        }
        if (file != null) {
            intent.putExtra(Intent.EXTRA_STREAM,
                FileProvider.getUriForFile(context,
                    "${context.packageName}.provider", file))
        }
        context.startActivity(
            Intent.createChooser(intent, context.getString(chooseDialogTitle)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
    }

}