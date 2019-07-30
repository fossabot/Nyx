package com.larryhsiao.nyx.view.backup

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.larryhsiao.nyx.R
import com.silverhetch.aura.AuraFragment
import com.silverhetch.aura.view.fab.FabBehavior


/**
 * List of backup instance, currently available source: Google.
 */
class BackupListFragment : AuraFragment() {
    private lateinit var list: RecyclerView
    private val adapter = BackupListAdapter()
    private lateinit var viewModel: BackupsViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this)
            .get(BackupsViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return RecyclerView(inflater.context).also {
            it.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            it.layoutManager = LinearLayoutManager(it.context)
            it.adapter = adapter
            list = it
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.backups().observe(this, Observer {
            adapter.loadUp(it)
        })

        viewModel.fetch()
    }

    override fun onPermissionGranted() {
        super.onPermissionGranted()
        viewModel.newBackup()
    }

    override fun onResume() {
        super.onResume()
        attachFab(object : FabBehavior {
            override fun onClick() {
                context?.also {
                    if (ContextCompat.checkSelfPermission(
                            it,
                            WRITE_EXTERNAL_STORAGE
                        ) == PERMISSION_GRANTED
                    ) {
                        viewModel.newBackup()
                    } else {
                        requestPermissionsByObj(
                            arrayOf(
                                WRITE_EXTERNAL_STORAGE
                            )
                        )
                    }
                }
            }

            override fun icon(): Int {
                return R.drawable.ic_save
            }
        })
    }
}