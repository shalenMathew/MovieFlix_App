package com.shalenmathew.movieflix.core.utils

import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object ZipHelper {

    fun zip(files: List<File>, zipFile: File) {
        ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { out ->
            files.forEach { file ->
                if (file.exists()) {
                    val entry = ZipEntry(file.name)
                    out.putNextEntry(entry)
                    file.inputStream().use { it.copyTo(out) }
                }
            }
        }
    }

    fun unzip(zipFile: File, targetDirectory: File) {
        ZipInputStream(BufferedInputStream(zipFile.inputStream())).use { zis ->
            var entry: ZipEntry? = zis.nextEntry
            while (entry != null) {
                val file = File(targetDirectory, entry.name)
                if (entry.isDirectory) {
                    file.mkdirs()
                } else {
                    file.parentFile?.mkdirs()
                    file.outputStream().use { zis.copyTo(it) }
                }
                entry = zis.nextEntry
            }
        }
    }
}
