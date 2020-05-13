package com.appfab.basestructure.utils

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.telephony.TelephonyManager
import android.telephony.TelephonyManager.PHONE_TYPE_CDMA
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.AnyRes
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.appfab.basestructure.R
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.jetbrains.anko.activityManager
import org.jetbrains.anko.find
import org.jetbrains.anko.windowManager
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.lang.reflect.InvocationTargetException
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile


/**
 * Created by Umut ADALI on 19.08.2018.
 */

inline fun <T : View> T.afterMeasured(crossinline f: T.() -> Unit) {
    viewTreeObserver?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
        override fun onGlobalLayout() {
            if (measuredWidth > 0 && measuredHeight > 0) {
                viewTreeObserver?.removeOnGlobalLayoutListener(this)
                try {
                    f()
                } catch (e: Exception) {
//                    com.orhanobut.logger.Logger.e(e, "global layout crash")
                }
            }
        }
    })
}

fun Any.anyToBoolean(): Boolean {
    return when (this) {
        is String -> {
            when (this) {
                "true", "1" -> true
                else -> false
            }
        }
        is Int -> this == 1
        is Boolean -> this
        else -> false
    }
}

fun Context.screenWidth(): Int {
    val metrics = DisplayMetrics()
    this.windowManager.defaultDisplay.getMetrics(metrics)
    return metrics.widthPixels
}

fun Context.screenHeight(): Int {
    val metrics = DisplayMetrics()
    this.windowManager.defaultDisplay.getMetrics(metrics)
    return metrics.heightPixels
}

fun View.setHeight(height: Int) {
    val params = layoutParams
    params.height = height
    layoutParams = params
}

fun View.setWidth(width: Int) {
    val params = layoutParams
    params.width = width
    layoutParams = params
}


// used for show a toast message in the UI Thread
fun Activity.toast(message: String) {
    runOnUiThread { Toast.makeText(this, message, Toast.LENGTH_SHORT).show() }
}

// used for simple start activity without Intent parameters
fun Activity.callTo(clazz: Class<out Activity>) {
    startActivity(Intent(this, clazz))
}

fun View.showKeyboard(show: Boolean) {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    if (show) {
        if (requestFocus()) imm.showSoftInput(this, 0)
    } else {
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
}

fun Dialog.showKeyboard() {
    this.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
}

fun Context?.convertDpToPixel(dp: Float): Float {
    return if (this != null) {
        val resources = this.resources
        val metrics = resources.displayMetrics
        dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    } else {
        val metrics = Resources.getSystem().displayMetrics
        dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }
}

val Int.pxToDp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()

val Int.dpToPx: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()


fun <T : Any?> MutableLiveData<T>.default(initialValue: T) = apply { setValue(initialValue) }

fun Int.toFloatValue(maxProgress: Float): Float {
    return this / maxProgress
}

fun getTag(clazz: Class<*>): String {
    val tag = clazz.simpleName
    return if (tag.length <= 23) {
        tag
    } else {
        tag.substring(0, 23)
    }
}

fun Bitmap.rotate(degrees: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

fun Bitmap.rotatedMatrix(matrix: Matrix): Bitmap {
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

fun Bitmap.writeInternal(context: Context, fileName: String) {
    val outputStream: FileOutputStream?
    try {
        outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)
        outputStream!!.write(this.rowBytes)
        outputStream.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }

}

fun RecyclerView.itemAnimationOff() {
    this.itemAnimator = null
    val animator = this.itemAnimator
    if (animator is SimpleItemAnimator) {
        animator.supportsChangeAnimations = false
    }
}

@Throws(Resources.NotFoundException::class)
fun Uri.getUriToResource(@AnyRes resId: Int): Uri {
    val res = Resources.getSystem()
    return Uri.parse(
        ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + res.getResourcePackageName(resId)
                + '/'.toString() + res.getResourceTypeName(resId)
                + '/'.toString() + res.getResourceEntryName(resId)
    )
}


fun Int.toBoolean(): Boolean {
    return (this == 1)
}

fun RecyclerView.betterSmoothScrollToPosition(targetItem: Int, maxScroll: Int = 10) {
    layoutManager?.apply {

        when (this) {
            is LinearLayoutManager -> {
                val topItem = findFirstVisibleItemPosition()
                val distance = topItem - targetItem
                val anchorItem = when {
                    distance > maxScroll -> targetItem + maxScroll
                    distance < -maxScroll -> targetItem - maxScroll
                    else -> topItem
                }
                if (anchorItem != topItem) scrollToPosition(anchorItem)
                post {
                    smoothScrollToPosition(targetItem).runCatching { }
                }
            }
            else -> post {
                smoothScrollToPosition(targetItem).runCatching { }
            }
        }
    }
}

fun SeekBar?.setProgress(progress: Int, animation: Boolean = true) {
    if (Build.VERSION_CODES.N <= Build.VERSION.SDK_INT) {
        this?.setProgress(progress, animation)
    } else {
        this?.progress = progress
    }
}

fun Context?.getDiskCacheDir(uniqueName: String): File? {
    val cachePath: String
    this?.let { context ->
        cachePath = if ((Environment.MEDIA_MOUNTED.equals(
                Environment
                    .getExternalStorageState()
            ) || !Environment.isExternalStorageRemovable())
        ) {
            context.externalCacheDir?.path ?: ""
        } else {
            context.cacheDir.path
        }
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        return File(cachePath + File.separator + uniqueName)

    }
    return null
}

fun Activity?.withContext(block: () -> Unit) {
    try {
        if (!((this?.getActivity()?.isFinishing)
                ?: (if (this is Activity) this.isFinishing else true))
        ) {
            block()
        }
    } catch (e: java.lang.Exception) {
        //TODO log olarak fırlat --> com.orhanobut.logger.Logger.e(e.localizedMessage, "Has Context Error!")
    }
}


fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
    observe(lifecycleOwner, object : Observer<T> {
        override fun onChanged(t: T?) {
            observer.onChanged(t)
            removeObserver(this)
        }
    })

}

@SuppressLint("MissingPermission")
fun Context.isOnline(): Boolean {

    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetworkInfo = connectivityManager.activeNetworkInfo
    return activeNetworkInfo != null && activeNetworkInfo.isConnected
}

fun Context.clearAppData() {
    try {
        // clearing app data
        if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
            applicationContext.activityManager.clearApplicationUserData() // note: it has a return value!
        } else {
            val packageName = applicationContext.packageName
            val runtime = Runtime.getRuntime()
            runtime.exec("pm clear $packageName")
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Dialog.safelyDismiss() {
    if (this.isShowing) {
        this.dismiss()
    }
}

fun Bitmap?.safeRecycled() {
    this?.let {
        if (!it.isRecycled) {
            it.recycle()
        }
    }
}

operator fun Bitmap?.not(): Boolean {
    this?.let {
        if (!this.isRecycled) {
            return true
        }
    }
    return false
}

fun Drawable?.bitmap(): Bitmap? {
    this?.let {
        return (this as BitmapDrawable).bitmap
    }
    return null
}

fun String.isJson(): Boolean {
    try {
        JSONObject(this)
    } catch (ex: JSONException) {
        try {
            JSONArray(this)
        } catch (ex1: JSONException) {
            return false
        }
    }
    return true
}

fun Int?.plusOne() = this.let { it?.plus(1) }

fun Any?.shakeAnimator(propertyName: String) =
    ObjectAnimator.ofFloat(this, propertyName, 0f, 5f).apply {
        repeatMode = ValueAnimator.REVERSE
        repeatCount = 7
        duration = 50
        interpolator = LinearInterpolator()
    }


private var mLastClickTime: Long = 0


fun Context?.getActivity(): Activity? {
    this?.let {
        while (it is ContextThemeWrapper) {
            if (it is Activity) {
                return it
            }
        }
    }
    return null
}


/**
 * @param name Name of the Shared Preferences
 * @return SharedPreferences
 */
fun Context.getPrefs(name: String): SharedPreferences =
    this.getSharedPreferences(name, AppCompatActivity.MODE_PRIVATE)


@Suppress("unused")
fun Context.mobileCountryCode(): String {
    return detectSIMCountry() ?: detectNetworkCountry() ?: detectLocaleCountry() ?: "0"
}

val Context.telephonyManager
    get() = ContextCompat.getSystemService(this, TelephonyManager::class.java)

private fun Context.detectSIMCountry(): String? {
    try {
        val iso = telephonyManager?.simCountryIso
        return if (!iso.isNullOrEmpty())
            iso.toUpperCase(Locale.US)
        else
            null
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return null
}

private fun Context.detectNetworkCountry(): String? {
    try {
        val country = if (telephonyManager?.phoneType == PHONE_TYPE_CDMA) {
            // CDMA has always got one home base and many visitor base location. and you can always
            // get to know about visitor Operator using TelephonyManager methods which is considered
            // as unreliable for CDMA.
            getCDMACountryIso()
        } else {
            telephonyManager?.networkCountryIso
        }
        return if (!country.isNullOrEmpty())
            country.toUpperCase(Locale.US)
        else
            null
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return null
}

private fun getCDMACountryIso(): String? {
    try {
        @SuppressLint("PrivateApi")
        val c = Class.forName("android.os.SystemProperties")
        val get = c.getMethod("get", String::class.java)
        val homeOperator = get.invoke(c, "ro.cdma.home.operator.numeric") as? String
            ?: return null // MCC + MNC

        // just MCC
        return when (Integer.parseInt(homeOperator.substring(0, 3))) {
            330 -> "PR"
            310, 311, 312, 316 -> "US"
            283 -> "AM"
            460 -> "CN"
            455 -> "MO"
            414 -> "MM"
            619 -> "SL"
            450 -> "KR"
            634 -> "SD"
            434 -> "UZ"
            232 -> "AT"
            204 -> "NL"
            262 -> "DE"
            247 -> "LV"
            255 -> "UA"
            else -> null
        }

    } catch (ignored: ClassNotFoundException) {
    } catch (ignored: NoSuchMethodException) {
    } catch (ignored: IllegalAccessException) {
    } catch (ignored: InvocationTargetException) {
    } catch (ignored: NullPointerException) {
    }
    return null
}

fun View.showSnackBar(message: String) {
    val snackBar = Snackbar.make(
        this,
        message,
        Snackbar.LENGTH_SHORT
    )
    snackBar.show()
}

private fun Context.detectLocaleCountry(): String? {
    try {
        val localeCountryISO = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            resources.configuration.locales[0].country
        } else {
            @Suppress("DEPRECATION")
            resources.configuration.locale.country
        }
        return localeCountryISO.toUpperCase(Locale.US)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

val gson = Gson()
inline fun <reified T> Any.convertModel(): T? {
    return if (this is String) {
        try {
            gson.fromJson(this, T::class.java)
        } catch (e: java.lang.Exception) {
            null
        }
    } else {
        this as T
    }
}

//convert a data class to a map
fun <T> T.serializeToMap(): Map<String, Any> {
    return convert()
}

//convert a map to a data class
inline fun <reified T> Map<String, Any>.toDataClass(): T {
    return convert()
}

//convert an object of type I to type O
inline fun <I, reified O> I.convert(): O {
    val json = gson.toJson(this)
    return gson.fromJson(json, object : TypeToken<O>() {}.type)
}

data class ZipIO (val entry: ZipEntry, val output: File)

fun File.unzip(unzipLocationRoot: File? = null) {

    val rootFolder = unzipLocationRoot ?: File(parentFile.absolutePath + File.separator + nameWithoutExtension)
    if (!rootFolder.exists()) {
        rootFolder.mkdirs()
    }

    ZipFile(this).use { zip ->
        zip
            .entries()
            .asSequence()
            .map {
                val outputFile = File(rootFolder.absolutePath + File.separator + it.name)
                ZipIO(it, outputFile)
            }
            .map {
                it.output.parentFile?.run{
                    if (!exists()) mkdirs()
                }
                it
            }
            .filter { !it.entry.isDirectory }
            .forEach { (entry, output) ->
                zip.getInputStream(entry).use { input ->
                    output.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
    }

}



