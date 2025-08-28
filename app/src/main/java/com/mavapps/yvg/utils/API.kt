package com.mavapps.yvg.utils

import com.google.ai.client.generativeai.GenerativeModel

object genModel{
    val apiKey = "AIzaSyBnIg70YEYczJ7Y34USIP_3XUyd_o_rGY4"
    val modelName = "gemini-2.0-flash"
    val generativeModel = GenerativeModel(
        modelName = modelName,
        apiKey = apiKey
    )
}