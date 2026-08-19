package com.mobileinvalley.journeypal

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable

@Serializable
object TimelineRoute

@Serializable
data class DetailRoute(val itemId: String)

@Serializable
object MapRoute

@Composable
fun App(database: JourneyDatabase? = null) {
    val dao = database?.journeyDao()
    val journeyItems by if (dao != null) {
        dao.getAllItems().collectAsState(initial = emptyList())
    } else {
        remember { mutableStateOf(getMockJourneyItems()) }
    }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""

    val themeMode = remember { mutableStateOf(ThemeMode.System) }

    CompositionLocalProvider(LocalThemeMode provides themeMode) {
        JourneyPalTheme(themeMode = themeMode.value) {
            Scaffold(
                bottomBar = {
                    // Show bottom bar only on main screens
                    if (currentRoute.contains("TimelineRoute") || currentRoute.contains("MapRoute")) {
                        NavigationBar {
                            NavigationBarItem(
                                selected = currentRoute.contains("TimelineRoute"),
                                onClick = { 
                                    navController.navigate(TimelineRoute) {
                                        popUpTo(TimelineRoute) { inclusive = true }
                                    }
                                },
                                label = { Text("Timeline") },
                                icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Timeline") }
                            )
                            NavigationBarItem(
                                selected = currentRoute.contains("MapRoute"),
                                onClick = { 
                                    navController.navigate(MapRoute)
                                },
                                label = { Text("Map") },
                                icon = { Icon(Icons.Default.Map, contentDescription = "Map") }
                            )
                        }
                    }
                }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    NavHost(
                        navController = navController, 
                        startDestination = TimelineRoute,
                        enterTransition = { fadeIn(animationSpec = tween(300)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
                        exitTransition = { fadeOut(animationSpec = tween(300)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
                        popEnterTransition = { fadeIn(animationSpec = tween(300)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) },
                        popExitTransition = { fadeOut(animationSpec = tween(300)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) }
                    ) {
                        composable<TimelineRoute> {
                            TimelineScreen(
                                dao = dao,
                                onItemClick = { item ->
                                    navController.navigate(DetailRoute(item.id))
                                }
                            )
                        }
                        composable<MapRoute> {
                            JourneyMapView(
                                items = journeyItems,
                                modifier = Modifier.fillMaxSize(),
                                onItemClick = { item ->
                                    navController.navigate(DetailRoute(item.id))
                                }
                            )
                        }
                        composable<DetailRoute> { backStackEntry ->
                            val detail: DetailRoute = backStackEntry.toRoute()
                            JourneyDetailScreen(
                                itemId = detail.itemId,
                                dao = dao,
                                onBack = { navController.popBackStack() },
                                onShowOnMap = {
                                    navController.navigate(MapRoute)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
