package org.wordpress.android.util;

import android.webkit.CookieManager;
import android.webkit.ValueCallback;

public class WebViewUtils {
    public static void clearCookiesAsync() {
        clearCookiesAsync(null);
    }

    public static void clearCookiesAsync(ValueCallback<Boolean> callback) {
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookies(callback);
    }
}
