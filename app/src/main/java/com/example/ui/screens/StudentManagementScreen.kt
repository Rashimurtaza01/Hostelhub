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
import com.example.data.model.StudentProfileEntity
import com.example.ui.HostelHubViewModel
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentManagementScreen(
    viewModel: HostelHubViewModel
) {
    val students by viewModel.allStudents.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("ALL") }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedStudent by remember { mutableStateOf<StudentProfileEntity?>(null) }

    val filteredStudents = students.filter { s ->
        val matchesSearch = s.fullName.contains(searchQuery, ignoreCase = true) ||
                s.id.contains(searchQuery, ignoreCase = true) ||
                s.department.contains(searchQuery, ignoreCase = true) ||
                s.phone.contains(searchQuery)
        val matchesStatus = if (selectedStatusFilter == "ALL") true else s.status == selectedStatusFilter
        matchesSearch && matchesStatus
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = TealPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_student_fab")
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add Student")
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

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("student_search_input"),
                placeholder = { Text("Search by name, ID (e.g. STD-2024-001), department...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SlateMuted) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = CardSurface,
                    unfocusedContainerColor = CardSurface,
                    focusedBorderColor = TealPrimary,
                    unfocusedBorderColor = SlateBorder
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val filters = listOf("ALL", "ACTIVE", "INACTIVE")
                items(filters) { f ->
                    FilterChip(
                        selected = selectedStatusFilter == f,
                        onClick = { selectedStatusFilter = f },
                        label = { Text(f) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TealContainer,
                            selectedLabelColor = OnTealContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Showing ${filteredStudents.size} students",
                style = MaterialTheme.typography.bodySmall,
                color = SlateMuted
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Students List
            if (filteredStudents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.PersonOff, contentDescription = null, tint = SlateMuted, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No students found", fontWeight = FontWeight.Bold, color = SlateDark)
                        Text("Try adjusting your search query or add a new student.", color = SlateMuted, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredStudents, key = { it.id }) { student ->
                        StudentItemCard(
                            student = student,
                            onClick = { selectedStudent = student }
                        )
                    }
                }
            }
        }
    }

    // Add Student Dialog
    if (showAddDialog) {
        AddStudentDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { id, name, email, phone, gender, dept, degree, sem, gName, gPhone, emerg ->
                viewModel.addStudent(id, name, email, phone, gender, dept, degree, sem, gName, gPhone, emerg)
                showAddDialog = false
            }
        )
    }

    // Student Detail Sheet
    if (selectedStudent != null) {
        StudentDetailDialog(
            student = selectedStudent!!,
            onDismiss = { selectedStudent = null },
            onDeactivate = {
                viewModel.deactivateStudent(selectedStudent!!.id)
                selectedStudent = null
            }
        )
    }
}

@Composable
fun StudentItemCard(
    student: StudentProfileEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("student_card_${student.id.lowercase()}"),
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
                    text = student.fullName.take(2).uppercase(),
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
                        text = student.fullName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = SlateDark
                    )
                    StatusBadge(status = student.status)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${student.id} • ${student.department} (Sem ${student.semester})",
                    style = MaterialTheme.typography.bodySmall,
                    color = SlateMuted
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (student.currentRoomNumber != null) Icons.Default.Hotel else Icons.Default.HotelClass,
                        contentDescription = null,
                        tint = if (student.currentRoomNumber != null) TealPrimary else SlateMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (student.currentRoomNumber != null) {
                            "${student.currentBuildingName ?: "Hostel"} - Room ${student.currentRoomNumber} (${student.currentBedNumber})"
                        } else "Unallocated Bed",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (student.currentRoomNumber != null) TealDark else AmberDark
                    )
                }
            }
        }
    }
}

@Composable
fun AddStudentDialog(
    onDismiss: () -> Unit,
    onAdd: (id: String, name: String, email: String, phone: String, gender: String, dept: String, degree: String, sem: Int, gName: String, gPhone: String, emerg: String) -> Unit
) {
    var id by remember { mutableStateOf("STD-2024-${(100..999).random()}") }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("+92 3") }
    var gender by remember { mutableStateOf("Male") }
    var dept by remember { mutableStateOf("Computer Science") }
    var degree by remember { mutableStateOf("BS Computer Science") }
    var semesterText by remember { mutableStateOf("1") }
    var gName by remember { mutableStateOf("") }
    var gPhone by remember { mutableStateOf("+92 3") }
    var emerg by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && id.isNotBlank()) {
                        onAdd(id, name, email, phone, gender, dept, degree, semesterText.toIntOrNull() ?: 1, gName, gPhone, emerg)
                    }
                },
                enabled = name.isNotBlank() && id.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                modifier = Modifier.testTag("submit_add_student")
            ) {
                Text("Register Student")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = {
            Text("Register New Resident", fontWeight = FontWeight.Bold, color = NavyPrimary)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = id,
                    onValueChange = { id = it },
                    label = { Text("Unique Student ID *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Mobile Phone") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dept,
                        onValueChange = { dept = it },
                        label = { Text("Department") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = semesterText,
                        onValueChange = { semesterText = it },
                        label = { Text("Semester") },
                        modifier = Modifier.weight(0.6f),
                        singleLine = true
                    )
                }
                OutlinedTextField(
                    value = degree,
                    onValueChange = { degree = it },
                    label = { Text("Degree Program") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = gName,
                    onValueChange = { gName = it },
                    label = { Text("Guardian Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = gPhone,
                    onValueChange = { gPhone = it },
                    label = { Text("Guardian Phone") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = emerg,
                    onValueChange = { emerg = it },
                    label = { Text("Emergency Contact Info") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }
    )
}

@Composable
fun StudentDetailDialog(
    student: StudentProfileEntity,
    onDismiss: () -> Unit,
    onDeactivate: () -> Unit
) {
    var showConfirmDeactivate by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
        dismissButton = {
            if (student.status == "ACTIVE") {
                TextButton(
                    onClick = { showConfirmDeactivate = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = StatusRed)
                ) {
                    Text("Deactivate Student")
                }
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(TealPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(student.fullName.take(2).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(student.fullName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(student.id, style = MaterialTheme.typography.bodySmall, color = SlateMuted)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                DetailItem("Status", student.status)
                DetailItem("Department", student.department)
                DetailItem("Degree", student.degreeProgram)
                DetailItem("Semester", student.semester.toString())
                DetailItem("Room Allocation", if (student.currentRoomNumber != null) "${student.currentBuildingName ?: "Hostel"} - Room ${student.currentRoomNumber} (${student.currentBedNumber})" else "Not Allocated")
                DetailItem("Email", student.email)
                DetailItem("Phone", student.phone)
                DetailItem("Gender", student.gender)
                DetailItem("Guardian", "${student.guardianName} (${student.guardianPhone})")
                DetailItem("Emergency Contact", student.emergencyContact)
                DetailItem("Admission Date", student.admissionDate)

                if (showConfirmDeactivate) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = StatusRedBg),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Confirm Deactivation?",
                                fontWeight = FontWeight.Bold,
                                color = StatusRed,
                                fontSize = 13.sp
                            )
                            Text(
                                "This will mark student as INACTIVE. Fee and allocation history will be safely preserved.",
                                fontSize = 11.sp,
                                color = SlateDark
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(onClick = { showConfirmDeactivate = false }, modifier = Modifier.height(32.dp)) {
                                    Text("Cancel", fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = onDeactivate,
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusRed),
                                    modifier = Modifier.height(32.dp).testTag("confirm_deactivate_student")
                                ) {
                                    Text("Yes, Deactivate", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun DetailItem(label: String, value: String) {
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
            fontWeight = FontWeight.SemiBold,
            color = SlateDark
        )
    }
}
