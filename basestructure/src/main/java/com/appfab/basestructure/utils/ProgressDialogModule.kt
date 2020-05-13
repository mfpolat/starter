package com.appfab.basestructure.utils

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.ColorRes
import androidx.annotation.IntegerRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.OnLifecycleEvent
import com.appfab.basestructure.R
import com.appfab.basestructure.base.BaseActivity
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.Appcompat
import java.lang.ref.WeakReference

class ProgressDialogModule : LifecycleObserver {
    private var lifecycleRegistry: LifecycleRegistry? = null
    var alert: AlertBuilder<AlertDialog>? = null
    private var alertBuilder: AlertDialog? = null
    private var progressBackgroundAlpha: Int = 220
    private var progressBackgroundColor: Int = R.color.colorPrimaryDark
    private var progressBarColor: Int = R.color.colorAccent
    private var backgroundWindowSize: Int = 100
    private var progressBarPadding: Int = 20
    private var context: WeakReference<Context>? = null
    private var isCancelable:Boolean = false

    /**
     * setConfig function is settings for progress dialog. If you want to customize then you change color and padding settings.
     *
     * @sample
     *
     * @param backgroundColor If you wanna change background color set this param. Default is R.color.colorPrimaryDark in Style File
     * @param backgroundAlpha The alpha channel value: 0x0..0xFF or you can set from range 0..255 default is 220
     * @param backgroundWindowSize This field is the back of the ProgressBar. Default is 100.
     * @param progressBarColor ProgressBar color only work Build Version 21 and above. Default is R.color.colorAccent in Style File
     * @param progressBarPadding This param specifies the blank area around progressbar. Default is 20
     */
    fun setConfig(
        lifecycleRegistry: LifecycleRegistry? = null,
        isCancelable: Boolean? = false,
        @ColorRes
        backgroundColor: Int? = null,
        @IntegerRes
        backgroundAlpha: Int? = null,
        @IntegerRes
        backgroundWindowSize: Int? = null,
        @ColorRes
        progressBarColor: Int? = null,
        @IntegerRes
        progressBarPadding: Int? = null
    ) {
        lifecycleRegistry?.let { this.lifecycleRegistry = it }
        isCancelable?.let { this.isCancelable = it }
        backgroundColor?.let { progressBackgroundColor = it }
        progressBarColor?.let { this.progressBarColor = it }
        backgroundAlpha?.let { progressBackgroundAlpha = it }
        backgroundWindowSize?.let { this.backgroundWindowSize = it }
        progressBarPadding?.let { this.progressBarPadding = it }

    }

    /**
     * This method lets you show progress
     *
     */
    fun show(context: WeakReference<Context>) {
        this.context = context
        if ((lifecycleRegistry?.observerCount?:0 == 0)) {
            lifecycleRegistry = if (context.get() is BaseActivity<*>) (context.get() as BaseActivity<*>).lifecycleRegistry else null
        }
        lifecycleRegistry?.addObserver(this)
        alert = context.get()?.alert(Appcompat) {
            customView {
                linearLayout {
                    gravity = Gravity.CENTER
                    backgroundResource = R.drawable.rounded_corners
                    backgroundDrawable?.let {
                        DrawableCompat.setTint(
                            it,
                            ResourcesCompat.getColor(resources, progressBackgroundColor, null).withAlpha(
                                progressBackgroundAlpha
                            )
                        )
                    }
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    progressBar {
                        //                        backgroundColor = Color.BLACK  MARK: Maybe later this add for change border color etc.
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            indeterminateTintList =
                                ColorStateList.valueOf(ResourcesCompat.getColor(resources, progressBarColor, null))
                        }
                        padding = dip(progressBarPadding)
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                }
            }
        }
        alert?.isCancelable = this.isCancelable
        alertBuilder = alert?.show()
        val windowSize = context.get()?.dip(backgroundWindowSize)
        windowSize?.let {
            alertBuilder?.window?.setLayout(it, it)
        }
        alertBuilder?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }


    /**
     * If you set lifecycle, you prevent memory leak. This provides if you forget to close the Progress Dialog,
     * it will automatically close the on stop method.
     * NOTICE: If your activity extended [AresActivity] its not necessary to set it, you don't have to use.
     *
     * @param lifecycleRegistry The default registry is in [AresActivity].
     */
    fun setLifeCycleRegistry(lifecycleRegistry: LifecycleRegistry) {
        this.lifecycleRegistry = lifecycleRegistry
    }

    /**
     * Dismiss method lets you dismiss the opened progress
     *
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun dismiss() {
        alertBuilder?.safelyDismiss()
        alertBuilder = null
        context?.clear()
        lifecycleRegistry?.removeObserver(this)
    }
}