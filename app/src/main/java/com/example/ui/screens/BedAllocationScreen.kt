package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.model.*
import com.example.ui.HostelHubViewModel
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BedAllocationScreen(
    viewModel: HostelHubViewModel
) {
    val allocations by viewModel.allAllocations.collectAsState()
    val students by viewModel.allStudents.collectAsState()
    val availableBeds by viewModel.availableBeds.collectAsState()
    val rooms by viewModel.allRooms.collectAsState()
    val buildings by viewModel.allBuildings.collectAsState()

    var showAllocateDialog by remember { mutableStateOf(false) }
    var allocationToCheckout by remember { mutableStateOf<AllocationEntity?>(null) }
    var selectedTab by remember { mutableStateOf(0) } // 0: Active, 1: History

    val activeList = allocations.filter { it.status == "ACTIVE" }
    val historyList = allocations.filter { it.status != "ACTIVE" }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAllocateDialog = true },
                containerColor = TealPrimary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Hotel, contentDescription = null) },
                text = { Text("Allocate Bed") },
                modifier = Modifier.testTag("allocate_bed_fab")
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

            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = CardSurface,
                contentColor = NavyPrimary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Active Allocations (${activeList.size})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Checkout History (${historyList.size})", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val currentDisplayList = if (selectedTab == 0) activeList else historyList

            if (currentDisplayList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.Hotel, contentDescription = null, tint = SlateMuted, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (selectedTab == 0) "No active bed allocations" else "No checkout records",
                            fontWeight = FontWeight.Bold,
                            color = SlateDark
                        )
                        Text(
                            text = "Tap 'Allocate Bed' to assign an available bed to a student.",
                            color = SlateMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(currentDisplayList, key = { it.id }) { allocation ->
                        AllocationCard(
                            allocation = allocation,
                            onCheckoutClick = { allocationToCheckout = allocation }
                        )
                    }
                }
            }
        }
    }

    // New Allocation Modal
    if (showAllocateDialog) {
        AllocateBedDialog(
            students = students.filter { it.status == "ACTIVE" && it.currentRoomNumber == null },
            availableBeds = availableBeds,
            rooms = rooms,
            buildings = buildings,
            onDismiss = { showAllocateDialog = false },
            onConfirm = { studentId, bedId, checkoutDate ->
                viewModel.allocateBed(studentId, bedId, checkoutDate)
                showAllocateDialog = false
            }
        )
    }

    // Confirm Checkout Dialog
    if (allocationToCheckout != null) {
        AlertDialog(
            onDismissRequest = { allocationToCheckout = null },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.checkoutStudent(allocationToCheckout!!.studentId)
                        allocationToCheckout = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRed),
                    modifier = Modifier.testTag("confirm_checkout_button")
                ) {
                    Text("Confirm Checkout")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { allocationToCheckout = null }) { Text("Cancel") }
            },
            title = {
                Text("Confirm Student Checkout", fontWeight = FontWeight.Bold, color = NavyPrimary)
            },
            text = {
                Column {
                    Text(
                        "Are you sure you want to checkout ${allocationToCheckout!!.studentName} from Room ${allocationToCheckout!!.roomNumber} (${allocationToCheckout!!.bedNumber})?",
                        color = SlateDark,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "• Bed will be immediately marked AVAILABLE.\n• Historical fees, invoices, and payment receipts are preserved.\n• Checkout date will be stamped as of today.",
                        color = SlateMuted,
                        fontSize = 12.sp
                    )
                }
            }
        )
    }
}

@Composable
fun AllocationCard(
    allocation: AllocationEntity,
    onCheckoutClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("allocation_card_${allocation.id.lowercase()}"),
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
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(NavyPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Hotel, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = allocation.studentName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = SlateDark
                        )
                        Text(
                            text = "${allocation.studentId} • ${allocation.buildingName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateMuted
                        )
                    }
                }
                StatusBadge(status = allocation.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                color = SlateLight,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Room / Bed", style = MaterialTheme.typography.labelSmall, color = SlateMuted)
                        Text("Room ${allocation.roomNumber} - ${allocation.bedNumber}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyPrimary)
                    }
                    Column {
                        Text("Allocated On", style = MaterialTheme.typography.labelSmall, color = SlateMuted)
                        Text(allocation.allocationDate, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SlateDark)
                    }
                    Column {
                        Text("Monthly Rent", style = MaterialTheme.typography.labelSmall, color = SlateMuted)
                        Text("Rs. ${String.format("%,.0f", allocation.monthlyRent)}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TealPrimary)
                    }
                }
            }

            if (allocation.status == "ACTIVE") {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onCheckoutClick,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusRed),
                        modifier = Modifier.height(34.dp).testTag("checkout_button_${allocation.studentId.lowercase()}")
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Checkout Resident", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            } else if (allocation.actualCheckoutDate != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Checked out on: ${allocation.actualCheckoutDate}",
                    style = MaterialTheme.typography.labelSmall,
                    color = SlateMuted
                )
            }
        }
    }
}

@Composable
fun AllocateBedDialog(
    students: List<StudentProfileEntity>,
    availableBeds: List<BedEntity>,
    rooms: List<RoomEntity>,
    buildings: List<com.example.data.model.BuildingEntity>,
    onDismiss: () -> Unit,
    onConfirm: (studentId: String, bedId: String, checkoutDate: String) -> Unit
) {
    var selectedStudentId by remember { mutableStateOf(students.firstOrNull()?.id ?: "") }
    var selectedBedId by remember { mutableStateOf(availableBeds.firstOrNull()?.id ?: "") }
    var checkoutDate by remember { mutableStateOf("2024-12-31") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (selectedStudentId.isNotBlank() && selectedBedId.isNotBlank()) {
                        onConfirm(selectedStudentId, selectedBedId, checkoutDate)
                    }
                },
                enabled = selectedStudentId.isNotBlank() && selectedBedId.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                modifier = Modifier.testTag("submit_allocation")
            ) {
                Text("Confirm Allocation")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = {
            Text("Allocate Available Bed", fontWeight = FontWeight.Bold, color = NavyPrimary)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (students.isEmpty()) {
                    Text(
                        "All registered active students currently have a bed allocated! Register a new student first.",
                        color = StatusAmber,
                        fontSize = 12.sp
                    )
                } else {
                    Text("1. Select Student (Unassigned Beds):", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SlateDark)
                    students.forEach { s ->
                        Surface(
                            onClick = { selectedStudentId = s.id },
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedStudentId == s.id) TealContainer else SlateLight,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(s.fullName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("${s.id} • ${s.department}", fontSize = 11.sp, color = SlateMuted)
                                }
                                RadioButton(selected = selectedStudentId == s.id, onClick = { selectedStudentId = s.id })
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                if (availableBeds.isEmpty()) {
                    Text("No available beds found in any room! Add more rooms or beds.", color = StatusRed, fontSize = 12.sp)
                } else {
                    Text("2. Select Available Bed:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SlateDark)
                    availableBeds.forEach { b ->
                        val r = rooms.find { it.id == b.roomId }
                        val bldName = if (r?.buildingId == "BLD-A") "Jinnah Hall" else if (r?.buildingId == "BLD-B") "Iqbal Hall" else "Fatima Hall"
                        Surface(
                            onClick = { selectedBedId = b.id },
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedBedId == b.id) TealContainer else SlateLight,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("$bldName • Room ${r?.roomNumber ?: ""} - ${b.bedNumber}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Rent: Rs. ${String.format("%,.0f", r?.monthlyRent ?: 0.0)}/mo", fontSize = 11.sp, color = TealDark)
                                }
                                RadioButton(selected = selectedBedId == b.id, onClick = { selectedBedId = b.id })
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = checkoutDate,
                    onValueChange = { checkoutDate = it },
                    label = { Text("Expected Checkout Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }
    )
}
