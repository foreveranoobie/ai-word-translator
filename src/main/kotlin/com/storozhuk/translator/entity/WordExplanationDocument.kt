package com.storozhuk.translator.entity

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "word_explanations")
data class WordExplanationDocument(
    @Id var id: ObjectId? = null,
    @Indexed val word: String? = null,
    @Indexed val language: String? = null,
    val definitions: MutableList<WordDefinitionDocument>? = null
)