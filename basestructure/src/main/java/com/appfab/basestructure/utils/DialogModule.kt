package com.appfab.basestructure.utils

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.OnLifecycleEvent
import com.appfab.basestructure.base.BaseActivity
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.okButton
import java.lang.ref.WeakReference

class DialogModule : LifecycleObserver {

    private var dialog: android.app.AlertDialog? = null
    private var alertText: String? = null
    private var cancelButtonAction: (() -> Unit)? = null
    private var okButtonAction: (() -> Unit)? = null
    private var neutralButtonAction: (() -> Unit)? = null
    private var lifecycleRegistry: LifecycleRegistry? = null
    private var context: WeakReference<Context>? = null
    private var isCancelable: Boolean = false
    private var okButtonText: String? = null
    private var cancelButtonText: String? = null
    private var neutralButtonText: String? = null

    fun setLifeCycleRegistry(lifecycleRegistry: LifecycleRegistry) {
        this.lifecycleRegistry = lifecycleRegistry
        this.lifecycleRegistry?.addObserver(this)
    }

    /**
     * Easy way to change text
     *
     * @param text
     */
    fun setText(text: String) {
        this.alertText = text
    }

    /**
     * Easy way to set cancel button action to click cancel button
     *
     * @param cancelBtnAction
     */
    fun setCancelAction(cancelBtnAction: () -> Unit) {
        this.cancelButtonAction = cancelBtnAction
    }

    /**
     * Easy way to set ok button action to click ok button
     *
     * @param okBtnAction
     */
    fun setOkAction(okBtnAction: () -> Unit) {
        this.okButtonAction = okBtnAction
    }

    fun setNeutralAction(neutralBtnAction: () -> Unit) {
        this.neutralButtonAction = neutralBtnAction
    }

    /**
     * set function is settings for dialog. You can customize buttons action and text
     *
     * @param text
     * @param lifecycleRegistry
     * @param cancelButtonAction
     * @param okButtonAction
     * @return [DialogModule]
     */
    fun set(
        text: String,
        lifecycleRegistry: LifecycleRegistry? = null,
        isCancelable: Boolean? = false,
        cancelButtonText: String? = null,
        cancelButtonAction: (() -> Unit)? = null,
        okButtonText: String? = null,
        okButtonAction: (() -> Unit)? = null,
        neutralButtonText: String? = null,
        neutralButtonAction: (() -> Unit)? = null

    ): DialogModule {
        setText(text)
        this.okButtonText = okButtonText
        this.cancelButtonText = cancelButtonText
        this.neutralButtonText = neutralButtonText
        lifecycleRegistry?.let { setLifeCycleRegistry(it) }
        cancelButtonAction?.let { setCancelAction(it) }

        isCancelable?.let { this.isCancelable = it }
        okButtonAction?.let {
            setOkAction(okButtonAction)
        }
        neutralButtonAction?.let {
            setNeutralAction(neutralButtonAction)
        }

        return this
    }

    /**
     * This method lets you show Dialog
     *
     * @param context required for lifecycle and showDialog alert dialog
     */
    fun show(context: WeakReference<Context>) {
        this.context = context
        if ((lifecycleRegistry?.observerCount ?: 0 == 0)) {
            lifecycleRegistry = if (context.get() is BaseActivity<*>) {
                (context.get() as BaseActivity<*>).lifecycleRegistry
            } else null
        }
        with(this) {
            if (dialog == null || dialog?.isShowing == false) {
                dialog = context.get()?.alert(alertText.toString(), "") {
                    if (okButtonText != null) {
                        positiveButton(okButtonText!!) {
                            dismiss()
                            okButtonAction?.invoke()
                        }
                    } else {
                        okButton {
                            dismiss()
                            okButtonAction?.invoke()
                        }
                    }
                    if (cancelButtonAction != null) {
                        if (cancelButtonText != null) {
                            negativeButton(cancelButtonText!!) {
                                dismiss()
                                cancelButtonAction?.invoke()
                            }
                        } else {
                            noButton {
                                dismiss()
                                dialog = null
                                cancelButtonAction?.let { action -> action() }
                            }
                        }
                    }
                    if (neutralButtonText != null) {
                        neutralPressed(neutralButtonText!!) {
                            dismiss()
                            neutralButtonAction?.invoke()
                        }
                    } else {
                        okButton {
                            dismiss()
                            okButtonAction?.invoke()
                        }
                    }
                }?.build()?.apply {
                    setCancelable(isCancelable)
                    setCanceledOnTouchOutside(isCancelable)
                    show().takeIf { context.get()?.getActivity()?.isFinishing == false }.apply {

                    }

                }
            }
        }
    }

    /**
     * Dismiss method lets you dismissDialog the opened dialog
     *
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun dismiss() {
        dialog?.safelyDismiss()
        dialog = null
        context?.clear()
        lifecycleRegistry?.removeObserver(this)
    }

}