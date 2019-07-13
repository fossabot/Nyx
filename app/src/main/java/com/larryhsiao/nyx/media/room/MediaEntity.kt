package com.larryhsiao.nyx.media.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity definition media table.
 */
@Entity(tableName = "media")
data class MediaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo(name = "diary_id") val diaryId: Long,
    val uri: String
)