package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PaymentEntity
import com.example.data.model.UserRole
import com.example.ui.AppNavDestination
import com.example.ui.theme.*

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    accentColor: Color = TealPrimary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("stat_card_${title.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = SlateMuted,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = SlateDark
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (status.uppercase()) {
        "PAID", "APPROVED", "RESOLVED", "ACTIVE", "OPERATIONAL" -> Pair(StatusGreenBg, StatusGreen)
        "UNPAID", "REJECTED", "OVERDUE", "UNDER_MAINTENANCE" -> Pair(StatusRedBg, StatusRed)
        "PARTIALLY_PAID", "PENDING", "IN_PROGRESS" -> Pair(StatusAmberBg, StatusAmber)
        "CHECKED_IN", "TRANSFERRED" -> Pair(StatusBlueBg, StatusBlue)
        "CHECKED_OUT", "CLOSED", "INACTIVE" -> Pair(SlateLight, SlateMuted)
        else -> Pair(SlateLight, SlateDark)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = status.replace("_", " "),
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
fun RoleBadge(role: UserRole) {
    val (bg, fg) = when (role) {
        UserRole.ADMIN -> Pair(NavyPrimary, Color.White)
        UserRole.WARDEN -> Pair(TealPrimary, Color.White)
        UserRole.ACCOUNTANT -> Pair(AmberDark, Color.White)
        UserRole.STUDENT -> Pair(StatusBlue, Color.White)
    }
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = bg,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Text(
            text = role.name,
            color = fg,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostelTopBar(
    title: String,
    currentRole: UserRole,
    userName: String,
    unreadNotifications: Int,
    onMenuClick: () -> Unit,
    onRoleSwitchClick: () -> Unit,
    onNotificationsClick: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "HostelHub",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = NavyPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    RoleBadge(role = currentRole)
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = SlateMuted
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick, modifier = Modifier.testTag("nav_menu_button")) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = NavyPrimary)
            }
        },
        actions = {
            // Quick role switch button for demonstration
            OutlinedButton(
                onClick = onRoleSwitchClick,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                modifier = Modifier
                    .height(32.dp)
                    .testTag("switch_role_button")
            ) {
                Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Role", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }

            // Notification Bell with badge
            IconButton(onClick = onNotificationsClick, modifier = Modifier.testTag("notifications_bell")) {
                BadgedBox(badge = {
                    if (unreadNotifications > 0) {
                        Badge(containerColor = StatusRed) {
                            Text(unreadNotifications.toString(), color = Color.White)
                        }
                    }
                }) {
                    Icon(Icons.Outlined.Notifications, contentDescription = "Notifications", tint = NavyPrimary)
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = CardSurface)
    )
}

@Composable
fun PrintableReceiptDialog(
    payment: PaymentEntity,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                modifier = Modifier.testTag("receipt_done_button")
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Done")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share / Print")
            }
        },
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "HOSTELHUB ACCOUNTS",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )
                Text(
                    text = "Official Payment Receipt",
                    style = MaterialTheme.typography.bodySmall,
                    color = TealPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        text = {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateLight),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ReceiptRow(label = "Receipt No:", value = payment.paymentReference, isBold = true)
                    ReceiptRow(label = "Date:", value = payment.paymentDate)
                    ReceiptRow(label = "Student Name:", value = payment.studentName)
                    ReceiptRow(label = "Student ID:", value = payment.studentId)
                    ReceiptRow(label = "Invoice Ref:", value = payment.invoiceId)
                    ReceiptRow(label = "Payment Method:", value = payment.paymentMethod.replace("_", " "))
                    if (payment.notes.isNotEmpty()) {
                        ReceiptRow(label = "Notes:", value = payment.notes)
                    }
                    Divider(modifier = Modifier.padding(vertical = 10.dp), color = SlateBorder)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AMOUNT RECEIVED",
                            fontWeight = FontWeight.Bold,
                            color = SlateDark,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Rs. ${String.format("%,.0f", payment.amount)}",
                            fontWeight = FontWeight.ExtraBold,
                            color = StatusGreen,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Cashier: ${payment.recordedBy} • Computer Generated",
                        style = MaterialTheme.typography.labelSmall,
                        color = SlateMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    )
}

@Composable
fun ReceiptRow(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = SlateMuted)
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = SlateDark
        )
    }
}
