package com.storozhuk.translator.db

import com.storozhuk.translator.entity.WordExplanationDocument
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface WordsRepository : MongoRepository<WordExplanationDocument, ObjectId> {
    fun findByWord(word: String): WordExplanationDocument?
    fun findByWordAndLanguage(word: String, language: String): WordExplanationDocument?
    fun findAllByLanguage(language: String): List<WordExplanationDocument>
    fun deleteByWord(word: String): Long
    fun existsByWord(word: String): Boolean
}