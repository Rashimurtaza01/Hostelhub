package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.ui.AppNavDestination
import com.example.ui.theme.*

data class NavItem(
    val destination: AppNavDestination,
    val icon: ImageVector,
    val selectedIcon: ImageVector
)

val allNavItems = listOf(
    NavItem(AppNavDestination.DASHBOARD, Icons.Outlined.Dashboard, Icons.Filled.Dashboard),
    NavItem(AppNavDestination.STUDENTS, Icons.Outlined.People, Icons.Filled.People),
    NavItem(AppNavDestination.BUILDINGS, Icons.Outlined.Domain, Icons.Filled.Domain),
    NavItem(AppNavDestination.ALLOCATION, Icons.Outlined.Hotel, Icons.Filled.Hotel),
    NavItem(AppNavDestination.FEES, Icons.Outlined.ReceiptLong, Icons.Filled.ReceiptLong),
    NavItem(AppNavDestination.COMPLAINTS, Icons.Outlined.ReportProblem, Icons.Filled.ReportProblem),
    NavItem(AppNavDestination.LEAVE, Icons.Outlined.FlightTakeoff, Icons.Filled.FlightTakeoff),
    NavItem(AppNavDestination.VISITORS, Icons.Outlined.Badge, Icons.Filled.Badge),
    NavItem(AppNavDestination.MESS, Icons.Outlined.RestaurantMenu, Icons.Filled.RestaurantMenu),
    NavItem(AppNavDestination.STAFF, Icons.Outlined.Engineering, Icons.Filled.Engineering),
    NavItem(AppNavDestination.NOTICES, Icons.Outlined.Campaign, Icons.Filled.Campaign),
    NavItem(AppNavDestination.REPORTS, Icons.Outlined.Assessment, Icons.Filled.Assessment),
    NavItem(AppNavDestination.AUDIT_LOGS, Icons.Outlined.History, Icons.Filled.History),
    NavItem(AppNavDestination.PROFILE, Icons.Outlined.Settings, Icons.Filled.Settings)
)

@Composable
fun HostelNavDrawerContent(
    currentUser: UserEntity?,
    currentRole: UserRole,
    currentDestination: AppNavDestination,
    onNavigate: (AppNavDestination) -> Unit,
    onRoleSwitchClick: () -> Unit,
    onLogoutClick: () -> Unit,
    isAuthorized: (AppNavDestination) -> Boolean
) {
    ModalDrawerSheet(
        modifier = Modifier.width(310.dp),
        drawerContainerColor = CardSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(NavyPrimary)
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(TealPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentUser?.fullName?.take(2)?.uppercase() ?: "HH",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        RoleBadge(role = currentRole)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = currentUser?.fullName ?: "Hostel Resident",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = currentUser?.email ?: "user@hostelhub.com",
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateBorder
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Role Switcher Banner
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clickable { onRoleSwitchClick() },
                shape = RoundedCornerShape(10.dp),
                color = TealContainer.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(1.dp, TealPrimary.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SwitchAccount, contentDescription = null, tint = OnTealContainer, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Switch Role (${currentRole.name})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OnTealContainer
                        )
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = OnTealContainer, modifier = Modifier.size(16.dp))
                }
            }

            Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = SlateBorder)

            // Scrollable Menu Items
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp)
            ) {
                allNavItems.forEach { item ->
                    val allowed = isAuthorized(item.destination)
                    if (allowed) {
                        val isSelected = currentDestination == item.destination
                        NavigationDrawerItem(
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.icon,
                                    contentDescription = item.destination.title,
                                    tint = if (isSelected) TealPrimary else SlateMuted
                                )
                            },
                            label = {
                                Text(
                                    text = item.destination.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) NavyPrimary else SlateDark,
                                    fontSize = 13.sp
                                )
                            },
                            selected = isSelected,
                            onClick = { onNavigate(item.destination) },
                            modifier = Modifier
                                .padding(vertical = 2.dp)
                                .testTag("nav_item_${item.destination.name.lowercase()}"),
                            shape = RoundedCornerShape(12.dp),
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = TealContainer.copy(alpha = 0.4f),
                                unselectedContainerColor = Color.Transparent
                            )
                        )
                    }
                }
            }

            Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp), color = SlateBorder)

            // Logout action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onLogoutClick() }
                    .padding(8.dp)
                    .testTag("nav_logout_button"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Logout, contentDescription = "Logout", tint = StatusRed, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Log Out / Switch Account",
                    color = StatusRed,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun RoleSwitcherDialog(
    currentRole: UserRole,
    onRoleSelected: (UserRole) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = NavyPrimary)
            }
        },
        title = {
            Text("Switch Role (FYP Demonstration)", fontWeight = FontWeight.Bold, color = NavyPrimary)
        },
        text = {
            Column {
                Text(
                    text = "Select any authorized role to test its specific views, permissions, and features in real-time:",
                    style = MaterialTheme.typography.bodySmall,
                    color = SlateMuted
                )
                Spacer(modifier = Modifier.height(12.dp))
                UserRole.values().forEach { role ->
                    val isCurrent = role == currentRole
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                onRoleSelected(role)
                                onDismiss()
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isCurrent) TealContainer else SlateLight,
                        border = if (isCurrent) androidx.compose.foundation.BorderStroke(1.5.dp, TealPrimary) else null
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = role.name,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCurrent) OnTealContainer else SlateDark,
                                        fontSize = 14.sp
                                    )
                                    if (isCurrent) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("(Active)", fontSize = 11.sp, color = TealDark, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Text(
                                    text = when (role) {
                                        UserRole.ADMIN -> "Full access to all 14 modules & audit logs"
                                        UserRole.WARDEN -> "Rooms, beds, complaints, leaves, visitors, notices"
                                        UserRole.ACCOUNTANT -> "Fees, invoices, receipts, expenses, finance"
                                        UserRole.STUDENT -> "Personal room, fee receipts, complaints, leaves"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SlateMuted
                                )
                            }
                            RadioButton(
                                selected = isCurrent,
                                onClick = {
                                    onRoleSelected(role)
                                    onDismiss()
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = TealPrimary)
                            )
                        }
                    }
                }
            }
        }
    )
}
