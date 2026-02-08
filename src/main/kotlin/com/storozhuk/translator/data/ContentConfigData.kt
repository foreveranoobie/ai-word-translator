package com.storozhuk.translator.data

import com.google.genai.types.ThinkingConfig

data class ContentConfigData(
    val maxOutputTokens: Int = 0,
    val temperature: Float = 0.0f,
    val thinkingConfig: ThinkingConfig? = null,
    val topP: Float = 0.0f,
    val responseMimeType: String? = null
)
