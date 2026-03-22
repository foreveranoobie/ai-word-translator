package com.storozhuk.translator.entity

data class WordDefinitionDocument(
    val meaning: String? = null,
    val usage: String? = null,
    val example: WordDefinitionExampleDocument? = null
)