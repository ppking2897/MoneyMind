package com.bianca.moneymind.presentation.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bianca.moneymind.navigation.NavGraph
import com.bianca.moneymind.navigation.Screen

data class BottomNavItem(
    val screen: Screen,
    val icon: ImageVector,
    val label: String
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, Icons.Default.Home, "首頁"),
    BottomNavItem(Screen.Analysis, Icons.Default.Info, "分析"),
    BottomNavItem(Screen.History, Icons.AutoMirrored.Filled.List, "歷史"),
    BottomNavItem(Screen.Settings, Icons.Default.Settings, "設定")
)

@Composable
fun MainScreen(
    navController: NavHostController = rememberNavController()
) {
    var isFabExpanded by remember { mutableStateOf(false) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Only show bottom nav on main screens
    val showBottomBar = currentDestination?.hierarchy?.any { dest ->
        bottomNavItems.any { it.screen.route == dest.route }
    } == true

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        bottomNavItems.forEachIndexed { index, item ->
                            // Add spacer in the middle for FAB
                            if (index == 2) {
                                Spacer(modifier = Modifier.weight(1f))
                            }

                            NavigationBarItem(
                                icon = { Icon(item.icon, contentDescription = item.label) },
                                label = { Text(item.label) },
                                selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true,
                                onClick = {
                                    navController.navigate(item.screen.route) {
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
            }
        ) { innerPadding ->
            NavGraph(
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        }

        // FAB and expanded menu
        if (showBottomBar) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 28.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                // Scrim when expanded
                AnimatedVisibility(
                    visible = isFabExpanded,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f),
                        onClick = { isFabExpanded = false }
                    ) {}
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Expanded options
                    AnimatedVisibility(
                        visible = isFabExpanded,
                        enter = slideInVertically { it } + fadeIn(),
                        exit = slideOutVertically { it } + fadeOut()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            FabOption(
                                icon = Icons.AutoMirrored.Filled.Send,
                                label = "AI 輸入",
                                onClick = {
                                    isFabExpanded = false
                                    navController.navigate(Screen.Chat.route)
                                }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            FabOption(
                                icon = Icons.Default.AddCircle,
                                label = "拍收據",
                                onClick = {
                                    isFabExpanded = false
                                    navController.navigate(Screen.Camera.route)
                                }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            FabOption(
                                icon = Icons.Default.Create,
                                label = "手動輸入",
                                onClick = {
                                    isFabExpanded = false
                                    navController.navigate(Screen.ManualInput.route)
                                }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    // Main FAB
                    FloatingActionButton(
                        onClick = { isFabExpanded = !isFabExpanded },
                        shape = CircleShape,
                        containerColor = if (isFabExpanded)
                            MaterialTheme.colorScheme.surfaceVariant
                        else
                            MaterialTheme.colorScheme.primaryContainer,
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = 6.dp
                        )
                    ) {
                        Icon(
                            imageVector = if (isFabExpanded) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = if (isFabExpanded) "關閉" else "新增記錄"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FabOption(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SmallFloatingActionButton(
            onClick = onClick,
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Icon(icon, contentDescription = label)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
