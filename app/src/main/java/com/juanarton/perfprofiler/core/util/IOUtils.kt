package com.juanarton.perfprofiler.core.util

import com.topjohnwu.superuser.Shell

object IOUtils {
    fun readFileAsList(path: String, fileName: String): List<String>? {
        val filePath = "$path/$fileName"
        val result = Shell.cmd("cat $filePath").exec()
        return if (result.isSuccess && result.out.isNotEmpty()) {
            result.out.first().split(" ")
        } else {
            null
        }
    }

    fun readFileAsString(path: String, fileName: String): String? {
        val filePath = "$path/$fileName"
        val result = Shell.cmd("cat $filePath").exec()
        return if (result.isSuccess && result.out.isNotEmpty()) {
            result.out.first()
        } else {
            null
        }
    }

    fun writeToFile(path: String, fileName: String, content: String, isGpu: Boolean): Boolean {
        val filePath = "$path/$fileName"

        return try {
            val process = Runtime.getRuntime().exec("su")
            val os = process.outputStream
            val writer = os.bufferedWriter()

            if (!isGpu) writer.write("chmod 644 \"$filePath\"\n")
            writer.write("echo \"$content\" > \"$filePath\"\n")
            if (!isGpu) writer.write("chmod 444 \"$filePath\"\n")
            writer.write("exit\n")
            writer.flush()
            writer.close()

            process.waitFor() == 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}