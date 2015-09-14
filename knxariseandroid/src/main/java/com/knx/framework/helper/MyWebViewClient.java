package com.knx.framework.helper;

import android.webkit.WebView;
import android.webkit.WebViewClient;

/**********************************
 * Not sure if this class is used.
 * @author le_vu
 **********************************
 */
public class MyWebViewClient extends WebViewClient {
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        view.loadUrl(url);
        return true;
    }
}