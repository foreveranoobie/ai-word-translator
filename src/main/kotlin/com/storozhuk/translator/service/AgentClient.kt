package com.storozhuk.translator.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.io.Resources
import com.google.genai.Client
import com.google.genai.types.GenerateContentConfig
import com.google.genai.types.Schema
import com.storozhuk.translator.data.ContentConfigData
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.nio.charset.Charset

@Service
class AgentClient {

    private val logger = LoggerFactory.getLogger(AgentClient::class.java)

    private lateinit var client: Client;
    private lateinit var config: GenerateContentConfig;

    @PostConstruct
    fun initAgentClient() {
        initializeConfig()
        client = Client()
    }

    fun askAgentToExplain(question: String): String? {
        val response =
            client.models.generateContent(
                "gemini-3-flash-preview",
                question,
                config
            )
        return response.text()
    }

    private fun initializeConfig() {
        val schemaStr = Resources.toString(
            Resources.getResource("response_structure.json"),
            Charset.defaultCharset()
        )
        val objectMapper = ObjectMapper()
        val schema: Schema = objectMapper.readValue(schemaStr, Schema::class.java)
        logger.info("Uploaded schema $schema")
        val contents = Resources.toString(
            Resources.getResource("content_config.json"),
            Charset.defaultCharset()
        )
        val contentConfigData = objectMapper.readValue(contents, ContentConfigData::class.java)
        config = GenerateContentConfig.builder()
            .maxOutputTokens(contentConfigData.maxOutputTokens)
            .temperature(contentConfigData.temperature)
            .thinkingConfig(contentConfigData.thinkingConfig)
            .topP(contentConfigData.topP)
            .responseMimeType(contentConfigData.responseMimeType)
            .responseSchema(schema)
            .build()
        logger.info("Uploaded agent config $config")
    }
}