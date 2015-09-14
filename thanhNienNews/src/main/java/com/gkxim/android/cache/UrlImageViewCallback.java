package com.gkxim.android.cache;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

public interface UrlImageViewCallback {
//    void onLoaded(ImageView imageView, Drawable loadedDrawable, String url, boolean loadedFromCache);
    void onLoaded(ImageView imageView, Drawable loadedDrawable, String url, boolean loadedFromCache, String id);
}
