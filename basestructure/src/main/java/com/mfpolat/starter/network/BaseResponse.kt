package com.mfpolat.starter.network

import com.appfab.cozgec.network.ApiErrorResponse

open class BaseResponse : ApiErrorResponse() {
    var message: String? = null
}

