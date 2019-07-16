package com.larryhsiao.nyx.view.diary

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.larryhsiao.nyx.R
import com.larryhsiao.nyx.view.diary.viewmodel.CalendarViewModel
import com.silverhetch.aura.AuraFragment
import com.silverhetch.aura.view.fab.FabBehavior

/**
 * Fragment shows all event list by dateTime
 */
class EventListFragment : AuraFragment(), FabBehavior {
    companion object {
        private const val ARG_DATETIME = "ARG_DATETIME"
        private const val REQUEST_CODE_NEW_DIARY = 1000
        private const val REQUEST_CODE_DIARY = 1001

        /**
         * @param dateTime The specific date in timestamp
         */
        fun newInstance(dateTime: Long): Fragment {
            return EventListFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_DATETIME, dateTime)
                }
            }
        }
    }

    private lateinit var viewModel: CalendarViewModel
    private lateinit var list: RecyclerView
    private val adapter = DiaryAdapter() {
        nextPage(
            DiaryFragment.newInstance(
                it.id()
            ).also { fragment ->
                fragment.setTargetFragment(
                    this,
                    REQUEST_CODE_DIARY
                )
            })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel =
            ViewModelProviders.of(this).get(CalendarViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return RecyclerView(inflater.context).also {
            list = it
            list.adapter = adapter
            it.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            it.layoutManager = LinearLayoutManager(inflater.context)
        }
    }

    override fun onResume() {
        super.onResume()
        attachFab(this)
    }

    override fun onPause() {
        super.onPause()
        detachFab()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadData()
    }

    override fun onClick() {
        startActivityForResult(
            Intent(context, NewDiaryActivity::class.java),
            REQUEST_CODE_NEW_DIARY
        )
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_NEW_DIARY) {
            loadData()
        }
        if (requestCode == REQUEST_CODE_DIARY) {
            activity?.onBackPressed()
            loadData()
        }
    }

    private fun loadData() {
        val date = arguments?.getLong(ARG_DATETIME) ?: 0L
        // @todo #10 scroll to the given date first time load up.
        viewModel.diaries()
            .observe(this, Observer { adapter.load(it) })
    }

    override fun icon(): Int {
        return R.drawable.ic_plus
    }
}