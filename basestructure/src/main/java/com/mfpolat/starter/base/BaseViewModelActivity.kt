package com.mfpolat.starter.base

import android.os.Bundle
import androidx.databinding.ViewDataBinding
import com.appfab.basestructure.R
import com.mfpolat.starter.utils.DialogListener
import com.tapadoo.alerter.Alerter
import com.tapadoo.alerter.OnHideAlertListener
import java.lang.ref.WeakReference

abstract class BaseViewModelActivity<T : ViewDataBinding, N : BaseNavigator, V : BaseViewModel<N>> :
    BaseActivity<T>(), BaseNavigator, DialogListener {

    abstract val vm: V
    abstract override fun getLayoutResId(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm.start(this as N)

    }

    override fun needToChangeView(reason: String?, result: String?) {
        super.needToChangeView(reason, result)
        vm.notifyChange()
    }

    override fun unhandledError(message: String?) {
        message?.let {
            showPopup(it, true)
        } ?: showPopup(resources.getString(R.string.DefaultErrorDescription), true)
    }

    fun showPopup(message: String, fatal: Boolean = false) {
        dialog.set(text = message, lifecycleRegistry = lifecycleRegistry) {
            if (fatal)
                finish()
        }
        dialog.show(WeakReference(this@BaseViewModelActivity))
    }

    fun showAlert(
        title: String = "",
        message: String? = "",
        isError: Boolean = false,
        onCloseFunc: () -> Unit = {}
    ) {

        val alterColor =
            if (isError) resources.getColor(R.color.errorColor) else resources.getColor(R.color.successColor)
        val alertMessage = if (message.isNullOrEmpty()) "" else message
        Alerter.create(this@BaseViewModelActivity)
            .setTitle(title)
            .setText(alertMessage)
            .setDuration(1000)
            .setBackgroundColorInt(alterColor)
            .setOnHideListener(object : OnHideAlertListener {
                override fun onHide() {
                    onCloseFunc()
                }
            })
            .show()

    }

    override fun askUser(
        message: String,
        yesText: String?,
        noText: String?,
        yesClickListener: () -> Unit,
        noClickListener: () -> Unit,
        neutralText: String?,
        neutralClickListener: () -> Unit,
        isCancelable: Boolean
    ) {
        dialog.set(
            text = message,
            isCancelable = isCancelable,
            lifecycleRegistry = lifecycleRegistry,
            okButtonText = yesText,
            cancelButtonText = noText,
            cancelButtonAction = { noClickListener() },
            okButtonAction = { yesClickListener() },
            neutralButtonAction = neutralClickListener,
            neutralButtonText = neutralText
        )
        dialog.show(WeakReference(this@BaseViewModelActivity))
    }
}