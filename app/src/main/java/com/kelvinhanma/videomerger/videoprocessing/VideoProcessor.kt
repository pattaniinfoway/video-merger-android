package com.kelvinhanma.videomerger.videoprocessing

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.kelvinhanma.videomerger.model.Video
import com.kelvinhanma.videomerger.util.toHumanReadableString
import java.util.logging.Logger

/**
 * Holds code to handle videos
 */
class VideoProcessor {
    companion object {
        val LOGGER = Logger.getLogger("VideoProcessor")
    }

    fun run(context: Context): List<Video> {
        LOGGER.info("staring media scan.")
        val uri: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection =
            arrayOf(
                MediaStore.Video.VideoColumns._ID,
                MediaStore.Video.VideoColumns.DISPLAY_NAME,
                MediaStore.Video.VideoColumns.DATE_TAKEN,
                MediaStore.Video.VideoColumns.DURATION
            )

        val videos: MutableList<Video> = ArrayList()

        context.contentResolver.query(
            uri,
            projection,
            null,
            null,
            MediaStore.Video.VideoColumns.DATE_TAKEN + " ASC"
        )?.use { cursor ->
            LOGGER.info("Result: " + cursor.count)
            while (cursor.moveToNext()) {
                // Use an ID column from the projection to get
                // a URI representing the media item itself.
                val id =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns._ID))
                val name =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DISPLAY_NAME))
                val timestamp =
                    cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DATE_TAKEN))
                val duration =
                    cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DURATION))
                LOGGER.info(
                    id + " , " + name + ":" + timestamp.toHumanReadableString()
                            + " , " + duration
                )
                videos.add(Video(id, name, timestamp, duration))
            }
        }
        return videos
    }

    // TODO write tests
    fun detectMergeableVideos(videos: List<Video>, allowedDeltaSecs: Int = 5): List<List<Video>> {
        val result = ArrayList<ArrayList<Video>>()
        val size = videos.size

        for ((startIndex, video) in videos.withIndex()) {
            val maxTime = video.dateTaken + video.dateTaken
            var candidatesFound = false
            for (candidateVideoIndex in startIndex + 1 until size) {
                val candidateVideo = videos[candidateVideoIndex]
                if (candidateVideo.dateTaken in maxTime - allowedDeltaSecs .. maxTime + allowedDeltaSecs) {
                    if (!candidatesFound) {
                        candidatesFound = true
                        val newList = ArrayList<Video>()
                        newList.add(video)
                        newList.add(candidateVideo)
                        result.add(newList)
                    } else {
                        result.last().add(candidateVideo)
                    }
                }
            }
        }
        return result
    }
}