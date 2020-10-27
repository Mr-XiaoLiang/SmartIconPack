package com.lollipop.iconcore.util

import android.text.TextUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * @author lollipop
 * @date 10/27/20 23:01
 */
class ZipHelper private constructor (private var zipFile: File) {

    companion object {
        fun to(path: String): ZipHelper {
            val fileName = if (path.endsWith(".zip", true)) {
                path
            } else {
                path + File.separator + "temp.zip"
            }
            return ZipHelper(File(fileName))
        }
    }

    private val fileList = LinkedList<FileEntry>()

    fun addFile(file: File): ZipHelper {
        fileList.addLast(FileEntry.create(file))
        return this
    }

    fun addFile(fileName: String): ZipHelper {
        return addFile(File(fileName))
    }

    fun removeExists(): ZipHelper {
        if (zipFile.exists()) {
            zipFile.delete()
        }
        return this
    }

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