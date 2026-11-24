package com.cookly.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cookly.data.AppContainer
import com.cookly.data.model.Product
import com.cookly.data.model.ProductDraft
import com.cookly.data.model.User
import com.cookly.data.network.OffProduct
import com.cookly.ui.components.CooklyToast
import com.cookly.ui.theme.CooklyColors
import com.cookly.ui.theme.LocalCookly
import com.cookly.ui.screens.*
import com.cookly.viewmodel.*

/**
 * Navegación de quien NO ha iniciado sesión: Login y Registro.
 * Cada destino crea su ViewModel con viewModel { }; queda ligado a esa pantalla de la pila.
 */
@Composable
fun AuthNavHost(container: AppContainer) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            val vm = viewModel { AuthViewModel(container.auth) }
            LoginScreen(vm, onGoToRegister = { navController.navigate(Screen.Register.route) })
        }
        composable(Screen.Register.route) {
            val vm = viewModel { AuthViewModel(container.auth) }
            RegisterScreen(vm, onBack = { navController.popBackStack() })
        }
    }
}

/** Con qué datos se abre el formulario de producto. */
private sealed interface AddRequest {
    data object New : AddRequest
    data class Edit(val productId: Long) : AddRequest
    /** Lleno con datos de Open Food Facts. fromScan = al guardar, el escáner pasa a "Agregado". */
    data class Draft(val draft: ProductDraft, val fromScan: Boolean) : AddRequest
}

/**
 * Navegación de quien SÍ inició sesión: barra inferior con 4 pestañas,
 * y encima se apilan Agregar/Editar producto, Detalle y Acerca de.
 */
@Composable
fun MainNavHost(container: AppContainer, user: User) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    // Estado compartido entre pantallas (se fija antes de navegar).
    var addRequest by remember { mutableStateOf<AddRequest>(AddRequest.New) }
    var detailProduct by remember { mutableStateOf<OffProduct?>(null) }
    var savedFromScan by remember { mutableStateOf<Product?>(null) }
    var toast by remember { mutableStateOf<String?>(null) }

    fun openAdd(request: AddRequest) {
        addRequest = request
        navController.navigate(Screen.AddProduct.route)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (AppTab.entries.any { it.screen.route == currentRoute }) {
                NavigationBar(containerColor = LocalCookly.current.surface) {
                    AppTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.screen.route,
                            onClick = { navController.navigateToTab(tab.screen.route) },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CooklyColors.Primary,
                                selectedTextColor = CooklyColors.Primary,
                                indicatorColor = CooklyColors.Primary.copy(alpha = 0.14f),
                            ),
                        )
                    }
                }
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            NavHost(navController = navController, startDestination = Screen.Nevera.route) {
                composable(Screen.Nevera.route) {
                    val vm = viewModel { NeveraViewModel(container.products, container.prefs, user.id) }
                    NeveraScreen(
                        viewModel = vm,
                        userName = user.name,
                        onAdd = { openAdd(AddRequest.New) },
                        onEdit = { product -> openAdd(AddRequest.Edit(product.id)) },
                        onScan = { navController.navigateToTab(Screen.Scan.route) },
                    )
                }
                composable(Screen.AddProduct.route) {
                    val request = addRequest
                    val vm = viewModel {
                        when (request) {
                            AddRequest.New -> AddProductViewModel(container.products, user.id)
                            is AddRequest.Edit -> AddProductViewModel(container.products, user.id, productId = request.productId)
                            is AddRequest.Draft -> AddProductViewModel(container.products, user.id, draft = request.draft)
                        }
                    }
                    AddProductScreen(
                        viewModel = vm,
                        onClose = { navController.popBackStack() },
                        onSaved = { product, isNew ->
                            if (request is AddRequest.Draft && request.fromScan) savedFromScan = product
                            toast = if (isNew) "Alimento agregado a tu nevera" else "Cambios guardados"
                            navController.popBackStack()
                        },
                    )
                }
                composable(Screen.Scan.route) {
                    val vm = viewModel { ScanViewModel(container.foodApi, container.products, user.id) }
                    // Si el formulario guardó algo que venía del escáner, la tarjeta pasa a "Agregado".
                    LaunchedEffect(savedFromScan) {
                        savedFromScan?.let { vm.noteSaved(it); savedFromScan = null }
                    }
                    ScanScreen(
                        viewModel = vm,
                        onEditDraft = { draft -> openAdd(AddRequest.Draft(draft, fromScan = draft.name.isNotEmpty())) },
                        onShowDetail = { product ->
                            detailProduct = product
                            navController.navigate(Screen.Detail.route)
                        },
                    )
                }
                composable(Screen.Detail.route) {
                    val initial = detailProduct
                    if (initial != null) {
                        val vm = viewModel { ProductDetailViewModel(container.foodApi, initial) }
                        ProductDetailScreen(
                            viewModel = vm,
                            onClose = { navController.popBackStack() },
                            onAdd = { draft -> openAdd(AddRequest.Draft(draft, fromScan = false)) },
                        )
                    }
                }
                composable(Screen.Reminders.route) {
                    val vm = viewModel { RemindersViewModel(container.reminders, user.id) }
                    RemindersScreen(vm)
                }
                composable(Screen.Profile.route) {
                    val vm = viewModel { ProfileViewModel(container.auth, container.prefs) }
                    ProfileScreen(vm, onAbout = { navController.navigate(Screen.About.route) })
                }
                composable(Screen.About.route) {
                    AboutScreen(onBack = { navController.popBackStack() })
                }
            }

            // Aviso flotante "Alimento agregado a tu nevera"
            CooklyToast(
                message = toast,
                onDismiss = { toast = null },
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp),
            )
        }
    }
}

/** Cambia de pestaña sin apilar pantallas repetidas y conservando el estado de cada pestaña. */
private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
