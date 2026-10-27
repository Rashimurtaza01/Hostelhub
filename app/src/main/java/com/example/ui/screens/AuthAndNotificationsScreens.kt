package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NotificationEntity
import com.example.data.model.UserRole
import com.example.ui.HostelHubViewModel
import com.example.ui.components.RoleBadge
import com.example.ui.theme.*

@Composable
fun AuthScreen(
    viewModel: HostelHubViewModel,
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("admin@hostelhub.com") }
    var password by remember { mutableStateOf("admin123") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Logo & Branding
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(NavyPrimary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Apartment,
                contentDescription = "HostelHub Logo",
                tint = TealLight,
                modifier = Modifier.size(42.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "HostelHub",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = NavyPrimary
        )
        Text(
            text = "Smart Living. Simple Management.",
            style = MaterialTheme.typography.bodyMedium,
            color = TealPrimary,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Login Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Sign In to Your Account",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SlateDark
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = SlateMuted) },
                    modifier = Modifier.fillMaxWidth().testTag("login_email_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = SlateMuted) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().testTag("login_password_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        isLoading = true
                        viewModel.login(email, password) { success ->
                            isLoading = false
                            if (success) onLoginSuccess()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("login_submit_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Role Demonstration Switcher
        Text(
            text = "Quick 1-Click Login (Demonstration Roles):",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = SlateMuted
        )
        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DemoLoginButton(
                    role = UserRole.ADMIN,
                    name = "Admin",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.setDemoUser(UserRole.ADMIN)
                        onLoginSuccess()
                    }
                )
                DemoLoginButton(
                    role = UserRole.WARDEN,
                    name = "Warden",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.setDemoUser(UserRole.WARDEN)
                        onLoginSuccess()
                    }
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DemoLoginButton(
                    role = UserRole.ACCOUNTANT,
                    name = "Accountant",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.setDemoUser(UserRole.ACCOUNTANT)
                        onLoginSuccess()
                    }
                )
                DemoLoginButton(
                    role = UserRole.STUDENT,
                    name = "Student",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.setDemoUser(UserRole.STUDENT)
                        onLoginSuccess()
                    }
                )
            }
        }
    }
}

@Composable
fun DemoLoginButton(
    role: UserRole,
    name: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(38.dp).testTag("demo_login_${name.lowercase()}"),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyPrimary)
    ) {
        RoleBadge(role = role)
        Spacer(modifier = Modifier.width(4.dp))
        Text(name, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

// =================== NOTIFICATIONS MODAL ===================

@Composable
fun NotificationsDialog(
    notifications: List<NotificationEntity>,
    onDismiss: () -> Unit,
    onMarkAsRead: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = NavyPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("HostelHub In-App Alerts", fontWeight = FontWeight.Bold, color = NavyPrimary)
            }
        },
        text = {
            if (notifications.isEmpty()) {
                Text("No notifications right now.", color = SlateMuted)
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 350.dp)
                ) {
                    items(notifications, key = { it.id }) { notif ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (notif.isRead) SlateLight else TealContainer.copy(alpha = 0.4f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onMarkAsRead(notif.id) }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = notif.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = SlateDark
                                    )
                                    if (!notif.isRead) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(TealPrimary)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = notif.message,
                                    fontSize = 11.sp,
                                    color = SlateMuted
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}
