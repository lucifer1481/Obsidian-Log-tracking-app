package com.axiel7.lucifer.data.model

import kotlinx.serialization.SerialName

interface BaseResponse {
    @SerialName("error")
    val error: String?

    @SerialName("message")
    val message: String?
}