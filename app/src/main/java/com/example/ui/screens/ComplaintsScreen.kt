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
import com.example.data.model.ComplaintEntity
import com.example.data.model.UserRole
import com.example.ui.HostelHubViewModel
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplaintsScreen(
    viewModel: HostelHubViewModel
) {
    val currentRole by viewModel.currentRole.collectAsState()
    val allComplaints by viewModel.allComplaints.collectAsState()
    val studentComplaints by viewModel.studentComplaints.collectAsState()

    val complaints = if (currentRole == UserRole.STUDENT) studentComplaints else allComplaints

    var selectedStatus by remember { mutableStateOf("ALL") }
    var showSubmitDialog by remember { mutableStateOf(false) }
    var complaintToResolve by remember { mutableStateOf<ComplaintEntity?>(null) }

    val filtered = complaints.filter {
        if (selectedStatus == "ALL") true else it.status == selectedStatus
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showSubmitDialog = true },
                containerColor = StatusRed,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.AddComment, contentDescription = null) },
                text = { Text("Report Issue") },
                modifier = Modifier.testTag("report_issue_fab")
            )
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

            // Filters
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val statuses = listOf("ALL", "PENDING", "IN_PROGRESS", "RESOLVED", "CLOSED")
                items(statuses) { st ->
                    FilterChip(
                        selected = selectedStatus == st,
                        onClick = { selectedStatus = st },
                        label = { Text(st.replace("_", " ")) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = TealContainer)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No complaints found", fontWeight = FontWeight.Bold, color = SlateDark)
                        Text("Tap 'Report Issue' to lodge a maintenance request.", color = SlateMuted, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filtered, key = { it.id }) { complaint ->
                        ComplaintCard(
                            complaint = complaint,
                            canResolve = currentRole == UserRole.ADMIN || currentRole == UserRole.WARDEN,
                            onResolveClick = { complaintToResolve = complaint }
                        )
                    }
                }
            }
        }
    }

    // Submit Complaint Dialog
    if (showSubmitDialog) {
        SubmitComplaintDialog(
            onDismiss = { showSubmitDialog = false },
            onSubmit = { cat, title, desc, priority ->
                viewModel.submitComplaint(cat, title, desc, priority)
                showSubmitDialog = false
            }
        )
    }

    // Resolve / Manage Complaint Dialog
    if (complaintToResolve != null) {
        ResolveComplaintDialog(
            complaint = complaintToResolve!!,
            onDismiss = { complaintToResolve = null },
            onUpdate = { newStatus, staff, notes ->
                viewModel.updateComplaintStatus(complaintToResolve!!.id, newStatus, staff, notes)
                complaintToResolve = null
            }
        )
    }
}

@Composable
fun ComplaintCard(
    complaint: ComplaintEntity,
    canResolve: Boolean,
    onResolveClick: () -> Unit
) {
    val priorityColor = when (complaint.priority) {
        "URGENT" -> StatusRed
        "HIGH" -> StatusAmber
        "MEDIUM" -> StatusBlue
        else -> SlateMuted
    }

    Card(
        modifier = Modifier.fillMaxWidth().testTag("complaint_card_${complaint.ticketNumber.lowercase()}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(priorityColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null, tint = priorityColor, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = complaint.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = SlateDark
                        )
                        Text(
                            text = "${complaint.ticketNumber} • ${complaint.category}",
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateMuted
                        )
                    }
                }
                StatusBadge(status = complaint.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = complaint.description,
                style = MaterialTheme.typography.bodySmall,
                color = SlateDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = SlateLight,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Location: Room ${complaint.roomNumber} (${complaint.buildingName})",
                        fontSize = 11.sp,
                        color = SlateMuted
                    )
                    Text(
                        text = "By: ${complaint.studentName}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SlateDark
                    )
                }
            }

            if (complaint.resolutionNotes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Resolution Notes: ${complaint.resolutionNotes} (${complaint.assignedStaff ?: "Warden"})",
                    style = MaterialTheme.typography.labelSmall,
                    color = StatusGreen,
                    fontWeight = FontWeight.Medium
                )
            }

            if (canResolve && complaint.status != "CLOSED") {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onResolveClick,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TealPrimary),
                        modifier = Modifier.height(32.dp).testTag("manage_complaint_${complaint.ticketNumber.lowercase()}")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Update Status", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun SubmitComplaintDialog(
    onDismiss: () -> Unit,
    onSubmit: (category: String, title: String, description: String, priority: String) -> Unit
) {
    var category by remember { mutableStateOf("ELECTRICITY") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("MEDIUM") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && description.isNotBlank()) {
                        onSubmit(category, title, description, priority)
                    }
                },
                enabled = title.isNotBlank() && description.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = StatusRed),
                modifier = Modifier.testTag("submit_complaint_button")
            ) {
                Text("Submit Ticket")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = {
            Text("Lodge Maintenance Request", fontWeight = FontWeight.Bold, color = NavyPrimary)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Category:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SlateDark)
                val categories = listOf("ELECTRICITY", "WATER_SUPPLY", "PLUMBING", "INTERNET", "FURNITURE", "CLEANING", "OTHER")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.replace("_", " "), fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = TealContainer)
                        )
                    }
                }

                Text("Priority:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SlateDark)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("LOW", "MEDIUM", "HIGH", "URGENT").forEach { pr ->
                        FilterChip(
                            selected = priority == pr,
                            onClick = { priority = pr },
                            label = { Text(pr, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AmberContainer)
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Short Issue Title *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Detailed Description *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        }
    )
}

@Composable
fun ResolveComplaintDialog(
    complaint: ComplaintEntity,
    onDismiss: () -> Unit,
    onUpdate: (newStatus: String, assignedStaff: String, notes: String) -> Unit
) {
    var status by remember { mutableStateOf(complaint.status) }
    var staff by remember { mutableStateOf(complaint.assignedStaff ?: "Plumber / Electrician") }
    var notes by remember { mutableStateOf(complaint.resolutionNotes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { onUpdate(status, staff, notes) },
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                modifier = Modifier.testTag("save_complaint_status")
            ) {
                Text("Update Ticket")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = {
            Text("Manage Complaint ${complaint.ticketNumber}", fontWeight = FontWeight.Bold, color = NavyPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Update Status:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SlateDark)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("IN_PROGRESS", "RESOLVED", "CLOSED").forEach { st ->
                        FilterChip(
                            selected = status == st,
                            onClick = { status = st },
                            label = { Text(st.replace("_", " "), fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = TealContainer)
                        )
                    }
                }
                OutlinedTextField(
                    value = staff,
                    onValueChange = { staff = it },
                    label = { Text("Assigned Maintenance Staff") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Resolution Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        }
    )
}
