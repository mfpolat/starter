package com.mfpolat.starter.utils

import android.content.Context
import androidx.lifecycle.LifecycleRegistry
import org.koin.core.KoinComponent
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import java.lang.ref.WeakReference

interface DialogListener : KoinComponent {
    /**
     * This progress for only set config in [DialogModule]. You can change some custom properties
     * in this function
     */

    private val dialogScope: Scope
        get() = getKoin().getOrCreateScope("scope1", named("dialog_scope"))

    val dialog: DialogModule
        get() = dialogScope.get()

    /**
     * Simple way to show dialog
     *
     * @param lifecycleRegistry is not required. Retrieving over context in extended Base class.
     * We recommend set lifecycleRegistry if you don't extend any Ares base classes
     * @receiver it must be activity [Context]
     */
    fun Context.showDialog(lifecycleRegistry: LifecycleRegistry? = null) {
        dialog.show(WeakReference(this))
    }

    /**
     * Simple way to dismiss dialog
     *
     * @param killDialogInstance is boolean and if you dont want to kill single instance which
     * running once in activity, param must be false. (not always singleton, only within specified scope)
     */
    fun dismissDialog(killDialogInstance: Boolean = true) {
        dialog.dismiss()
        if (killDialogInstance) {
            dialogScope.close()
        }
    }

}