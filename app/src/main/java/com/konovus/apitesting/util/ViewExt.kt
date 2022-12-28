package com.konovus.apitesting.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import java.util.concurrent.Executors
import kotlin.math.ln
import kotlin.math.pow

private var job: Job? = null

fun runAtInterval(coroutineScope: LifecycleCoroutineScope, interval: Long, block: () -> Unit) {
    job?.cancel()
    job = coroutineScope.launchWhenStarted {
        while (true) {
            block()
            delay(interval)
        }
    }
}

inline fun SearchView.onQueryTextChanged(
    searchView: SearchView?,
    crossinline listener: (String) -> Unit
) {
    this.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            searchView?.clearFocus()
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            newText?.let { listener(it) }
            return true
        }
    })
}

inline fun TabLayout.OnTabSelected( crossinline listener: (Int) -> Unit) {
    this.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab?) {
            listener(tab?.position ?: 0)
        }

        override fun onTabReselected(tab: TabLayout.Tab?) {
            // Handle tab reselect
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {
            // Handle tab unselect
        }
    })
}

inline fun myItemTouchHelper(recyclerView: RecyclerView, crossinline listener: (Int) -> Unit){
    ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0,
        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
           listener(viewHolder.adapterPosition)
        }
    }).attachToRecyclerView(recyclerView)
}

fun <T, S> LiveData<T?>.combineWith(other: LiveData<S?>): LiveData<Pair<T?, S?>> =
    MediatorLiveData<Pair<T?, S?>>().apply {
        addSource(this@combineWith) { value = Pair(it, other.value) }
        addSource(other) { value = Pair(this@combineWith.value, it) }
    }

fun <T> List<T>.replaceIf(newValue: T, block: (T) -> Boolean): List<T> {
    return map {
        if (block(it)) newValue else it
    }
}


fun String?.withSuffix(): String {
    if (this == null) return ""
    val count = this.toDoubleOrNull() ?: return ""
    if (count < 1000) return "" + count
    val exp = (ln(count) / ln(1000.0)).toInt()
    return String.format(
        "%.1f %s",
        count / 1000.0.pow(exp.toDouble()),
        arrayOf("k", "mil", "bn", "t", "p", "e")[exp - 1]
    )
}

fun Double.toNDecimals(n : Int) : Double {
    return "%.${n}f".format(this).toDouble()
}
fun String?.toNDecimals(n : Int) : String {
    if (this == null) return ""
    val d = this.toDoubleOrNull() ?: return this
    return "%.${n}f".format(d)
}

fun String.firstCharToUppercase(): String {
    if (this.isEmpty())
        return this
    return this.first().uppercase() + this.substring(1)
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

suspend fun saveDataStore(key: String, value: String, context: Context) {
    val dataStoreKey = stringPreferencesKey(key)
    context.dataStore.edit { settings ->
        settings[dataStoreKey] = value
    }
}

suspend fun readDataStore(key: String, context: Context) : String {
    val dataStoreKey = stringPreferencesKey(key)
    val preferences = context.dataStore.data.first()
    return preferences[dataStoreKey] ?: ""
}

suspend fun clearDataStore(key: String, context: Context) {
    val dataStoreKey = stringPreferencesKey(key)
    context.dataStore.edit { settings ->
        settings.remove(dataStoreKey)
    }
}



private val IO_EXECUTOR = Executors.newSingleThreadExecutor()

/**
 * Utility method to run blocks on a dedicated background thread, used for io/database work.
 */
fun ioThread(f : () -> Unit) {
    IO_EXECUTOR.execute(f)
}

//create a bitmap using view height and width to draw to it
fun View.getBitmap(): Bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888).also {
    //create a canvas for this bitmap (it)
    Canvas(it).apply {
        //if the view has a background, draw it to the canvas, else draw a white screen
        //because canvas default background is black
        background?.draw(this) ?: drawColor(Color.WHITE)
        //draw the view to the canvas
        draw(this)
    }
}