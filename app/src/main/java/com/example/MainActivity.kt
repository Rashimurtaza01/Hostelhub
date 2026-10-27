package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.ui.AppNavDestination
import com.example.ui.HostelHubViewModel
import com.example.ui.components.HostelNavDrawerContent
import com.example.ui.components.HostelTopBar
import com.example.ui.components.RoleSwitcherDialog
import com.example.ui.screens.*
import com.example.ui.theme.HostelHubTheme
import com.example.ui.theme.OffWhite
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: HostelHubViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HostelHubTheme {
                HostelHubApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun HostelHubApp(viewModel: HostelHubViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()
    val currentDestination by viewModel.currentDestination.collectAsState()
    val notifications by viewModel.allNotifications.collectAsState()
    val unreadNotifications by viewModel.unreadNotificationsCount.collectAsState()
    val uiFeedback by viewModel.uiFeedback.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showRoleDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }

    // Display Toast / Feedback Snackbar
    LaunchedEffect(uiFeedback) {
        uiFeedback?.let {
            snackbarHostState.showSnackbar(
                message = it.message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearFeedback()
        }
    }

    // Handle back button: if drawer is open, close it; if not on Dashboard, navigate to Dashboard
    BackHandler(enabled = drawerState.isOpen || currentDestination != AppNavDestination.DASHBOARD) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else if (currentDestination != AppNavDestination.DASHBOARD) {
            viewModel.navigateTo(AppNavDestination.DASHBOARD)
        }
    }

    // If logged out, display Auth Screen
    if (currentUser == null) {
        AuthScreen(
            viewModel = viewModel,
            onLoginSuccess = {
                viewModel.navigateTo(AppNavDestination.DASHBOARD)
            }
        )
        return
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            HostelNavDrawerContent(
                currentUser = currentUser,
                currentRole = currentRole,
                currentDestination = currentDestination,
                onNavigate = { dest ->
                    viewModel.navigateTo(dest)
                    scope.launch { drawerState.close() }
                },
                onRoleSwitchClick = {
                    showRoleDialog = true
                    scope.launch { drawerState.close() }
                },
                onLogoutClick = {
                    viewModel.logout()
                    scope.launch { drawerState.close() }
                },
                isAuthorized = { dest -> viewModel.isAuthorizedFor(dest) }
            )
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                HostelTopBar(
                    title = currentDestination.title,
                    currentRole = currentRole,
                    userName = currentUser?.fullName ?: "Resident",
                    unreadNotifications = unreadNotifications,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onRoleSwitchClick = { showRoleDialog = true },
                    onNotificationsClick = { showNotificationsDialog = true }
                )
            },
            contentWindowInsets = WindowInsets.safeDrawing,
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(OffWhite)
                    .padding(paddingValues)
            ) {
                when (currentDestination) {
                    AppNavDestination.DASHBOARD -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigate = { dest -> viewModel.navigateTo(dest) }
                    )
                    AppNavDestination.STUDENTS -> StudentManagementScreen(viewModel = viewModel)
                    AppNavDestination.BUILDINGS -> BuildingAndRoomScreen(viewModel = viewModel)
                    AppNavDestination.ALLOCATION -> BedAllocationScreen(viewModel = viewModel)
                    AppNavDestination.FEES -> FeeManagementScreen(viewModel = viewModel)
                    AppNavDestination.COMPLAINTS -> ComplaintsScreen(viewModel = viewModel)
                    AppNavDestination.LEAVE -> LeaveManagementScreen(viewModel = viewModel)
                    AppNavDestination.VISITORS -> VisitorManagementScreen(viewModel = viewModel)
                    AppNavDestination.MESS -> MessManagementScreen(viewModel = viewModel)
                    AppNavDestination.STAFF -> StaffManagementScreen(viewModel = viewModel)
                    AppNavDestination.NOTICES -> NoticesScreen(viewModel = viewModel)
                    AppNavDestination.REPORTS -> ReportsScreen(viewModel = viewModel)
                    AppNavDestination.AUDIT_LOGS -> AuditLogScreen(viewModel = viewModel)
                    AppNavDestination.PROFILE -> SettingsAndProfileScreen(
                        viewModel = viewModel,
                        onRoleSwitchClick = { showRoleDialog = true }
                    )
                }
            }
        }
    }

    // Role Switcher Dialog for FYP Demo
    if (showRoleDialog) {
        RoleSwitcherDialog(
            currentRole = currentRole,
            onRoleSelected = { role ->
                viewModel.setDemoUser(role)
                showRoleDialog = false
            },
            onDismiss = { showRoleDialog = false }
        )
    }

    // In-App Notifications Dialog
    if (showNotificationsDialog) {
        NotificationsDialog(
            notifications = notifications,
            onDismiss = { showNotificationsDialog = false },
            onMarkAsRead = { id -> viewModel.markNotificationAsRead(id) }
        )
    }
}
