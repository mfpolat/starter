package com.appfab.basestructure.base

import android.content.Intent
import android.widget.Toast

interface BaseNavigator {

    fun requestReload() {}
    fun toast(toastMessage: String, length: Int = Toast.LENGTH_LONG) {}
    fun unhandledError(message: String? = "") {}
    fun showLoader() {}
    fun dismissLoader() {}
    fun isLoaderVisible(): Boolean {
        return false
    }

    fun forceLogout(intent: Intent? = null) {}

    fun askUser(
        message: String,
        yesText: String? = null,
        noText: String? = null,
        yesClickListener: () -> Unit = {},
        noClickListener: () -> Unit = {},
        neutralText: String? = null,
        neutralClickListener: () -> Unit = {},
        isCancelable: Boolean = true
    ) {
    }
}