package com.tally.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.tally.app.navigation.NavGraph
import com.tally.app.ui.theme.TallyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // enableEdgeToEdge 会自动设置 setDecorFitsSystemWindows(false) 和透明系统栏
        // 不要在此再设置 window.statusBarColor / navigationBarColor，否则会覆盖透明设置
        enableEdgeToEdge()

        setContent {
            TallyTheme {
                NavGraph()
            }
        }
    }
}
