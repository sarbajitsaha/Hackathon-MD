package com.coredumped.project.common.utils

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri

object AssetMediaHelper {
    /**
     * returns a Uri for a file in assets/media to be used with ExoPlayer or VideoView
     * e.g. "rabbit_eating_carrot.mp4"
     */
    fun getAssetUri(filename: String): Uri {
        return Uri.parse("file:///android_asset/media/$filename")
    }

    /**
     * Creates and prepares a MediaPlayer from an asset in assets/media.
     * Equivalent to MediaPlayer.create(context, R.raw.foo)
     */
    fun createMediaPlayer(context: Context, filename: String): MediaPlayer? {
        return try {
            val afd = context.assets.openFd("media/$filename")
            val mp = MediaPlayer()
            mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()
            mp.prepare()
            mp
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
