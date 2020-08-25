package com.mfpolat.starter.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson

/**
 * Manages Shared Preferences and provides utility funtions
 * Can read and write simple values or objects
 * Uses Gson as the backend to serialize and deserialize objects
 * @property context Required to access SharedPreferences
 * @property sharedPrefsName Parent name of the SharedPreferences space
 */
open class CacheManager(val context: Context, private val sharedPrefsName: String) {

    private val gson: Gson = Gson()
    private val prefs: SharedPreferences by lazy { context.getPrefs(sharedPrefsName) }


    /**
     * Reads a single String, Int, Boolean or Long value from SharedPreferences
     * @param T Object type to read, determined from defaultValue
     * @param key Key of the value
     * @param defaultValue Default value to return if the key does not exist
     * @return A single String, Int, Boolean or Long value
     */
    fun <T> read(key: String, defaultValue: T): T {
        return when (defaultValue) {
            is String -> prefs.getString(key, defaultValue as String) as? T ?: defaultValue
            is Int -> prefs.getInt(key, defaultValue as Int) as T ?: defaultValue
            is Boolean -> prefs.getBoolean(key, defaultValue as Boolean) as T ?: defaultValue
            is Long -> prefs.getLong(key, defaultValue as Long) as T ?: defaultValue
            else -> defaultValue
        }
    }

    /**
     * Stores a single String, Int, Boolean or Long value to SharedPreferences
     * @param T Object type to write, determined from value
     * @param key Key to write to
     * @param value Object of String, Int, Boolean or Long types to store
     */
    fun <T> write(key: String, value: T) {
        when (value) {
            is String -> prefs.edit {
                putString(key, value).commit()
            }
            is Int -> prefs.edit {
                putInt(key, value).commit()
            }
            is Boolean -> prefs.edit {
                putBoolean(key, value).commit()
            }
            is Long -> prefs.edit {
                putLong(key, value).commit()
            }
            else -> Unit
        }
    }

    /**
     * Reads json from SharedPreferences and casts it to requested type using Gson
     * @param T Type parameter to cast gson to
     * @param key Key to read from
     * @param target Specifies requested object type
     * @return An object of requested type
     */
    fun <T> readObject(key: String? = null, target: Class<T>): T? {
        key?.let { safeKey ->
            return gson.fromJson(read(safeKey, ""), target)
        } ?: return gson.fromJson(read(target.simpleName, ""), target) as T
    }

    /**
     * get metodu ile cagiracaginiz modeli belirtebilirsiniz ve eger sharedpref ile kaydettiyseniz ornegin
     * get<AresModel>() seklinde okumaniz mumkundur.
     *
     * @param T
     * @return vermis oldugunuz modeli geri donecektir.
     */
    inline fun <reified T : Any> get(): T? {
        return readObject(target = T::class.java)
    }


    /**
     * Stores an object under given key or class name.
     * @param key Key to write object to. If not given, class name will be used
     * @param data Object to store.
     */
    fun writeObject(key: String? = null, data: Any) {
        key?.let { safeKey ->
            write(safeKey, gson.toJson(data))
        } ?: write(data::class.java.simpleName, gson.toJson(data))
    }

    /**
     * Deletes an object from SharedPreferences
     * @param key to be removed
     */
    fun clearObject(key: String): Unit = prefs.edit {
        remove(key)
    }


    /**
     * Clears all the data under current SharedPreferences name
     * @param successFunction Function to be executed after completing the operation
     */
    fun clearEverything(successFunction: () -> Unit = {}) {
        prefs.edit {
            clear().commit()
            successFunction()
        }
    }
}