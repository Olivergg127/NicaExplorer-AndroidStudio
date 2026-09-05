package com.lospuntoycoma.nicaexplorer.navigation

import android.content.Intent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lospuntoycoma.nicaexplorer.data.FirebaseRepository
import com.lospuntoycoma.nicaexplorer.data.SampleData
import com.lospuntoycoma.nicaexplorer.ui.screens.AcercaDeScreen
import com.lospuntoycoma.nicaexplorer.ui.screens.AdminPanelScreen
import com.lospuntoycoma.nicaexplorer.ui.screens.ArPlaceholderScreen
import com.lospuntoycoma.nicaexplorer.ui.screens.AssistantScreen
import com.lospuntoycoma.nicaexplorer.ui.screens.CatalogScreen
import com.lospuntoycoma.nicaexplorer.ui.screens.CitySelectionScreen
import com.lospuntoycoma.nicaexplorer.ui.screens.ComercioDetalleScreen
import com.lospuntoycoma.nicaexplorer.ui.screens.ComerciosScreen
import com.lospuntoycoma.nicaexplorer.ui.screens.ConfiguracionScreen
import com.lospuntoycoma.nicaexplorer.ui.screens.EditarPerfilScreen
import com.lospuntoycoma.nicaexplorer.ui.screens.HistorialExploracionScreen
import com.lospuntoycoma.nicaexplorer.ui.screens.HomeScreen
import com.lospuntoycoma.nicaexplorer.ui.screens.LoginScreen
import com.lospuntoycoma.nicaexplorer.ui.screens.LugaresGuardadosScreen
import com.lospuntoycoma.nicaexplorer.ui.screens.ProfileScreen
import com.lospuntoycoma.nicaexplorer.ui.screens.RecoveryPasswordScreen
import com.lospuntoycoma.nicaexplorer.ui.screens.RegisterScreen
import com.lospuntoycoma.nicaexplorer.ui.screens.RutasInteligentesScreen
import com.lospuntoycoma.nicaexplorer.ui.screens.SplashScreen
import com.lospuntoycoma.nicaexplorer.ui.screens.SolicitudComercioScreen
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
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
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
                },
                onNavigateToRecovery = {
                    navController.navigate(Routes.RECUPERAR_CONTRASENA)
                }
            )
        }

        composable(Routes.RECUPERAR_CONTRASENA) {
            RecoveryPasswordScreen(
                onBack = {
                    navController.popBackStack()
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
                onCityCardClick = { cityId ->
                    navController.navigate(Routes.catalog(cityId))
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
                onSavedPlacesClick = {
                    navController.navigate(Routes.LUGARES_GUARDADOS)
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
            val profile by userViewModel.userProfile.collectAsState()
            val loading by userViewModel.isLoading.collectAsState()
            val authorized = profile?.rol == com.lospuntoycoma.nicaexplorer.model.UserRole.ADMIN ||
                profile?.rol == com.lospuntoycoma.nicaexplorer.model.UserRole.AUDITOR

            LaunchedEffect(profile, loading) {
                if (!loading && !authorized) {
                    navController.popBackStack()
                }
            }

            if (authorized) {
                AdminPanelScreen(
                    userViewModel = userViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
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
            arguments = listOf(
                navArgument("cityId") { type = NavType.StringType },
                navArgument("monumentId") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val context = LocalContext.current
            val cityId = backStackEntry.arguments?.getString("cityId") ?: ""
            val monumentId = backStackEntry.arguments?.getString("monumentId")
                ?.takeIf { it.isNotBlank() }
            CatalogScreen(
                cityId = cityId,
                initialMonumentId = monumentId,
                onVerEn3dClick = { visorMonumentId ->
                    Intent().setClassName(
                        context.packageName,
                        "com.lospuntoycoma.nicaexplorer.ar.UnityArActivity"
                    ).also { intent ->
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.putExtra("cityId", cityId)
                        intent.putExtra("monumentId", visorMonumentId)
                        intent.putExtra("escena", "visor3d")
                        context.startActivity(intent)
                    }
                },
                onAssistantClick = { assistantMonumentId ->
                    navController.navigate(Routes.assistant(assistantMonumentId))
                },
                onComercioClick = { comercioId ->
                    navController.navigate(Routes.comercioDetalle(comercioId))
                },
                onVerTodosComercios = {
                    navController.navigate(Routes.comercios(cityId))
                },
                onRutasInteligentesClick = {
                    navController.navigate(Routes.rutasInteligentes(cityId))
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Routes.RUTAS_INTELIGENTES,
            arguments = listOf(
                navArgument("cityId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val routeCityId = backStackEntry.arguments?.getString("cityId") ?: ""
            RutasInteligentesScreen(
                cityId = routeCityId,
                onVerLugar = { monumentId ->
                    navController.navigate(
                        Routes.catalogWithMonument(routeCityId, monumentId)
                    )
                },
                onVerComercio = { comercioId ->
                    navController.navigate(Routes.comercioDetalle(comercioId))
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
                },
                onEditProfile = {
                    navController.navigate(Routes.EDITAR_PERFIL)
                },
                onSavedPlaces = {
                    navController.navigate(Routes.LUGARES_GUARDADOS)
                },
                onHistory = {
                    navController.navigate(Routes.HISTORIAL)
                },
                onSettings = {
                    navController.navigate(Routes.CONFIGURACION)
                },
                onAbout = {
                    navController.navigate(Routes.ACERCA_DE)
                }
            )
        }

        composable(
            route = Routes.COMERCIOS_CON_FILTRO,
            arguments = listOf(
                navArgument("cityId") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val cityFilter = backStackEntry.arguments?.getString("cityId")?.takeIf { it.isNotBlank() }
            ComerciosScreen(
                cityFilter = cityFilter,
                onComercioClick = { comercioId ->
                    navController.navigate(Routes.comercioDetalle(comercioId))
                },
                onSolicitarAparicion = { cityId ->
                    navController.navigate(Routes.solicitudComercio(cityId))
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Routes.SOLICITUD_COMERCIO,
            arguments = listOf(
                navArgument("cityId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val requestedCityId = backStackEntry.arguments?.getString("cityId") ?: ""
            val city = SampleData.cities.firstOrNull { it.id == requestedCityId }

            SolicitudComercioScreen(
                cityId = city?.id.orEmpty(),
                ciudad = city?.name.orEmpty(),
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Routes.COMERCIO_DETALLE,
            arguments = listOf(
                navArgument("comercioId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val comercioId = backStackEntry.arguments?.getString("comercioId") ?: ""
            ComercioDetalleScreen(
                comercioId = comercioId,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.EDITAR_PERFIL) {
            EditarPerfilScreen(
                userViewModel = userViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.LUGARES_GUARDADOS) {
            LugaresGuardadosScreen(
                onExplore = {
                    navController.navigate(Routes.CITY_SELECTION)
                },
                onOpenMonument = { cityId, monumentId ->
                    navController.navigate(Routes.catalogWithMonument(cityId, monumentId))
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.HISTORIAL) {
            HistorialExploracionScreen(
                onExplore = {
                    navController.navigate(Routes.CITY_SELECTION)
                },
                onOpenMonument = { cityId, monumentId ->
                    navController.navigate(Routes.catalogWithMonument(cityId, monumentId))
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.CONFIGURACION) {
            ConfiguracionScreen(
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

        composable(Routes.ACERCA_DE) {
            AcercaDeScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Routes.ASSISTANT_ROUTE,
            arguments = listOf(
                navArgument("monumentId") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val monumentId = backStackEntry.arguments?.getString("monumentId")
                ?.takeIf { it.isNotBlank() }
            val monumentContext = monumentId?.let { id ->
                SampleData.allMonuments.firstOrNull { it.id == id }
            }

            AssistantScreen(
                monumentContext = monumentContext,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
