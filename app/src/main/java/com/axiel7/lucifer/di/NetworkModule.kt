package com.axiel7.lucifer.di

import com.axiel7.lucifer.data.network.Api
import com.axiel7.lucifer.data.network.JikanApi
import com.axiel7.lucifer.data.network.ktorHttpClient
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val networkModule = module {
    single { ktorHttpClient }
    singleOf(::Api)
    singleOf(::JikanApi)
}