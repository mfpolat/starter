package com.appfab.cozgec.network


open class ApiErrorResponse {
    constructor(
            type: String? = null,
            title: String? = null,
            status: Int? = null,
            invalid_params: List<InvalidParam>? = null,
            detail: String? = null,
            instance: String? = null
    ) {
        this.detail = detail
        this.instance = instance
        this.invalid_params = invalid_params
        this.status = status
        this.title = title
        this.type = type
    }

    var type: String? = null
    var title: String? = null
    var status: Int? = null
    var invalid_params: List<InvalidParam>? = null
    var detail: String? = null
    var instance: String? = null
}

open class InvalidParam {
    constructor(
            key: String? = null,
            messages: List<String>? = null
    ) {
        this.key = key
        this.messages = messages
    }

    var key: String? = null
    var messages: List<String>? = listOf()
}

