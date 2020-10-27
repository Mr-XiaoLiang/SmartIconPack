package com.lollipop.iconcore.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.util.ArraySet
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

/**
 * @author lollipop
 * @date 10/28/20 04:32
 * 保存Icon的工具
 */
class IconSaveHelper (
    private val iconWidth: Int,
    private val iconHeight: Int = iconWidth) {

    companion object {
        const val SUFFIX = ".png"
    }

    private val iconList = ArrayList<Icon>()
    private val fileList = ArrayList<File>()

    fun add(context: Context, id: Int, name: String): IconSaveHelper {
        val drawable = ContextCompat.getDrawable(context, id)?:return this
        return add(drawable, name)
    }

    fun add(drawable: Drawable, name: String): IconSaveHelper {
        iconList.add(Icon(drawable, name))
        return this
    }

    fun saveTo(dir: File) {
        if (iconList.isEmpty()) {
            return
        }
        try {
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val bitmap = Bitmap.createBitmap(iconWidth, iconHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            for (icon in iconList) {
                var outputStream: OutputStream? = null
                try {
                    val iconFile = File(dir, icon.name + SUFFIX)
                    outputStream = FileOutputStream(iconFile)
                    val drawable = icon.drawable
                    drawable.setBounds(0, 0, iconWidth, iconHeight)
                    // 绘制前清理历史内容
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                    drawable.draw(canvas)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    outputStream.flush()
                    fileList.add(iconFile)
                } catch (e: Throwable) {
                    e.printStackTrace()
                } finally {
                    outputStream?.close()
                }
            }
            bitmap.recycle()
        } catch (ee: Throwable) {
            ee.printStackTrace()
        }
    }

    fun getFiles(): ArrayList<File> {
        return fileList
    }

    private data class Icon(val drawable: Drawable, val name: String)

}