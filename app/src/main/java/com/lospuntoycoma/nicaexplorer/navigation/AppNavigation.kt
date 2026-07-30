package com.lospuntoycoma.nicaexplorer.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lospuntoycoma.nicaexplorer.data.FirebaseRepository
import com.lospuntoycoma.nicaexplorer.ui.screens.AdminPanelScreen
import com.lospuntoycoma.nicaexplorer.ui.screens.ArPlaceholderScreen
import com.lospuntoycoma.nicaexplorer.ui.screens.AssistantScreen
import com.lospuntoycoma.nicaexplorer.ui.screens.CatalogScreen
import com.lospuntoycoma.nicaexplorer.ui.screens.CitySelectionScreen
import com.lospuntoycoma.nicaexplorer.ui.screens.HomeScreen
import com.lospuntoycoma.nicaexplorer.ui.screens.LoginScreen
import com.lospuntoycoma.nicaexplorer.ui.screens.ProfileScreen
import com.lospuntoycoma.nicaexplorer.ui.screens.RegisterScreen
import com.lospuntoycoma.nicaexplorer.ui.screens.SplashScreen
import com.lospuntoycoma.nicaexplorer.ui.viewmodels.UserViewModel

@Composable
fun AppNavigation(navController: NavHostController) {
    val userViewModel: UserViewModel = viewModel()
    
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        }
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    userViewModel.loadUserProfile()
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.popBackStack()
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                userViewModel = userViewModel,
                onCityClick = {
                    navController.navigate(Routes.CITY_SELECTION)
                },
                onProfileClick = {
                    navController.navigate(Routes.PROFILE)
                },
                onAssistantClick = {
                    navController.navigate(Routes.ASSISTANT)
                },
                onAdminPanelClick = {
                    navController.navigate(Routes.ADMIN_PANEL)
                },
                onLogout = {
                    FirebaseRepository.signOut()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.ADMIN_PANEL) {
            AdminPanelScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.CITY_SELECTION) {
            CitySelectionScreen(
                onCitySelected = { cityId ->
                    navController.navigate(Routes.catalog(cityId))
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Routes.CATALOG,
            arguments = listOf(navArgument("cityId") { type = NavType.StringType })
        ) { backStackEntry ->
            val cityId = backStackEntry.arguments?.getString("cityId") ?: ""
            CatalogScreen(
                cityId = cityId,
                onArClick = { monumentId ->
                    navController.navigate(Routes.arPlaceholder(cityId, monumentId))
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Routes.AR_PLACEHOLDER,
            arguments = listOf(
                navArgument("cityId") { type = NavType.StringType },
                navArgument("monumentId") { type = NavType.StringType }
            )
        ) {
            ArPlaceholderScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                userViewModel = userViewModel,
                onLogout = {
                    FirebaseRepository.signOut()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.ASSISTANT) {
            AssistantScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
