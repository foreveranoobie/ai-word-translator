package com.storozhuk.translator.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.storozhuk.translator.TestUtils
import com.storozhuk.translator.data.WordDefinitionData
import com.storozhuk.translator.data.WordDefinitionExampleData
import com.storozhuk.translator.data.WordExplanationData
import com.storozhuk.translator.data.WordRowData
import com.storozhuk.translator.db.WordsRepository
import com.storozhuk.translator.entity.WordDefinitionDocument
import com.storozhuk.translator.entity.WordDefinitionExampleDocument
import com.storozhuk.translator.entity.WordExplanationDocument
import com.storozhuk.translator.mapper.WordExplanationMapper
import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import java.util.*

@ExtendWith(MockitoExtension::class)
class WordsServiceUnitTest {

    private val objectMapper = ObjectMapper()

    @Mock
    private lateinit var wordsRepository: WordsRepository

    @Mock
    private lateinit var agentClient: AgentClient

    @Mock
    private lateinit var wordExplanationMapper: WordExplanationMapper

    @InjectMocks
    private lateinit var sut: WordsService

    companion object {
        private const val TEST_DESCRIPTION_JSON = "test_description.json"
    }

    @Test
    fun shouldSaveWordAndReturnWordExplanation_whenExplainWord_givenAgentClientReturnsJsonContent() {
        // given
        val word = "test word"
        val testContent = TestUtils.readTestResourceFile(TEST_DESCRIPTION_JSON)

        val expectedWordExplanationData = getDefaultWordExplanationData()
        val expectedDocument = getDefaultWordExplanationDocument()

        // Mock the agent client to return the test JSON content
        val question = "Explain the word \"${word}\""
        `when`(agentClient.askAgentToExplain(question)).thenReturn(testContent)
        `when`(wordExplanationMapper.toDocument(any())).thenCallRealMethod()

        // when
        val actualResult = sut.explainWord(word)

        // then
        verify(wordsRepository).save(expectedDocument)
        assertEquals(expectedWordExplanationData, actualResult)
    }

    @Test
    fun shouldReturnDefaultWordExplanation_whenExplainWord_givenAgentClientReturnsNull() {
        // given
        val word = "unknown word"
        val question = "Explain the word \"${word}\""

        `when`(agentClient.askAgentToExplain(question)).thenReturn(null)

        val expectedWordExplanationData = WordExplanationData(null, word, "", null)

        // when
        val actualResult = sut.explainWord(word)

        // then
        assertEquals(expectedWordExplanationData, actualResult)
    }

    @Test
    fun shouldReturnTrue_whenExistsWord_givenRepositoryReturnsNonNull() {
        // given
        val word = "hello"
        `when`(wordsRepository.existsByWord(anyString())).thenReturn(true)

        val expectedWord = "hello"

        // when
        val result = sut.existsWord(word)

        // then
        assertTrue(result)
        verify(wordsRepository).existsByWord(expectedWord)
    }

    @Test
    fun shouldReturnFalse_whenExistsWord_givenRepositoryReturnsNull() {
        // given
        val word = "nonexistent"
        val expectedFilename = word
        `when`(wordsRepository.existsByWord(anyString())).thenReturn(false)

        // when
        val result = sut.existsWord(word)

        // then
        assertFalse(result)
        verify(wordsRepository).existsByWord(expectedFilename)
    }

    @Test
    fun shouldReturnListOfWordRowData_whenGetTranslationsFromExplanationDataList_givenListWith2Explanations() {
        // given
        val wordExplanation1 = WordExplanationData(
            word = "word1", language = "Lang1", definitions = mutableListOf(
                WordDefinitionData(
                    meaning = "Meaning1", usage = "Usage1", example = WordDefinitionExampleData(
                        english = "Example1", foreignLanguage = "Beispiel1"
                    )
                )
            )
        )

        val wordExplanation2 = WordExplanationData(
            word = "word2", language = "Lang2", definitions = mutableListOf(
                WordDefinitionData(
                    meaning = "Meaning2A", usage = "Usage2A", example = WordDefinitionExampleData(
                        english = "Example2A", foreignLanguage = "Beispiel2A"
                    )
                ), WordDefinitionData(
                    meaning = "Meaning2B", usage = "Usage2B", example = WordDefinitionExampleData(
                        english = "Example2B", foreignLanguage = "Beispiel2B"
                    )
                )
            )
        )

        val explanationDataList = listOf(wordExplanation1, wordExplanation2)

        val expectedList = listOf(
            WordRowData("word1", "Meaning1"), WordRowData("word2", "Meaning2A, Meaning2B")
        )

        // when
        val result = sut.getTranslationsFromExplanationDataList(explanationDataList)

        // then
        assertEquals(expectedList, result)
    }

    @Test
    fun shouldReturnTrue_whenDeleteWord_givenWordString() {
        // given
        val word = "hello"
        val expectedWordName = word
        `when`(wordsRepository.deleteByWord(anyString())).thenReturn(1)

        // when
        val result = sut.deleteWord(word)

        // then
        assertTrue(result)
        verify(wordsRepository).deleteByWord(expectedWordName)
    }

    @Test
    fun shouldReturnFalse_whenDeleteWord_givenRepositoryReturnsZero() {
        // given
        val word = "hello"
        val expectedWordName = word
        `when`(wordsRepository.deleteByWord(anyString())).thenReturn(0)

        // when
        val result = sut.deleteWord(word)

        // then
        assertFalse(result)
        verify(wordsRepository).deleteByWord(expectedWordName)
    }

    @Test
    fun shouldCallRepositorySaveWord_whenUpdateWord_givenWordExplanationData() {
        // given
        val id = ObjectId()
        val testContent = TestUtils.readTestResourceFile(TEST_DESCRIPTION_JSON)
        val givenWordExplanationData =
            objectMapper.readValue(testContent, WordExplanationData::class.java)
        givenWordExplanationData.id = id
        val document = WordExplanationDocument(id, null, null, null)
        `when`(wordsRepository.findById(any())).thenReturn(Optional.of(document))
        `when`(wordExplanationMapper.updateFromData(any(), any())).thenCallRealMethod()

        val expectedDocument =
            objectMapper.readValue(testContent, WordExplanationDocument::class.java)
        expectedDocument.id = id

        // when
        sut.updateWord(givenWordExplanationData)

        // then
        verify(wordsRepository).findById(id)
        verify(wordExplanationMapper).updateFromData(document, givenWordExplanationData)
        verify(wordsRepository).save(expectedDocument)
    }

    @Test
    fun shouldNotCallRepositorySaveWord_whenUpdateWord_givenEntityNotPresent() {
        // given
        val id = ObjectId()
        val testContent = TestUtils.readTestResourceFile(TEST_DESCRIPTION_JSON)
        val givenWordExplanationData =
            objectMapper.readValue(testContent, WordExplanationData::class.java)
        givenWordExplanationData.id = id
        `when`(wordsRepository.findById(any())).thenReturn(Optional.empty())

        // when
        sut.updateWord(givenWordExplanationData)

        // then
        verify(wordsRepository).findById(id)
        verify(wordExplanationMapper, never()).updateFromData(any(), any())
        verify(wordsRepository, never()).save(any())
    }

    @Test
    fun shouldCallRepositorySaveWord_whenSaveWord_givenNoEntityExists() {
        // given
        val word = "test word"
        val testContent = TestUtils.readTestResourceFile(TEST_DESCRIPTION_JSON)
        val expectedWordExplanationData = getDefaultWordExplanationData()
        val expectedDocument = getDefaultWordExplanationDocument()

        `when`(wordsRepository.existsByWord(any())).thenReturn(false)
        `when`(wordExplanationMapper.toDocument(any())).thenCallRealMethod()

        // when
        sut.saveWord(word, testContent)

        // then
        verify(wordsRepository).existsByWord(word)
        verify(wordExplanationMapper).toDocument(expectedWordExplanationData)
        verify(wordsRepository).save(expectedDocument)
    }

    @Test
    fun shouldNotCallRepositorySaveWord_whenSaveWord_givenEntityExists() {
        // given
        val word = "test word"
        val testContent = TestUtils.readTestResourceFile(TEST_DESCRIPTION_JSON)

        `when`(wordsRepository.existsByWord(any())).thenReturn(true)

        // when
        sut.saveWord(word, testContent)

        // then
        verify(wordsRepository).existsByWord(word)
        verify(wordExplanationMapper, never()).toDocument(any())
        verify(wordsRepository, never()).save(any())
    }

    @Test
    fun shouldReturnAllWordsWithContent_whenGetAllWordsWithContent_givenRepositoryHasTwoDocuments() {
        // given
        val id1 = ObjectId()
        val id2 = ObjectId()

        val firstDocument = WordExplanationDocument(
            id = id1, word = "word1", language = "Lang1", definitions = mutableListOf(
                WordDefinitionDocument(
                    meaning = "Meaning1", usage = "Usage1", example = WordDefinitionExampleDocument(
                        english = "Ex1", foreignLanguage = "Beispiel1"
                    )
                )
            )
        )

        val secondDocument = WordExplanationDocument(
            id = id2, word = "word2", language = "Lang2", definitions = mutableListOf(
                WordDefinitionDocument(
                    meaning = "Meaning2", usage = "Usage2", example = WordDefinitionExampleDocument(
                        english = "Ex2", foreignLanguage = "Beispiel2"
                    )
                )
            )
        )

        `when`(wordsRepository.findAll()).thenReturn(listOf(firstDocument, secondDocument))

        val expectedMap = mutableMapOf(
            "word1" to WordExplanationData(
                id = id1, word = "word1", language = "Lang1", definitions = mutableListOf(
                    WordDefinitionData(
                        meaning = "Meaning1", usage = "Usage1", example = WordDefinitionExampleData(
                            english = "Ex1", foreignLanguage = "Beispiel1"
                        )
                    )
                )
            ),
            "word2" to WordExplanationData(
                id = id2, word = "word2", language = "Lang2", definitions = mutableListOf(
                    WordDefinitionData(
                        meaning = "Meaning2", usage = "Usage2", example = WordDefinitionExampleData(
                            english = "Ex2", foreignLanguage = "Beispiel2"
                        )
                    )
                )
            )
        )

        // when
        val actualMap = sut.getAllWordsWithContent()

        // then
        assertEquals(expectedMap, actualMap)
        verify(wordsRepository).findAll()
    }

    private fun getDefaultWordExplanationData(): WordExplanationData {
        return WordExplanationData(
            word = "test word", language = "Lang", definitions = mutableListOf(
                WordDefinitionData(
                    meaning = "Meaning", usage = "Usage", example = WordDefinitionExampleData(
                        english = "Something", foreignLanguage = "Etwas"
                    )
                )
            )
        )
    }

    private fun getDefaultWordExplanationDocument(): WordExplanationDocument {
        return WordExplanationDocument(
            id = null, word = "test word", language = "Lang", definitions = mutableListOf(
                WordDefinitionDocument(
                    meaning = "Meaning", usage = "Usage", example = WordDefinitionExampleDocument(
                        english = "Something", foreignLanguage = "Etwas"
                    )
                )
            )
        )
    }
}
