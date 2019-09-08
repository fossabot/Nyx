package com.larryhsiao.nyx.web.diaries

import com.larryhsiao.nyx.database.RDatabase
import com.larryhsiao.nyx.web.RsJson
import org.takes.Request
import org.takes.Response
import org.takes.Take

/**
 * Fork of response Diary resources in json.
 */
class TkDiaries(private val db: RDatabase) : Take {
    override fun act(req: Request?): Response {
        return RsJson(DiaryListView(db.diaryDao().all()).value().toString())
    }
}