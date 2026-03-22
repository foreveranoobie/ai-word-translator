package com.storozhuk.translator.data

import com.fasterxml.jackson.annotation.JsonIgnore
import org.bson.types.ObjectId

/**
 * Represents the structure of an agent response
 */
data class WordExplanationData(
    @JsonIgnore var id: ObjectId? = null,
    val word: String? = null,
    val language: String? = null,
    var definitions: MutableList<WordDefinitionData>? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WordExplanationData

        if (id != other.id) return false
        if (word != other.word) return false
        if (language != other.language) return false
        if (definitions != null) {
            if (definitions != other.definitions) return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + word.hashCode()
        result = 31 * result + language.hashCode()
        result = 31 * result + definitions.hashCode()
        return result
    }
}