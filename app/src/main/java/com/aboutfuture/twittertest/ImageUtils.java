package com.aboutfuture.twittertest;

import android.net.Uri;

public class ImageUtils {
    private static final String VIDEO_THUMBNAIL_BASE_URL = "https://img.youtube.com/vi";
    private static final String VIDEO_THUMBNAIL_SIZE_SD = "sddefault.jpg";
    private static final String VIDEO_THUMBNAIL_SIZE_HQ = "hqdefault.jpg";

    public static String buildSdVideoThumbnailUrl(String missionKey) {
        return Uri.parse(VIDEO_THUMBNAIL_BASE_URL).buildUpon()
                .appendPath(missionKey)
                .appendPath(VIDEO_THUMBNAIL_SIZE_SD)
                .build().toString();
    }
}
