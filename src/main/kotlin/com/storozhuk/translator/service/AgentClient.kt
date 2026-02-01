package com.storozhuk.translator.service

import com.google.genai.Client
import com.google.genai.types.GenerateContentConfig
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service


@Service
class AgentClient {

    private lateinit var client: Client;

    @PostConstruct
    fun initAgentClient(){
        System.err.println("GEMINI_API_KEY=${System.getenv("GEMINI_API_KEY")}")
        client = Client()
    }

    fun askAgentToExplain(word: String): String? {
        val response =
            client.models.generateContent(
                "gemini-3-flash-preview",
                "Explain how AI works in a few words",
                null
            )
        System.err.println(response.text())
        return response.text()
    }
}