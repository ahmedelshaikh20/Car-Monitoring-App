package com.example.carmonitoringapp.video

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class VideoFrameExtractor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var retriever: MediaMetadataRetriever? = null

    fun init(uri: Uri) {
        retriever = MediaMetadataRetriever().apply {
            setDataSource(context, uri)
        }
    }

    fun extractFrameAt(positionMs: Long): Bitmap? {
        if (retriever == null) return null
        return retriever?.getFrameAtTime(
            positionMs * 1000,
            MediaMetadataRetriever.OPTION_CLOSEST_SYNC
        )
    }

    fun release() {
        retriever?.release()
        retriever = null
    }
}
