package com.tally.app.navigation

import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface

class TallyJsBridge(private val onNavigateToChat: (String) -> Unit) {
    @JavascriptInterface
    fun navigateToChat(message: String) {
        // @JavascriptInterface 在后台线程执行，切换到主线程触发导航
        Handler(Looper.getMainLooper()).post {
            onNavigateToChat(message)
        }
    }
}
