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
import com.example.data.model.LeaveRequestEntity
import com.example.data.model.StudentProfileEntity
import com.example.data.model.UserRole
import com.example.data.model.VisitorEntity
import com.example.ui.HostelHubViewModel
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

// =================== LEAVE MANAGEMENT ===================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveManagementScreen(
    viewModel: HostelHubViewModel
) {
    val currentRole by viewModel.currentRole.collectAsState()
    val allRequests by viewModel.allLeaveRequests.collectAsState()
    val studentRequests by viewModel.studentLeaveRequests.collectAsState()

    val requests = if (currentRole == UserRole.STUDENT) studentRequests else allRequests

    var showApplyDialog by remember { mutableStateOf(false) }
    var requestToReview by remember { mutableStateOf<LeaveRequestEntity?>(null) }
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filtered = requests.filter {
        if (selectedFilter == "ALL") true else it.status == selectedFilter
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showApplyDialog = true },
                containerColor = TealPrimary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.FlightTakeoff, contentDescription = null) },
                text = { Text("Apply for Leave") },
                modifier = Modifier.testTag("apply_leave_fab")
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

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("ALL", "PENDING", "APPROVED", "REJECTED").forEach { f ->
                    item {
                        FilterChip(
                            selected = selectedFilter == f,
                            onClick = { selectedFilter = f },
                            label = { Text(f) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = TealContainer)
                        )
                    }
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
                        Icon(Icons.Outlined.FlightTakeoff, contentDescription = null, tint = SlateMuted, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No leave requests found", fontWeight = FontWeight.Bold, color = SlateDark)
                        Text("Submitted hostel leave passes will appear here.", color = SlateMuted, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filtered, key = { it.id }) { req ->
                        LeaveRequestCard(
                            request = req,
                            canReview = currentRole == UserRole.ADMIN || currentRole == UserRole.WARDEN,
                            onReviewClick = { requestToReview = req }
                        )
                    }
                }
            }
        }
    }

    if (showApplyDialog) {
        ApplyLeaveDialog(
            onDismiss = { showApplyDialog = false },
            onSubmit = { start, end, dest, reason, contact ->
                viewModel.submitLeaveRequest(start, end, dest, reason, contact)
                showApplyDialog = false
            }
        )
    }

    if (requestToReview != null) {
        ReviewLeaveDialog(
            request = requestToReview!!,
            onDismiss = { requestToReview = null },
            onReview = { approved, notes ->
                viewModel.reviewLeaveRequest(requestToReview!!.id, approved, notes)
                requestToReview = null
            }
        )
    }
}

@Composable
fun LeaveRequestCard(
    request: LeaveRequestEntity,
    canReview: Boolean,
    onReviewClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("leave_card_${request.id.lowercase()}"),
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
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(TealContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.FlightTakeoff, contentDescription = null, tint = OnTealContainer, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = request.studentName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = SlateDark
                        )
                        Text(
                            text = "${request.studentId} • Room ${request.roomNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateMuted
                        )
                    }
                }
                StatusBadge(status = request.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                color = SlateLight,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Destination: ${request.destination}",
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Departure: ${request.startDate}  |  Return: ${request.endDate}",
                        fontSize = 11.sp,
                        color = SlateDark
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Reason: ${request.reason}",
                        fontSize = 11.sp,
                        color = SlateMuted
                    )
                    Text(
                        text = "Emergency Phone: ${request.emergencyContact}",
                        fontSize = 11.sp,
                        color = SlateMuted
                    )
                }
            }

            if (request.reviewNotes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Warden Remarks: ${request.reviewNotes} (${request.reviewedBy ?: "Warden"})",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (request.status == "APPROVED") StatusGreen else StatusRed,
                    fontWeight = FontWeight.Medium
                )
            }

            if (canReview && request.status == "PENDING") {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onReviewClick,
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        modifier = Modifier.height(32.dp).testTag("review_leave_${request.id.lowercase()}")
                    ) {
                        Icon(Icons.Default.RateReview, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Approve / Reject", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ApplyLeaveDialog(
    onDismiss: () -> Unit,
    onSubmit: (start: String, end: String, dest: String, reason: String, emergency: String) -> Unit
) {
    var startDate by remember { mutableStateOf("2024-11-01 10:00") }
    var endDate by remember { mutableStateOf("2024-11-03 18:00") }
    var destination by remember { mutableStateOf("Lahore") }
    var reason by remember { mutableStateOf("Weekend visit to family") }
    var emergency by remember { mutableStateOf("+92 301 2233445 (Father)") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (destination.isNotBlank() && reason.isNotBlank()) {
                        onSubmit(startDate, endDate, destination, reason, emergency)
                    }
                },
                enabled = destination.isNotBlank() && reason.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                modifier = Modifier.testTag("submit_apply_leave")
            ) {
                Text("Submit Application")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = {
            Text("Apply for Hostel Leave Pass", fontWeight = FontWeight.Bold, color = NavyPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = destination,
                    onValueChange = { destination = it },
                    label = { Text("Destination City / Address *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = startDate,
                    onValueChange = { startDate = it },
                    label = { Text("Departure Date & Time") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = endDate,
                    onValueChange = { endDate = it },
                    label = { Text("Expected Return Date & Time") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason for Leave *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                OutlinedTextField(
                    value = emergency,
                    onValueChange = { emergency = it },
                    label = { Text("Emergency Contact (Guardian)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }
    )
}

@Composable
fun ReviewLeaveDialog(
    request: LeaveRequestEntity,
    onDismiss: () -> Unit,
    onReview: (approved: Boolean, notes: String) -> Unit
) {
    var remarks by remember { mutableStateOf("Verified with guardian. Gate pass authorized.") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { onReview(true, remarks) },
                colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                modifier = Modifier.testTag("approve_leave_button")
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Approve")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = { onReview(false, remarks) },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusRed),
                modifier = Modifier.testTag("reject_leave_button")
            ) {
                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reject")
            }
        },
        title = {
            Text("Review Leave: ${request.studentName}", fontWeight = FontWeight.Bold, color = NavyPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Destination: ${request.destination}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("Dates: ${request.startDate} to ${request.endDate}", fontSize = 12.sp, color = SlateMuted)
                Text("Reason: ${request.reason}", fontSize = 12.sp, color = SlateDark)
                Text("Contact: ${request.emergencyContact}", fontSize = 12.sp, color = SlateMuted)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Warden Remarks / Verification Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        }
    )
}

// =================== VISITOR MANAGEMENT ===================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitorManagementScreen(
    viewModel: HostelHubViewModel
) {
    val visitors by viewModel.allVisitors.collectAsState()
    val students by viewModel.allStudents.collectAsState()

    var showRegisterDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("CHECKED_IN") }

    val filteredVisitors = visitors.filter {
        if (selectedFilter == "ALL") true else it.status == selectedFilter
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showRegisterDialog = true },
                containerColor = NavyPrimary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Badge, contentDescription = null) },
                text = { Text("Register Visitor") },
                modifier = Modifier.testTag("register_visitor_fab")
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

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("CHECKED_IN", "ALL", "CHECKED_OUT").forEach { f ->
                    item {
                        FilterChip(
                            selected = selectedFilter == f,
                            onClick = { selectedFilter = f },
                            label = { Text(if (f == "CHECKED_IN") "Active In-Hostel" else f.replace("_", " ")) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = TealContainer)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (filteredVisitors.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.Badge, contentDescription = null, tint = SlateMuted, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No visitor records found", fontWeight = FontWeight.Bold, color = SlateDark)
                        Text("Register guest arrivals at hostel entrance gates.", color = SlateMuted, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredVisitors, key = { it.id }) { visitor ->
                        VisitorCard(
                            visitor = visitor,
                            onCheckoutClick = { viewModel.checkoutVisitor(visitor) }
                        )
                    }
                }
            }
        }
    }

    if (showRegisterDialog) {
        RegisterVisitorDialog(
            students = students.filter { it.status == "ACTIVE" },
            onDismiss = { showRegisterDialog = false },
            onRegister = { name, phone, cnic, stdId, rel, purpose ->
                viewModel.registerVisitor(name, phone, cnic, stdId, rel, purpose)
                showRegisterDialog = false
            }
        )
    }
}

@Composable
fun VisitorCard(
    visitor: VisitorEntity,
    onCheckoutClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("visitor_card_${visitor.id.lowercase()}"),
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
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(NavySurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = visitor.visitorName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = SlateDark
                        )
                        Text(
                            text = "CNIC: ${visitor.cnicOrId} • ${visitor.contactNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateMuted
                        )
                    }
                }
                StatusBadge(status = visitor.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                color = SlateLight,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Visiting: ${visitor.studentName} (${visitor.relationship})",
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Purpose: ${visitor.purpose}",
                        fontSize = 11.sp,
                        color = SlateDark
                    )
                    Text(
                        text = "Entry: ${visitor.entryTime} ${if (visitor.exitTime != null) " • Exit: ${visitor.exitTime}" else ""}",
                        fontSize = 11.sp,
                        color = SlateMuted
                    )
                }
            }

            if (visitor.status == "CHECKED_IN") {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onCheckoutClick,
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        modifier = Modifier.height(32.dp).testTag("checkout_visitor_${visitor.id.lowercase()}")
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Record Exit", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun RegisterVisitorDialog(
    students: List<StudentProfileEntity>,
    onDismiss: () -> Unit,
    onRegister: (name: String, phone: String, cnic: String, studentId: String, relation: String, purpose: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("+92 3") }
    var cnic by remember { mutableStateOf("35201-") }
    var selectedStudentId by remember { mutableStateOf(students.firstOrNull()?.id ?: "") }
    var relation by remember { mutableStateOf("Father") }
    var purpose by remember { mutableStateOf("Family meeting and dropping supplies") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && selectedStudentId.isNotBlank()) {
                        onRegister(name, phone, cnic, selectedStudentId, relation, purpose)
                    }
                },
                enabled = name.isNotBlank() && selectedStudentId.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                modifier = Modifier.testTag("submit_register_visitor")
            ) {
                Text("Log Visitor Entry")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = {
            Text("Gate Visitor Registration", fontWeight = FontWeight.Bold, color = NavyPrimary)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Visitor Full Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Contact Phone") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = cnic,
                    onValueChange = { cnic = it },
                    label = { Text("CNIC / National ID Reference") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text("Student Being Visited:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SlateDark)
                students.take(4).forEach { s ->
                    Surface(
                        onClick = { selectedStudentId = s.id },
                        shape = RoundedCornerShape(8.dp),
                        color = if (selectedStudentId == s.id) TealContainer else SlateLight,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${s.fullName} (${s.id})", fontSize = 12.sp)
                            RadioButton(selected = selectedStudentId == s.id, onClick = { selectedStudentId = s.id })
                        }
                    }
                }

                OutlinedTextField(
                    value = relation,
                    onValueChange = { relation = it },
                    label = { Text("Relationship (e.g. Father, Brother, Friend)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = purpose,
                    onValueChange = { purpose = it },
                    label = { Text("Purpose of Visit") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }
    )
}
