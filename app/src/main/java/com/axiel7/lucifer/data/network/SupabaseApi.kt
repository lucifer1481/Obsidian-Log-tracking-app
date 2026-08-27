package com.axiel7.lucifer.data.network

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseApi {
    private const val SUPABASE_URL = "https://iyutfgxurhinevsocwbn.supabase.co"
    private const val SUPABASE_KEY = "sb_publishable_5NdeqnacuJIQgkK9nXcAKg_G1cICMyp"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Auth) {
            scheme = "lucifer"
            host = "login-callback"
        }
        install(Postgrest)
    }
}