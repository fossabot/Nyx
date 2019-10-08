package com.larryhsiao.nyx.view.diary.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.larryhsiao.nyx.database.RDatabase
import com.larryhsiao.nyx.diary.Diary
import com.larryhsiao.nyx.diary.DiaryWithLocation
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * ViewModel
 */
class LocationViewModel(app: Application) : AndroidViewModel(app) {
    private val db = RDatabase.Singleton(app).value()

    fun loadUp(): LiveData<List<Diary>> {
        return MutableLiveData<List<Diary>>().apply {
            GlobalScope.launch { postValue(DiaryWithLocation(db).value()) }
        }
    }
}