package com.lollipop.iconcore.util

import android.text.TextUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.time.temporal.Temporal
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.collections.ArrayList

/**
 * @author lollipop
 * @date 10/27/20 23:01
 * 文件压缩辅助类
 */
class ZipHelper private constructor (private var zipFile: File) {

    companion object {

        const val SUFFIX = ".zip"

        const val TEMP_ZIP = "temp$SUFFIX"

        /**
         * 创建一个符合规范的压缩文件名
         */
        fun zipFile(dir: File, name: String): File {
            return File(dir, name + SUFFIX)
        }

        /**
         * 指定压缩文件名，开始一个文件压缩流程
         */
        fun zipTo(dir: File, name: String): ZipHelper {
            return zipTo(zipFile(dir, name))
        }

        /**
         * 指定一个文件的形式开始一个文件压缩流程
         */
        fun zipTo(file: File): ZipHelper {
            val zipFile = if (file.path.endsWith(SUFFIX, true)) {
                file
            } else {
                File(file, TEMP_ZIP)
            }
            return ZipHelper(zipFile)
        }

        /**
         * 指定一个路径，开始一个文件压缩流程
         */
        fun zipTo(path: String): ZipHelper {
            val fileName = if (path.endsWith(SUFFIX, true)) {
                path
            } else {
                path + File.separator + TEMP_ZIP
            }
            return ZipHelper(File(fileName))
        }
    }

    private val fileList = LinkedList<FileEntry>()

    /**
     * 添加一个文件到压缩包中
     */
    fun addFile(file: File): ZipHelper {
        fileList.addLast(FileEntry.create(file))
        return this
    }

    /**
     * 添加一组文件到压缩包中
     */
    fun addFiles(files: List<File>): ZipHelper {
        for (file in files) {
            addFile(file)
        }
        return this
    }

    /**
     * 以文件路径的形式添加文件到压缩包中
     */
    fun addFile(fileName: String): ZipHelper {
        return addFile(File(fileName))
    }

    /**
     * 如果当前压缩包已经存在了，那么删除它
     */
    fun removeExists(): ZipHelper {
        if (zipFile.exists()) {
            zipFile.delete()
        }
        return this
    }

    /**
     * 开始压缩流程
     * 此操作是异步的，可以放心在主线程中操作
     * @param callback 返回压缩后的压缩包文件，
     * 它可能会和最开始设置的有所不同，当压缩文件已存在时，
     * 它会创建一个新的文件并且添加别名
     */
    fun startUp(callback: (File) -> Unit) {
        doAsync {
            if (zipFile.exists()) {
                val path = zipFile.path
                val suffixIndex = path.lastIndexOf(".")
                val filePath = path.substring(0, suffixIndex)
                zipFile = File(filePath + "_new.zip")
            } else {
                zipFile.parentFile?.mkdirs()
            }
            run()
            onUI {
                callback(zipFile)
            }
        }
    }

    private fun run() {
        var fileOutput: ZipOutputStream? = null
        try {
            fileOutput = ZipOutputStream(FileOutputStream(zipFile))
            val buffer = ByteArray(2048)
            while (fileList.isNotEmpty()) {
                val fileEntry = fileList.removeFirst()
                try {
                    fileOutput.putNextEntry(ZipEntry(fileEntry.name))
                    if (fileEntry.isExist) {
                        if (fileEntry.isDir) {
                            val listFiles = fileEntry.listFiles()
                            for (child in listFiles) {
                                fileList.addLast(FileEntry.create(child, fileEntry))
                            }
                        } else {
                            var inputStream: FileInputStream? = null
                            try {
                                inputStream = FileInputStream(fileEntry.file)
                                var length = inputStream.read(buffer)
                                while (length >= 0) {
                                    fileOutput.write(buffer, 0, length)
                                    length = inputStream.read(buffer)
                                }
                                fileOutput.flush()
                            } catch (ee: Throwable) {
                                ee.printStackTrace()
                            } finally {
                                inputStream?.close()
                            }
                        }
                    }
                    fileOutput.closeEntry()
                } catch (ee: Throwable) {
                    ee.printStackTrace()
                }
            }
            fileOutput.flush()
        } catch (e: Throwable) {
            e.printStackTrace()
        } finally {
            fileOutput?.close()
        }
    }

    private class FileEntry(val name: String, val file: File) {

        companion object {
            fun create(file: File, parent: FileEntry? = null): FileEntry {
                val parentName = parent?.name
                val entryName = if (TextUtils.isEmpty(parentName)) {
                    file.name
                } else {
                    parentName + File.separator + file.name
                }
                return FileEntry(entryName, file)
            }
        }

        val isDir: Boolean
            get() {
                return file.isDirectory
            }

        val isExist: Boolean
            get() {
                return file.exists()
            }

        fun listFiles(): Array<File> {
            return file.listFiles() ?: arrayOf()
        }

    }

}