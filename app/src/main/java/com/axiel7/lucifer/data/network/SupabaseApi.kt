package com.axiel7.lucifer.data.network

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseApi {
    // We pulled your specific project URL from your screenshot
    private const val SUPABASE_URL = "https://iyutfgxurhinevsocwbn.supabase.co"

    // TODO: Paste your actual "Publishable key" from your Supabase dashboard here
    private const val SUPABASE_KEY = "sb_publishable_5NdeqnacuJIQgkK9nXcAKg_G1cICMyp"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Postgrest)
    }
}