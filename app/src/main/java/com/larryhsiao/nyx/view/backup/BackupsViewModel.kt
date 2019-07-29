package com.larryhsiao.nyx.view.backup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.larryhsiao.nyx.backup.Backup

/**
 * View Model of backups, represent the backup instances.
 */
class BackupsViewModel(private val app: Application) : AndroidViewModel(app) {
    private val backups =
        MutableLiveData<List<Backup>>().apply { value = listOf() }

    fun backups(): LiveData<List<Backup>> {
        return backups
    }

    fun fetch() {
//        GlobalScope.launch {
//            try {
//                backups.postValue(
//
//                )
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
//        }
    }


}