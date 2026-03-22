package com.storozhuk.translator.service

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.storozhuk.translator.data.WordDefinitionData
import com.storozhuk.translator.data.WordDefinitionExampleData
import com.storozhuk.translator.data.WordExplanationData
import com.storozhuk.translator.data.WordRowData
import com.storozhuk.translator.db.WordsRepository
import com.storozhuk.translator.mapper.WordExplanationMapper
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class WordsService(
    @Autowired private val wordsRepository: WordsRepository,
    @Autowired private val agentClient: AgentClient,
    @Autowired private val wordExplanationMapper: WordExplanationMapper
) {

    private val objectMapper: ObjectMapper = ObjectMapper()

    @PostConstruct
    fun init() {
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
    }

    fun saveWord(word: String, explanation: String) {
        if (!existsWord(word)) {
            val wordExplanationData =
                objectMapper.readValue(explanation, WordExplanationData::class.java)
            val entity = wordExplanationMapper.toDocument(wordExplanationData)
            wordsRepository.save(entity)
        }
    }

    fun existsWord(word: String): Boolean {
        return wordsRepository.existsByWord(word)
    }

    fun updateWord(wordExplanationData: WordExplanationData) {
        val entity = wordsRepository.findById(wordExplanationData.id!!)
        entity.ifPresent {
            val updatedEntity = wordExplanationMapper.updateFromData(it, wordExplanationData)
            wordsRepository.save(updatedEntity)
        }
    }

    fun getAllWordsWithContent(): MutableMap<String, WordExplanationData> {
        return wordsRepository.findAll().associate { document ->
            val data = WordExplanationData(
                id = document.id,
                word = document.word,
                language = document.language,
                definitions = document.definitions?.map { definitionDocument ->
                    WordDefinitionData(
                        meaning = definitionDocument.meaning,
                        usage = definitionDocument.usage,
                        example = definitionDocument.example?.let { exampleDocument ->
                            WordDefinitionExampleData(
                                english = exampleDocument.english,
                                foreignLanguage = exampleDocument.foreignLanguage
                            )
                        }
                    )
                }?.toMutableList()
            )
            document.word!! to data
        }.toMutableMap()
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
        return WordExplanationData(null, word, "", null)
    }

    fun getTranslationsFromExplanationDataList(explanationDataList: Collection<WordExplanationData>): List<WordRowData> {
        val words = ArrayList<WordRowData>(20)
        explanationDataList.forEach { explanationData ->
            words.add(getWordRowDataFromExplanation(explanationData)!!)
        }
        return words
    }

    fun deleteWord(word: String): Boolean {
        return wordsRepository.deleteByWord(word) > 0
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