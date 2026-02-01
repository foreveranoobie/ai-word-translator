package com.storozhuk.translator.service

import com.storozhuk.translator.data.WordRowData
import com.storozhuk.translator.db.WordsRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class WordsService(@Autowired private val wordsRepository: WordsRepository, @Autowired
private  val agentClient: AgentClient) {
    fun saveWord(word: String, explanation: String) {
        val filename = "${word}.txt"
        wordsRepository.saveWord(filename, explanation)
    }

    fun getAllWords(): List<String> {
        return wordsRepository.getAllWords()
    }

    fun getAllWordsWithContent(): List<WordRowData> {
        val wordsMap = wordsRepository.getAllWordsWithContent()
        return wordsMap.mapNotNull{ (k, v) -> WordRowData(k, v) }
    }

    fun explainWord(word: String){
        val question = "Explain the word \"${word}\""
        agentClient.askAgentToExplain(question)
    }
}