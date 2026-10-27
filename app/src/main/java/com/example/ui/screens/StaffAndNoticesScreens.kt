package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NoticeEntity
import com.example.data.model.StaffProfileEntity
import com.example.data.model.UserRole
import com.example.ui.HostelHubViewModel
import com.example.ui.components.RoleBadge
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

// =================== STAFF DIRECTORY ===================

@Composable
fun StaffManagementScreen(
    viewModel: HostelHubViewModel
) {
    val staffList by viewModel.allStaff.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
            .padding(16.dp)
    ) {
        Text(
            text = "Hostel Administration & Staff (${staffList.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = NavyPrimary
        )
        Text(
            text = "Authorized wardens, accountants, and building caretakers.",
            style = MaterialTheme.typography.bodySmall,
            color = SlateMuted
        )

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            items(staffList, key = { it.id }) { staff ->
                StaffCard(staff = staff)
            }
        }
    }
}

@Composable
fun StaffCard(staff: StaffProfileEntity) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("staff_card_${staff.id.lowercase()}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(NavyPrimary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = staff.fullName.take(2).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = staff.fullName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = SlateDark
                    )
                    RoleBadge(role = staff.role)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${staff.id} • ${staff.email}",
                    style = MaterialTheme.typography.bodySmall,
                    color = SlateMuted
                )
                Text(
                    text = "Contact: ${staff.phone} ${if (staff.assignedBuildingId != null) " • Assigned: ${staff.assignedBuildingId}" else ""}",
                    fontSize = 11.sp,
                    color = SlateDark
                )
            }
        }
    }
}

// =================== NOTICES & ALERTS ===================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoticesScreen(
    viewModel: HostelHubViewModel
) {
    val currentRole by viewModel.currentRole.collectAsState()
    val notices by viewModel.activeNotices.collectAsState()

    var selectedCategory by remember { mutableStateOf("ALL") }
    var showPublishDialog by remember { mutableStateOf(false) }

    val filteredNotices = notices.filter {
        if (selectedCategory == "ALL") true else it.category == selectedCategory
    }

    Scaffold(
        floatingActionButton = {
            if (currentRole == UserRole.ADMIN || currentRole == UserRole.WARDEN) {
                ExtendedFloatingActionButton(
                    onClick = { showPublishDialog = true },
                    containerColor = TealPrimary,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Campaign, contentDescription = null) },
                    text = { Text("Publish Notice") },
                    modifier = Modifier.testTag("publish_notice_fab")
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(OffWhite)
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("ALL", "FEE_REMINDER", "MAINTENANCE", "RULES", "ANNOUNCEMENT").forEach { cat ->
                    item {
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.replace("_", " ")) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = TealContainer)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (filteredNotices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.Campaign, contentDescription = null, tint = SlateMuted, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No active announcements", fontWeight = FontWeight.Bold, color = SlateDark)
                        Text("Hostel notifications and reminders will appear here.", color = SlateMuted, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredNotices, key = { it.id }) { notice ->
                        NoticeCard(notice = notice)
                    }
                }
            }
        }
    }

    if (showPublishDialog) {
        PublishNoticeDialog(
            onDismiss = { showPublishDialog = false },
            onPublish = { title, body, cat, priority, audience ->
                viewModel.publishNotice(title, body, cat, priority, audience)
                showPublishDialog = false
            }
        )
    }
}

@Composable
fun NoticeCard(notice: NoticeEntity) {
    val isUrgent = notice.priority == "URGENT" || notice.priority == "HIGH"
    Card(
        modifier = Modifier.fillMaxWidth().testTag("notice_card_${notice.id.lowercase()}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = if (isUrgent) StatusAmberBg.copy(alpha = 0.25f) else CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = if (isUrgent) androidx.compose.foundation.BorderStroke(1.dp, AmberAccent.copy(alpha = 0.5f)) else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isUrgent) Icons.Default.PriorityHigh else Icons.Default.Campaign,
                        contentDescription = null,
                        tint = if (isUrgent) StatusRed else TealPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = notice.category.replace("_", " "),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isUrgent) StatusRed else TealDark
                    )
                }
                Text(
                    text = notice.publishedDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = SlateMuted
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = notice.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SlateDark
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = notice.body,
                style = MaterialTheme.typography.bodyMedium,
                color = SlateDark
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Audience: ${notice.targetAudience.replace("_", " ")}",
                    style = MaterialTheme.typography.labelSmall,
                    color = SlateMuted
                )
                Text(
                    text = "By: ${notice.author}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = NavyPrimary
                )
            }
        }
    }
}

@Composable
fun PublishNoticeDialog(
    onDismiss: () -> Unit,
    onPublish: (title: String, body: String, category: String, priority: String, audience: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("ANNOUNCEMENT") }
    var priority by remember { mutableStateOf("NORMAL") }
    var audience by remember { mutableStateOf("ALL_STUDENTS") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && body.isNotBlank()) {
                        onPublish(title, body, category, priority, audience)
                    }
                },
                enabled = title.isNotBlank() && body.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                modifier = Modifier.testTag("submit_publish_notice")
            ) {
                Text("Publish Notice")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = {
            Text("Publish Official Notice", fontWeight = FontWeight.Bold, color = NavyPrimary)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Notice Headline *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("Notice Body Text *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                Text("Category:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(listOf("ANNOUNCEMENT", "FEE_REMINDER", "MAINTENANCE", "RULES", "MESS")) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.replace("_", " "), fontSize = 10.sp) }
                        )
                    }
                }
                Text("Priority:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("NORMAL", "HIGH", "URGENT").forEach { pr ->
                        FilterChip(
                            selected = priority == pr,
                            onClick = { priority = pr },
                            label = { Text(pr, fontSize = 11.sp) }
                        )
                    }
                }
                Text("Target Audience:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("ALL_STUDENTS", "BUILDING_A", "STAFF").forEach { aud ->
                        FilterChip(
                            selected = audience == aud,
                            onClick = { audience = aud },
                            label = { Text(aud.replace("_", " "), fontSize = 11.sp) }
                        )
                    }
                }
            }
        }
    )
}
