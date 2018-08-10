package com.aboutfuture.twittertest.utils;

import android.net.Uri;

public class ImageUtils {
    private static final String VIDEO_THUMBNAIL_BASE_URL = "https://img.youtube.com/vi";
    private static final String VIDEO_THUMBNAIL_SIZE_SD = "sddefault.jpg";

    public static String buildSdVideoThumbnailUrl(String missionKey) {
        return Uri.parse(VIDEO_THUMBNAIL_BASE_URL).buildUpon()
                .appendPath(missionKey)
                .appendPath(VIDEO_THUMBNAIL_SIZE_SD)
                .build().toString();
    }
}
