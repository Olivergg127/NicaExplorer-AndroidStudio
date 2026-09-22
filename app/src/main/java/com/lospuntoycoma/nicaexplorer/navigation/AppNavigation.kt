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
import com.lospuntoycoma.nicaexplorer.ui.screens.ComercioFormScreen
import com.lospuntoycoma.nicaexplorer.ui.screens.ComerciosScreen
import com.lospuntoycoma.nicaexplorer.ui.screens.ConfiguracionScreen
import com.lospuntoycoma.nicaexplorer.ui.screens.EditarPerfilScreen
import com.lospuntoycoma.nicaexplorer.ui.screens.HistorialExploracionScreen
import com.lospuntoycoma.nicaexplorer.ui.screens.HomeScreen
import com.lospuntoycoma.nicaexplorer.ui.screens.LoginScreen
import com.lospuntoycoma.nicaexplorer.ui.screens.LugaresGuardadosScreen
import com.lospuntoycoma.nicaexplorer.ui.screens.MisComerciosScreen
import com.lospuntoycoma.nicaexplorer.ui.screens.ComerciosSubcategoriaScreen
import com.lospuntoycoma.nicaexplorer.ui.screens.ComerciosSubcategoriasScreen
import com.lospuntoycoma.nicaexplorer.ui.screens.MapaPrincipalScreen
import com.lospuntoycoma.nicaexplorer.ui.screens.ProfileScreen
import com.lospuntoycoma.nicaexplorer.ui.screens.RecoveryPasswordScreen
import com.lospuntoycoma.nicaexplorer.ui.screens.RegisterScreen
import com.lospuntoycoma.nicaexplorer.ui.screens.RutasInteligentesScreen
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
                onNavigateToHome = {
                    userViewModel.loadUserProfile()
                    navController.navigate(Routes.HOME) {
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
                },
                onContinueAsGuest = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
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
                    userViewModel.loadUserProfile()
                    navController.navigate(Routes.HOME) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            val profile by userViewModel.userProfile.collectAsState()
            val isLoggedIn = FirebaseRepository.getCurrentUser() != null
            HomeScreen(
                userViewModel = userViewModel,
                isLoggedIn = isLoggedIn,
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
                onMapClick = {
                    navController.navigate(Routes.MAPA)
                },
                onAdminPanelClick = {
                    navController.navigate(Routes.ADMIN_PANEL)
                },
                onSavedPlacesClick = {
                    if (isLoggedIn) {
                        navController.navigate(Routes.LUGARES_GUARDADOS)
                    } else {
                        navController.navigate(Routes.LOGIN)
                    }
                },
                onMisComerciosClick = {
                    navController.navigate(Routes.MIS_COMERCIOS)
                },
                onLoginClick = {
                    navController.navigate(Routes.LOGIN)
                },
                onPlaceClick = { placeCityId, placeId ->
                    navController.navigate(Routes.catalogWithPlace(placeCityId, placeId))
                },
                onComercioClick = { comercioId ->
                    navController.navigate(Routes.comercioDetalle(comercioId))
                },
                onLogout = {
                    FirebaseRepository.signOut()
                    userViewModel.loadUserProfile()
                    navController.navigate(Routes.HOME) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.MAPA) {
            MapaPrincipalScreen(
                onBack = { navController.popBackStack() },
                onVerLugar = { cityId, placeId ->
                    navController.navigate(Routes.catalogWithPlace(cityId, placeId))
                },
                onVerComercio = { comercioId ->
                    navController.navigate(Routes.comercioDetalle(comercioId))
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
                navArgument("placeId") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val context = LocalContext.current
            val cityId = backStackEntry.arguments?.getString("cityId") ?: ""
            val placeId = backStackEntry.arguments?.getString("placeId")
                ?.takeIf { it.isNotBlank() }
            CatalogScreen(
                cityId = cityId,
                initialPlaceId = placeId,
                onVerEn3dClick = { visorPlaceId ->
                    Intent().setClassName(
                        context.packageName,
                        "com.lospuntoycoma.nicaexplorer.ar.UnityArActivity"
                    ).also { intent ->
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.putExtra("cityId", cityId)
                        // La clave "monumentId" es el contrato con Unity (visor 3D).
                        intent.putExtra("monumentId", visorPlaceId)
                        intent.putExtra("escena", "visor3d")
                        context.startActivity(intent)
                    }
                },
                onAssistantClick = { assistantPlaceId ->
                    navController.navigate(Routes.assistant(assistantPlaceId))
                },
                onComercioClick = { comercioId ->
                    navController.navigate(Routes.comercioDetalle(comercioId))
                },
                onVerSubcategoriasComercio = { padre ->
                    navController.navigate(Routes.comerciosSubcategorias(cityId, padre))
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
                onVerLugar = { placeId ->
                    navController.navigate(
                        Routes.catalogWithPlace(routeCityId, placeId)
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
            route = Routes.COMERCIOS_SUBCATEGORIAS,
            arguments = listOf(
                navArgument("cityId") { type = NavType.StringType },
                navArgument("padre") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val subCityId = backStackEntry.arguments?.getString("cityId") ?: ""
            val padre = backStackEntry.arguments?.getString("padre") ?: ""
            ComerciosSubcategoriasScreen(
                cityId = subCityId,
                padre = padre,
                onSubcategoriaClick = { subcategoria ->
                    navController.navigate(Routes.comerciosSubcategoria(subCityId, subcategoria))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.COMERCIOS_SUBCATEGORIA,
            arguments = listOf(
                navArgument("cityId") { type = NavType.StringType },
                navArgument("subcategoria") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val subCityId = backStackEntry.arguments?.getString("cityId") ?: ""
            val subcategoria = backStackEntry.arguments?.getString("subcategoria") ?: ""
            ComerciosSubcategoriaScreen(
                cityId = subCityId,
                subcategoria = subcategoria,
                onComercioClick = { comercioId ->
                    navController.navigate(Routes.comercioDetalle(comercioId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.AR_PLACEHOLDER,
            arguments = listOf(
                navArgument("cityId") { type = NavType.StringType },
                navArgument("placeId") { type = NavType.StringType }
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
                    userViewModel.loadUserProfile()
                    navController.navigate(Routes.HOME) {
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
                },
                onLogin = {
                    navController.navigate(Routes.LOGIN)
                },
                onRegister = {
                    navController.navigate(Routes.REGISTER)
                },
                onMisComercios = {
                    navController.navigate(Routes.MIS_COMERCIOS)
                }
            )
        }

        composable(Routes.MIS_COMERCIOS) {
            val profile by userViewModel.userProfile.collectAsState()
            val loading by userViewModel.isLoading.collectAsState()
            val isComercio = profile?.rol == com.lospuntoycoma.nicaexplorer.model.UserRole.COMERCIO ||
                profile?.rol == com.lospuntoycoma.nicaexplorer.model.UserRole.ADMIN

            LaunchedEffect(profile, loading) {
                if (!loading && !isComercio) {
                    navController.popBackStack()
                }
            }

            if (isComercio) {
                MisComerciosScreen(
                    onBack = { navController.popBackStack() },
                    onCrear = { navController.navigate(Routes.comercioForm()) },
                    onEditar = { comercioId ->
                        navController.navigate(Routes.comercioForm(comercioId))
                    },
                    onVerPublico = { comercioId ->
                        navController.navigate(Routes.comercioDetalle(comercioId))
                    }
                )
            }
        }

        composable(
            route = Routes.COMERCIO_FORM,
            arguments = listOf(
                navArgument("comercioId") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val formComercioId = backStackEntry.arguments?.getString("comercioId")
                ?.takeIf { it.isNotBlank() }
            ComercioFormScreen(
                comercioId = formComercioId,
                onBack = { navController.popBackStack() },
                onSaved = {
                    navController.navigate(Routes.MIS_COMERCIOS) {
                        popUpTo(Routes.MIS_COMERCIOS) { inclusive = true }
                    }
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
                onSolicitarAparicion = { _ ->
                    if (FirebaseRepository.getCurrentUser() != null) {
                        navController.navigate(Routes.MIS_COMERCIOS)
                    } else {
                        navController.navigate(Routes.REGISTER)
                    }
                },
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
                onOpenPlace = { cityId, placeId ->
                    navController.navigate(Routes.catalogWithPlace(cityId, placeId))
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
                onOpenPlace = { cityId, placeId ->
                    navController.navigate(Routes.catalogWithPlace(cityId, placeId))
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
                navArgument("placeId") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val placeId = backStackEntry.arguments?.getString("placeId")
                ?.takeIf { it.isNotBlank() }
            val placeContext = placeId?.let { id ->
                SampleData.allPlaces.firstOrNull { it.id == id }
            }

            AssistantScreen(
                placeContext = placeContext,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
