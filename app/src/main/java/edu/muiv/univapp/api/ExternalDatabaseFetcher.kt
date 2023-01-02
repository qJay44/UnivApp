package edu.muiv.univapp.api

import com.mobprofs.retrofit.converters.SimpleXmlConverter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ExternalDatabaseFetcher {

    companion object {
        private const val TAG = "ExternalDatabaseFetcher"
    }

    private val externalDatabaseApi: ExternalDatabaseApi

    init {
        val retrofit = Retrofit.Builder()
            // TODO: Add base url
            .baseUrl("base url here")
            .addConverterFactory(SimpleXmlConverterFactory.)
            .build()

        externalDatabaseApi = retrofit.create(ExternalDatabaseApi::class.java)
    }
}
