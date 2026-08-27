package com.axiel7.lucifer.di

import com.axiel7.lucifer.data.repository.AnimeRepository
import com.axiel7.lucifer.data.repository.DefaultPreferencesRepository
import com.axiel7.lucifer.data.repository.ExternalMediaRepository
import com.axiel7.lucifer.data.repository.LoginRepository
import com.axiel7.lucifer.data.repository.MangaRepository
import com.axiel7.lucifer.data.repository.UserRepository
import com.axiel7.lucifer.data.repository.SupabaseRepository // 🚀 Added import
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val repositoryModule = module {
    single { DefaultPreferencesRepository(get(named(DEFAULT_DATA_STORE))) }
    singleOf(::AnimeRepository)
    singleOf(::MangaRepository)
    singleOf(::LoginRepository)
    singleOf(::UserRepository)
    singleOf(::SupabaseRepository)
    singleOf(::ExternalMediaRepository)// 🚀 Added Supabase here!
}