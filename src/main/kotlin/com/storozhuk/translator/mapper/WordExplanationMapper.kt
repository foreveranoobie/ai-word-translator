package com.storozhuk.translator.mapper

import com.storozhuk.translator.data.WordDefinitionData
import com.storozhuk.translator.data.WordDefinitionExampleData
import com.storozhuk.translator.data.WordExplanationData
import com.storozhuk.translator.entity.WordDefinitionDocument
import com.storozhuk.translator.entity.WordDefinitionExampleDocument
import com.storozhuk.translator.entity.WordExplanationDocument
import org.springframework.stereotype.Component

@Component
class WordExplanationMapper {

    fun updateFromData(document: WordExplanationDocument, data: WordExplanationData): WordExplanationDocument {
        return document.copy(
            word = data.word,
            language = data.language,
            definitions = data.definitions?.map { definitionData ->
                toDefinitionDocument(definitionData)
            }?.toMutableList()
        )
    }

    fun toDocument(data: WordExplanationData): WordExplanationDocument {
        return WordExplanationDocument(
            id = data.id,
            word = data.word,
            language = data.language,
            definitions = data.definitions?.map { definitionData ->
                toDefinitionDocument(definitionData)
            }?.toMutableList()
        )
    }

    private fun toDefinitionDocument(data: WordDefinitionData): WordDefinitionDocument {
        return WordDefinitionDocument(
            meaning = data.meaning,
            usage = data.usage,
            example = data.example?.let { exampleData ->
                toExampleDocument(exampleData)
            }
        )
    }

    private fun toExampleDocument(data: WordDefinitionExampleData): WordDefinitionExampleDocument {
        return WordDefinitionExampleDocument(
            english = data.english,
            foreignLanguage = data.foreignLanguage
        )
    }
}

