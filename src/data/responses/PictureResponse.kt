package com.fares.train.data.responses

import java.net.URI

data class PictureResponse(
    val isSuccessful: Boolean,
    val pictures: List<String>?
)