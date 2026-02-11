package com.storozhuk.translator

import java.nio.file.Files
import java.nio.file.Paths

object TestUtils {

    fun readTestResourceFile(filename: String): String {
        val filePath = Paths.get("src/test/resources/$filename")
        return String(Files.readAllBytes(filePath)).trim()
    }
}

