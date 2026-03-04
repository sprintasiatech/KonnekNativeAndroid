package com.example.appsample1.support

import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class KonnekService {
    val gson = Gson()

    fun getConfig(
        clientIdValue: String,
        onSuccess: (data: String) -> Unit?,
        onFailed: (errorMessage: String) -> Unit?,
    ) {
        try {
            val clientId: String = clientIdValue
            val platform: String = "android"

            if (clientId == "") {
                onFailed.invoke("empty params")
                return
            }

            CoroutineScope(Dispatchers.IO).launch {
                val apiService = ApiConfig.provideApiService()
                val response = apiService.getConfig(
                    clientId = clientId,
                    platform = platform,
                )
                if (response.isSuccessful) {

                    val data = response.body()
                    val json: String = gson.toJson(data)
                    withContext(Dispatchers.Main) { // to the main thread for UI update
                        onSuccess.invoke(json)
                    }
                } else {
                    onFailed.invoke(response.message())
                }
            }
        } catch (e: Exception) {
            onFailed.invoke(e.toString())
            return
        }
    }
}