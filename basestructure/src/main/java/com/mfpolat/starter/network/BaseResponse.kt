package com.mfpolat.starter.network

import com.mfpolat.starter.network.ApiErrorResponse

open class BaseResponse : ApiErrorResponse() {
    var message: String? = null
}

