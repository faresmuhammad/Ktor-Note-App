package com.fares.train.data.requests


data class AddPictureRequest(
    val noteId: String,
    val pictures: List<ByteArray>
)