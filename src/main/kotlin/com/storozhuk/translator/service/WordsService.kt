package com.storozhuk.translator.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.storozhuk.translator.data.WordExplanationData
import com.storozhuk.translator.data.WordRowData
import com.storozhuk.translator.db.WordsRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class WordsService(
    @Autowired private val wordsRepository: WordsRepository,
    @Autowired private val agentClient: AgentClient
) {

    private val objectMapper: ObjectMapper = ObjectMapper()

    fun saveWord(word: String, explanation: String) {
        val filename = "${word}.txt"
        if(wordsRepository.findWordByName(filename) == null) {
            wordsRepository.saveWord(filename, explanation)
        }
    }

    fun existsWord(word: String): Boolean {
        val filename = "${word}.txt"
        return wordsRepository.findWordByName(filename) != null
    }

    fun updateWord(wordExplanationData: WordExplanationData) {
        val contents = objectMapper.writeValueAsString(wordExplanationData)
        saveWord(wordExplanationData.word!!, contents)
    }

    fun getAllWordsWithContent(): MutableMap<String, WordExplanationData> {
        val wordsMap = wordsRepository.getAllWordsWithContent()
        val mappedResult = wordsMap.mapValues {
            objectMapper.readValue(
                it.value, WordExplanationData::class.java
            )
        }.mapKeys { it.value.word!! }.toMutableMap()

        return mappedResult
    }

    fun explainWord(word: String): WordExplanationData {
        val question = "Explain the word \"${word}\""
        val response = agentClient.askAgentToExplain(question)
        response?.let {
            saveWord(word, response)
            return objectMapper.readValue(
                response, WordExplanationData::class.java
            )
        }
        return WordExplanationData(word, "", null)
    }

    fun getTranslationsFromExplanationDataList(explanationDataList: Collection<WordExplanationData>): List<WordRowData> {
        val words = ArrayList<WordRowData>(20)
        explanationDataList.forEach { explanationData ->
            words.add(getWordRowDataFromExplanation(explanationData)!!)
        }
        return words
    }

    fun deleteWord(word: String): Boolean {
        val filename = "${word}.txt"
        return wordsRepository.deleteWord(filename)
    }

    private fun getWordRowDataFromExplanation(explanationData: WordExplanationData): WordRowData? {
        val translations = StringBuilder()
        if (explanationData.definitions != null) {
            for (definition in explanationData.definitions) {
                translations.append(definition.meaning)
                translations.append(", ")
            }
            return WordRowData(
                explanationData.word!!, translations.dropLast(2).toString()
            )
        }
        return WordRowData(explanationData.word!!, "")
    }
}