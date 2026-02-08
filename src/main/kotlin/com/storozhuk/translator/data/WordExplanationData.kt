package com.storozhuk.translator.data

/**
 * Represents the structure of an agent response
 */
data class WordExplanationData(
    val word: String? = null,
    val language: String? = null,
    val definitions: Array<WordDefinitionData>? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WordExplanationData

        if (word != other.word) return false
        if (language != other.language) return false
        if (!definitions.contentEquals(other.definitions)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = word.hashCode()
        result = 31 * result + language.hashCode()
        result = 31 * result + definitions.contentHashCode()
        return result
    }
}