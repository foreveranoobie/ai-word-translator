package com.storozhuk.translator.service

import com.storozhuk.translator.TestUtils
import com.storozhuk.translator.data.WordDefinitionData
import com.storozhuk.translator.data.WordDefinitionExampleData
import com.storozhuk.translator.data.WordExplanationData
import com.storozhuk.translator.data.WordRowData
import com.storozhuk.translator.db.WordsRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.Mockito.never
import org.mockito.junit.jupiter.MockitoExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertFalse

@ExtendWith(MockitoExtension::class)
class WordsServiceUnitTest {


    @Mock
    private lateinit var wordsRepository: WordsRepository
    @Mock
    private lateinit var agentClient: AgentClient

    @InjectMocks
    private lateinit var sut: WordsService

    @Test
    fun shouldCallRepositoryWithProperData_whenUpdateWord_givenWordAndExplanationStrings() {
        // given
        val word = "hello"
        val explanation = "a greeting"
        val expectedFilename = "hello.txt"

        // when
        sut.saveWord(word, explanation)

        // then
        verify(wordsRepository).saveWord(expectedFilename, explanation)
    }

    @Test
    fun shouldNotSaveWord_whenSaveWord_givenRepositoryReturnsNonNull() {
        // given
        val word = "hello"
        val explanation = "a greeting"
        val expectedFilename = "${word}.txt"
        `when`(wordsRepository.findWordByName(expectedFilename)).thenReturn("existing content")

        // when
        sut.saveWord(word, explanation)

        // then verify saveWord was not called
        verify(wordsRepository, never()).saveWord(expectedFilename, explanation)
    }

    @Test
    fun shouldCallRepositoryWithProperData_whenUpdateWord_givenWordExplanationData() {
        // given
        // Load expected content from test_description.json
        val expectedContent = TestUtils.readTestResourceFile("test_description.json")

        // Create WordExplanationData object directly
        val wordExplanationData = WordExplanationData(
            word = "test word",
            language = "Lang",
            definitions = mutableListOf(
                WordDefinitionData(
                    meaning = "Meaning",
                    usage = "Usage",
                    example = WordDefinitionExampleData(
                        english = "Something",
                        foreignLanguage = "Etwas"
                    )
                )
            )
        )
        val expectedFilename = "${wordExplanationData.word}.txt"

        // when
        sut.updateWord(wordExplanationData)

        // then
        verify(wordsRepository).saveWord(expectedFilename, expectedContent)
    }

    @Test
    fun shouldReturnMappedWordExplanationData_whenGetAllWordsWithContent_givenRepositoryReturnsOneEntry() {
        // given
        // Load expected content from test_description.json
        val testContent = TestUtils.readTestResourceFile("test_description.json")

        // Create expected WordExplanationData object directly
        val expectedWordExplanationData = WordExplanationData(
            word = "test word",
            language = "Lang",
            definitions = mutableListOf(
                WordDefinitionData(
                    meaning = "Meaning",
                    usage = "Usage",
                    example = WordDefinitionExampleData(
                        english = "Something",
                        foreignLanguage = "Etwas"
                    )
                )
            )
        )

        // Mock the repository to return a map with key "test" and the content from test_description.json
        val repositoryReturnMap = mapOf("test" to testContent)
        `when`(wordsRepository.getAllWordsWithContent()).thenReturn(repositoryReturnMap)


        // when
        val result = sut.getAllWordsWithContent()

        // then
        assertEquals(1, result.size)
        assertEquals(expectedWordExplanationData, result[expectedWordExplanationData.word!!])
    }

    @Test
    fun shouldSaveWordAndReturnWordExplanation_whenExplainWord_givenAgentClientReturnsJsonContent() {
        // given
        val word = "test word"
        val testContent = TestUtils.readTestResourceFile("test_description.json")
        val expectedFilename = "${word}.txt"

        val expectedWordExplanationData = WordExplanationData(
            word = "test word",
            language = "Lang",
            definitions = mutableListOf(
                WordDefinitionData(
                    meaning = "Meaning",
                    usage = "Usage",
                    example = WordDefinitionExampleData(
                        english = "Something",
                        foreignLanguage = "Etwas"
                    )
                )
            )
        )

        // Mock the agent client to return the test JSON content
        val question = "Explain the word \"${word}\""
        `when`(agentClient.askAgentToExplain(question)).thenReturn(testContent)

        // when
        val actualResult = sut.explainWord(word)

        // then
        verify(wordsRepository).saveWord(expectedFilename, testContent)
        assertEquals(expectedWordExplanationData, actualResult)
    }

    @Test
    fun shouldReturnDefaultWordExplanation_whenExplainWord_givenAgentClientReturnsNull() {
        // given
        val word = "unknown word"
        val question = "Explain the word \"${word}\""

        `when`(agentClient.askAgentToExplain(question)).thenReturn(null)

        val expectedWordExplanationData = WordExplanationData(word, "", null)

        // when
        val actualResult = sut.explainWord(word)

        // then
        assertEquals(expectedWordExplanationData, actualResult)
    }

    @Test
    fun shouldReturnTrue_whenExistsWord_givenRepositoryReturnsNonNull() {
        // given
        val word = "hello"
        val expectedFilename = "${word}.txt"
        `when`(wordsRepository.findWordByName(expectedFilename)).thenReturn("content")

        // when
        val result = sut.existsWord(word)

        // then
        assertTrue(result)
    }

    @Test
    fun shouldReturnFalse_whenExistsWord_givenRepositoryReturnsNull() {
        // given
        val word = "nonexistent"
        val expectedFilename = "${word}.txt"
        `when`(wordsRepository.findWordByName(expectedFilename)).thenReturn(null)

        // when
        val result = sut.existsWord(word)

        // then
        assertFalse(result)
    }

    @Test
    fun shouldReturnListOfWordRowData_whenGetTranslationsFromExplanationDataList_givenListWith2Explanations() {
        // given
        val wordExplanation1 = WordExplanationData(
            word = "word1",
            language = "Lang1",
            definitions = mutableListOf(
                WordDefinitionData(
                    meaning = "Meaning1",
                    usage = "Usage1",
                    example = WordDefinitionExampleData(
                        english = "Example1",
                        foreignLanguage = "Beispiel1"
                    )
                )
            )
        )

        val wordExplanation2 = WordExplanationData(
            word = "word2",
            language = "Lang2",
            definitions = mutableListOf(
                WordDefinitionData(
                    meaning = "Meaning2A",
                    usage = "Usage2A",
                    example = WordDefinitionExampleData(
                        english = "Example2A",
                        foreignLanguage = "Beispiel2A"
                    )
                ),
                WordDefinitionData(
                    meaning = "Meaning2B",
                    usage = "Usage2B",
                    example = WordDefinitionExampleData(
                        english = "Example2B",
                        foreignLanguage = "Beispiel2B"
                    )
                )
            )
        )

        val explanationDataList = listOf(wordExplanation1, wordExplanation2)

        val expectedList = listOf(
            WordRowData("word1", "Meaning1"),
            WordRowData("word2", "Meaning2A, Meaning2B")
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
        val expectedFilename = "${word}.txt"
        `when`(wordsRepository.deleteWord(expectedFilename)).thenReturn(true)

        // when
        val result = sut.deleteWord(word)

        // then
        assertTrue(result)
        verify(wordsRepository).deleteWord(expectedFilename)
    }
}
