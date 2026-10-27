package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.data.model.*
import com.example.ui.HostelHubViewModel
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildingAndRoomScreen(
    viewModel: HostelHubViewModel
) {
    val buildings by viewModel.allBuildings.collectAsState()
    val rooms by viewModel.allRooms.collectAsState()
    val beds by viewModel.allBeds.collectAsState()

    var selectedBuildingId by remember { mutableStateOf<String?>(null) }
    var selectedFloor by remember { mutableStateOf<Int?>(null) }
    var showAddBuildingDialog by remember { mutableStateOf(false) }
    var showAddRoomDialog by remember { mutableStateOf(false) }

    // Initialize selected building to first available
    LaunchedEffect(buildings) {
        if (selectedBuildingId == null && buildings.isNotEmpty()) {
            selectedBuildingId = buildings.first().id
        }
    }

    val activeBuilding = buildings.find { it.id == selectedBuildingId }
    val buildingRooms = rooms.filter { it.buildingId == selectedBuildingId }
    val filteredRooms = if (selectedFloor != null) {
        buildingRooms.filter { it.floor == selectedFloor }
    } else {
        buildingRooms
    }

    Scaffold(
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SmallFloatingActionButton(
                    onClick = { showAddBuildingDialog = true },
                    containerColor = NavySurface,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_building_fab")
                ) {
                    Icon(Icons.Default.DomainAdd, contentDescription = "Add Building")
                }
                FloatingActionButton(
                    onClick = { showAddRoomDialog = true },
                    containerColor = TealPrimary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_room_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Room")
                }
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

            // Building Selector Tabs
            Text(
                text = "Select Hostel Building",
                style = MaterialTheme.typography.labelMedium,
                color = SlateMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(buildings) { bld ->
                    val isSelected = bld.id == selectedBuildingId
                    Surface(
                        onClick = {
                            selectedBuildingId = bld.id
                            selectedFloor = null
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) NavyPrimary else CardSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) NavyPrimary else SlateBorder),
                        modifier = Modifier.testTag("building_tab_${bld.id.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Domain,
                                contentDescription = null,
                                tint = if (isSelected) TealLight else SlateMuted,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = bld.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isSelected) Color.White else SlateDark
                                )
                                Text(
                                    text = "${bld.floorsCount} Floors • ${bld.code}",
                                    fontSize = 10.sp,
                                    color = if (isSelected) SlateBorder else SlateMuted
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Floor Filter Chips
            if (activeBuilding != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Floors in ${activeBuilding.name}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = SlateDark
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedFloor == null,
                            onClick = { selectedFloor = null },
                            label = { Text("All Floors") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = TealContainer)
                        )
                    }
                    items(activeBuilding.floorsCount) { floorIndex ->
                        val floorNum = floorIndex + 1
                        FilterChip(
                            selected = selectedFloor == floorNum,
                            onClick = { selectedFloor = floorNum },
                            label = { Text("Floor $floorNum") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = TealContainer)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Room Cards Grid / List
            if (filteredRooms.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.MeetingRoom, contentDescription = null, tint = SlateMuted, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No rooms configured in this section", fontWeight = FontWeight.Bold, color = SlateDark)
                        Text("Tap the '+' button below to add a room.", color = SlateMuted, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredRooms, key = { it.id }) { room ->
                        val roomBeds = beds.filter { it.roomId == room.id }
                        val occupiedCount = roomBeds.count { it.isOccupied }
                        val status = when {
                            room.maintenanceStatus == "UNDER_MAINTENANCE" -> "UNDER_MAINTENANCE"
                            occupiedCount == room.capacity -> "FULL"
                            occupiedCount > 0 -> "PARTIALLY_OCCUPIED"
                            else -> "AVAILABLE"
                        }
                        RoomItemCard(room = room, roomBeds = roomBeds, roomStatus = status)
                    }
                }
            }
        }
    }

    // Add Building Dialog
    if (showAddBuildingDialog) {
        AddBuildingDialog(
            onDismiss = { showAddBuildingDialog = false },
            onAdd = { name, code, address, floors ->
                viewModel.addBuilding(name, code, address, floors)
                showAddBuildingDialog = false
            }
        )
    }

    // Add Room Dialog
    if (showAddRoomDialog && activeBuilding != null) {
        AddRoomDialog(
            building = activeBuilding,
            onDismiss = { showAddRoomDialog = false },
            onAdd = { bldId, roomNum, floor, type, cap, rent, desc ->
                viewModel.addRoom(bldId, roomNum, floor, type, cap, rent, desc)
                showAddRoomDialog = false
            }
        )
    }
}

@Composable
fun RoomItemCard(
    room: RoomEntity,
    roomBeds: List<BedEntity>,
    roomStatus: String
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("room_card_${room.id.lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(NavyPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = room.roomNumber,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Room ${room.roomNumber} (Floor ${room.floor})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = SlateDark
                        )
                        Text(
                            text = "${room.roomType} • Capacity: ${room.capacity} Beds",
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateMuted
                        )
                    }
                }
                StatusBadge(status = roomStatus)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = room.description,
                style = MaterialTheme.typography.bodySmall,
                color = SlateDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Rent: Rs. ${String.format("%,.0f", room.monthlyRent)} / month",
                    fontWeight = FontWeight.Bold,
                    color = TealPrimary,
                    fontSize = 13.sp
                )
                Text(
                    text = "${roomBeds.count { it.isOccupied }} / ${room.capacity} Occupied",
                    style = MaterialTheme.typography.labelSmall,
                    color = SlateMuted
                )
            }

            Divider(modifier = Modifier.padding(vertical = 10.dp), color = SlateBorder)

            // Individual Beds in this room
            Text(
                text = "Beds in this Room:",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = SlateMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                roomBeds.forEach { bed ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (bed.isOccupied) StatusRedBg else StatusGreenBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (bed.isOccupied) StatusRed.copy(alpha = 0.3f) else StatusGreen.copy(alpha = 0.3f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Bed,
                                contentDescription = null,
                                tint = if (bed.isOccupied) StatusRed else StatusGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = bed.bedNumber,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (bed.isOccupied) StatusRed else StatusGreen
                            )
                            Text(
                                text = if (bed.isOccupied) (bed.currentStudentName?.split(" ")?.firstOrNull() ?: "Occupied") else "Available",
                                fontSize = 10.sp,
                                color = SlateDark,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddBuildingDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, code: String, address: String, floors: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var floorsText by remember { mutableStateOf("3") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && code.isNotBlank()) {
                        onAdd(name, code, address, floorsText.toIntOrNull() ?: 3)
                    }
                },
                enabled = name.isNotBlank() && code.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                modifier = Modifier.testTag("submit_add_building")
            ) {
                Text("Add Building")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = {
            Text("Add Hostel Building", fontWeight = FontWeight.Bold, color = NavyPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Building Name (e.g. Liaquat Hall)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Building Code (e.g. LH-D)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Location / Address") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = floorsText,
                    onValueChange = { floorsText = it },
                    label = { Text("Number of Floors") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }
    )
}

@Composable
fun AddRoomDialog(
    building: BuildingEntity,
    onDismiss: () -> Unit,
    onAdd: (bldId: String, roomNum: String, floor: Int, type: String, cap: Int, rent: Double, desc: String) -> Unit
) {
    var roomNum by remember { mutableStateOf("") }
    var floorText by remember { mutableStateOf("1") }
    var type by remember { mutableStateOf("DOUBLE") }
    var capacityText by remember { mutableStateOf("2") }
    var rentText by remember { mutableStateOf("18000") }
    var desc by remember { mutableStateOf("Standard room with attached bath and study tables") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (roomNum.isNotBlank()) {
                        onAdd(
                            building.id,
                            roomNum,
                            floorText.toIntOrNull() ?: 1,
                            type,
                            capacityText.toIntOrNull() ?: 2,
                            rentText.toDoubleOrNull() ?: 18000.0,
                            desc
                        )
                    }
                },
                enabled = roomNum.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                modifier = Modifier.testTag("submit_add_room")
            ) {
                Text("Add Room")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = {
            Text("Add Room to ${building.name}", fontWeight = FontWeight.Bold, color = NavyPrimary)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = roomNum,
                    onValueChange = { roomNum = it },
                    label = { Text("Room Number (e.g. 104)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = floorText,
                        onValueChange = { floorText = it },
                        label = { Text("Floor No.") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = capacityText,
                        onValueChange = {
                            capacityText = it
                            type = when (it.toIntOrNull() ?: 2) {
                                1 -> "SINGLE"
                                2 -> "DOUBLE"
                                3 -> "TRIPLE"
                                else -> "QUAD"
                            }
                        },
                        label = { Text("Capacity (Beds)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                OutlinedTextField(
                    value = rentText,
                    onValueChange = { rentText = it },
                    label = { Text("Monthly Rent (PKR)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Room Description") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }
    )
}
