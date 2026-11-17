package com.nova.music

import android.net.Uri

data class Track(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val uri: Uri
) {
    val durationLabel: String
        get() {
            val total = durationMs / 1000
            return "%d:%02d".format(total / 60, total % 60)
        }
}
