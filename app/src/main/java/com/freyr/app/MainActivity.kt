package com.freyr.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.navigation.compose.rememberNavController
import com.freyr.app.ui.navigation.FreyrNavHost
import com.freyr.app.ui.theme.FreyrTheme
import com.freyr.app.ui.viewmodel.FreyrViewModel
import com.freyr.app.ui.viewmodel.FreyrViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: FreyrViewModel by viewModels {
        FreyrViewModelFactory((application as FreyrApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FreyrTheme {
                val navController = rememberNavController()
                FreyrNavHost(
                    navController = navController,
                    viewModel = viewModel
                )
            }
        }
    }
}
