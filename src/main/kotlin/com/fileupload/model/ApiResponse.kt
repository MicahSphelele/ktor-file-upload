package com.fileupload.model

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse(val statusCode: Int, val message: String)
