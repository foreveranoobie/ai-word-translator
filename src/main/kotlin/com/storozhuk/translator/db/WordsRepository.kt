package com.storozhuk.translator.db

import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Repository
import java.io.File
import java.io.Serializable

@Repository
class WordsRepository : Serializable {

    private val wordsDir = File("words")

    @PostConstruct
    fun initializeWordsFolder() {
        if (!wordsDir.exists()) {
            wordsDir.mkdirs()
        }
    }

    fun saveWord(filename: String, content: String) {
        val file = File(wordsDir, filename)
        file.writeText(content)
    }

    fun getAllWords(): List<String> {
        return wordsDir.listFiles()?.map { it.nameWithoutExtension } ?: emptyList()
    }

    fun getAllWordsWithContent(): Map<String, String> {
        return wordsDir.listFiles()?.associate { file ->
            file.nameWithoutExtension to file.readText()
        } ?: emptyMap()
    }
}