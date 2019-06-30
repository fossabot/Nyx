package com.larryhsiao.nyx.diary.pages

import android.os.Bundle
import android.os.PersistableBundle
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import com.silverhetch.aura.AuraActivity

/**
 * Create Diary Activity invoked by Widgets, launch once and do not back to app after saving.
 */
class NewDiaryWidgetActivity : AuraActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(FrameLayout(this).apply {
            id = ViewCompat.generateViewId()
            layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            setupPageControl(id)
        })

        rootPage(NewDiaryFragment())
    }
}