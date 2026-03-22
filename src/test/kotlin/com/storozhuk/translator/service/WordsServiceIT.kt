package com.storozhuk.translator.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.storozhuk.translator.TestUtils
import com.storozhuk.translator.data.WordDefinitionData
import com.storozhuk.translator.data.WordDefinitionExampleData
import com.storozhuk.translator.data.WordExplanationData
import com.storozhuk.translator.db.WordsRepository
import com.storozhuk.translator.entity.WordExplanationDocument
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import kotlin.math.exp

@SpringBootTest
@ActiveProfiles("test")
class WordsServiceIT(
    @Autowired private val wordsService: WordsService,
    @Autowired private val wordsRepository: WordsRepository,
    @Autowired private val mongoTemplate: MongoTemplate
) {

    @MockitoBean
    lateinit var agentClient: AgentClient

    private val objectMapper = ObjectMapper()

    @BeforeEach
    fun setUp() {
        wordsRepository.deleteAll()
        mongoTemplate.dropCollection(WordExplanationDocument::class.java)
    }

    @Test
    fun shouldSaveWordToDatabase_whenSaveWord_givenWordDoesNotExist() {
        // given
        val givenWord = "test word"
        val givenContent = TestUtils.readTestResourceFile("test_description.json")
        val expectedDocument =
            objectMapper.readValue(givenContent, WordExplanationDocument::class.java)

        // when
        wordsService.saveWord(givenWord, givenContent)

        // then
        val savedWord = wordsRepository.findByWord(givenWord)
        assertThat(savedWord).usingRecursiveComparison().ignoringFields("id").isEqualTo(expectedDocument)
    }

    @Test
    fun shouldNotSaveWord_whenSaveWord_givenWordAlreadyExists() {
        // given
        addTestWord()
        val givenWord = "test word"
        val givenContent = TestUtils.readTestResourceFile("test_description_upd.json")

        val expectedDocument = objectMapper.readValue(TestUtils.readTestResourceFile("test_description.json"), WordExplanationDocument::class.java)

        // when
        wordsService.saveWord(givenWord, givenContent)

        // then
        assertThat(wordsRepository.findAll().size).isEqualTo(1)
        assertThat(wordsRepository.findByWord(givenWord)).usingRecursiveComparison().ignoringFields("id").isEqualTo(expectedDocument)
    }

    @Test
    fun shouldExistWordReturnTrue_whenExistsWord_givenWordExists() {
        // given
        addTestWord()
        val givenWord = "test word"

        // when
        val exists = wordsService.existsWord(givenWord)

        // then
        assertThat(exists).isTrue
    }

    @Test
    fun shouldExistWordReturnFalse_whenExistsWord_givenWordDoesNotExist() {
        // when
        val exists = wordsService.existsWord("nonexistent word")

        // then
        assertThat(exists).isFalse
    }

    @Test
    fun shouldDeleteWord_whenDeleteWord_givenWordExists() {
        // given
        val givenWord = "test word"
        addTestWord()

        // when
        val deleted = wordsService.deleteWord(givenWord)

        // then
        assertThat(deleted).isTrue
        assertThat(wordsService.existsWord(givenWord)).isFalse
    }

    @Test
    fun shouldReturnAllWordsWithContent_whenGetAllWordsWithContent() {
        // given
        addTestWord()
        addSecondTestWord()

        val expectedFirstWordExplanation = objectMapper.readValue(TestUtils.readTestResourceFile("test_description.json"),
            WordExplanationData::class.java)
        val expectedSecondWordExplanation = objectMapper.readValue(TestUtils.readTestResourceFile("test_description2.json"),
            WordExplanationData::class.java)

        val expectedWordsMap = HashMap<String, WordExplanationData>()
        expectedWordsMap["test word"] = expectedFirstWordExplanation
        expectedWordsMap["test word2"] = expectedSecondWordExplanation

        // when
        val result = wordsService.getAllWordsWithContent()

        // then
        assertThat(result).hasSize(2).usingRecursiveComparison().ignoringFieldsMatchingRegexes(".*\\.id").isEqualTo(expectedWordsMap)
    }

    @Test
    fun shouldUpdateWord_whenUpdateWord_givenWordExists() {
        // given
        addTestWord()

        val givenUpdateWord = objectMapper.readValue(TestUtils.readTestResourceFile("test_description_upd.json"),
            WordExplanationData::class.java)

        val word = "test word"

        val expectedWord = objectMapper.readValue(TestUtils.readTestResourceFile("test_description_upd.json"),
            WordExplanationData::class.java)

        val id = wordsRepository.findByWord(word)?.id
        givenUpdateWord.id = id

        // when
        wordsService.updateWord(givenUpdateWord)

        // then
        val actualWord = wordsRepository.findByWord(word)
        assertThat(actualWord).usingRecursiveComparison().ignoringFields("id").isEqualTo(expectedWord)
    }

    private fun addTestWord(){
        val content = objectMapper.readValue(TestUtils.readTestResourceFile("test_description.json"), WordExplanationDocument::class.java)
        mongoTemplate.save(content)
    }

    private fun addSecondTestWord(){
        val content = objectMapper.readValue(TestUtils.readTestResourceFile("test_description2.json"), WordExplanationDocument::class.java)
        mongoTemplate.save(content)
    }
}

