package com.tally.app.navigation

import android.annotation.SuppressLint
import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.webkit.WebViewAssetLoader
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tally.app.ui.chat.ChatScreen
import com.tally.app.ui.chat.ChatViewModel

sealed class Screen(val route: String, val label: String, val icon: @Composable () -> Unit) {
    object Chat : Screen("chat", "聊天", { Icon(Icons.Default.Home, contentDescription = "聊天") })
    object Calendar : Screen("calendar", "日历", { Icon(Icons.Default.DateRange, contentDescription = "日历") })
    object Expense : Screen("expense", "费用", { Icon(Icons.Default.ShoppingCart, contentDescription = "费用") })
}

val bottomNavItems = listOf(Screen.Chat, Screen.Calendar, Screen.Expense)

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val chatViewModel: ChatViewModel = viewModel()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = screen.icon,
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Chat.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Chat.route) {
                ChatScreen(vm = chatViewModel)
            }
            composable(Screen.Calendar.route) {
                WebViewScreen(route = "calendar") { message ->
                    chatViewModel.prefillInput(message)
                    navController.navigate(Screen.Chat.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = false }
                        launchSingleTop = true
                    }
                }
            }
            composable(Screen.Expense.route) {
                WebViewScreen(route = "expense") { message ->
                    chatViewModel.prefillInput(message)
                    navController.navigate(Screen.Chat.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = false }
                        launchSingleTop = true
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun WebViewScreen(route: String, onNavigateToChat: (String) -> Unit) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            // 允许 chrome://inspect 远程调试
            WebView.setWebContentsDebuggingEnabled(true)

            val assetLoader = WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(context))
                .build()

            WebView(context).apply {
                setBackgroundColor(Color.WHITE)

                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(msg: ConsoleMessage): Boolean {
                        Log.d("WebViewConsole", "[${msg.messageLevel()}] ${msg.message()} @ ${msg.sourceId()}:${msg.lineNumber()}")
                        return true
                    }
                }

                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                        Log.d("WebViewNav", "onPageStarted: $url")
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        Log.d("WebViewNav", "onPageFinished: $url")
                        // 诊断: 检查 DOM 状态
                        view?.evaluateJavascript("""
                            (function() {
                                var root = document.getElementById('root');
                                var body = document.body;
                                return JSON.stringify({
                                    rootChildren: root ? root.children.length : -1,
                                    rootOffsetH: root ? root.offsetHeight : -1,
                                    viewportH: window.innerHeight,
                                    bodyBg: window.getComputedStyle(body).backgroundColor,
                                    hash: window.location.hash,
                                    rootHTML: root ? root.innerHTML.substring(0, 400) : 'null'
                                });
                            })()
                        """.trimIndent()) { result ->
                            Log.d("WebViewDOM", "DOM: $result")
                        }
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?
                    ) {
                        if (request?.isForMainFrame == true) {
                            Log.e("WebViewNav", "onReceivedError: ${error?.errorCode} ${error?.description} url=${request.url}")
                            val errorHtml = """
                                <html>
                                <body style="background:#111111;color:#ffffff;text-align:center;padding:60px 20px;font-family:sans-serif">
                                  <div style="font-size:48px;margin-bottom:16px">⚠️</div>
                                  <h2 style="color:#ffffff;margin-bottom:8px">加载失败</h2>
                                  <p style="color:#888888;margin-bottom:32px">${error?.description ?: "网络连接错误"}</p>
                                  <button onclick="location.reload()"
                                    style="background:#1DB954;color:#000000;border:none;padding:12px 32px;border-radius:24px;font-size:16px;cursor:pointer">
                                    重新加载
                                  </button>
                                </body>
                                </html>
                            """.trimIndent()
                            view?.loadData(errorHtml, "text/html", "utf-8")
                        }
                    }

                    override fun shouldInterceptRequest(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): WebResourceResponse? {
                        val url = request?.url ?: return null
                        val response = assetLoader.shouldInterceptRequest(url)
                        if (response != null) {
                            response.responseHeaders = (response.responseHeaders ?: emptyMap()).toMutableMap().apply {
                                put("Access-Control-Allow-Origin", "*")
                                put("Access-Control-Allow-Methods", "GET, OPTIONS")
                                put("Access-Control-Allow-Headers", "*")
                            }
                        }
                        return response
                    }
                }

                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    allowFileAccess = false
                    allowContentAccess = false
                    setSupportZoom(false)
                    builtInZoomControls = false
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                }

                val bridge = TallyJsBridge(onNavigateToChat)
                addJavascriptInterface(bridge, "Android")

                loadUrl("https://appassets.androidplatform.net/assets/www/index.html#/$route")
            }
        }
    )
}
