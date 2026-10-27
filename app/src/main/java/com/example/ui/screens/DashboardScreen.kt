package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.AppNavDestination
import com.example.ui.HostelHubViewModel
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    viewModel: HostelHubViewModel,
    onNavigate: (AppNavDestination) -> Unit
) {
    val currentRole by viewModel.currentRole.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val students by viewModel.allStudents.collectAsState()
    val buildings by viewModel.allBuildings.collectAsState()
    val rooms by viewModel.allRooms.collectAsState()
    val beds by viewModel.allBeds.collectAsState()
    val allocations by viewModel.activeAllocations.collectAsState()
    val invoices by viewModel.allInvoices.collectAsState()
    val payments by viewModel.allPayments.collectAsState()
    val complaints by viewModel.allComplaints.collectAsState()
    val leaveRequests by viewModel.allLeaveRequests.collectAsState()

    // Calculated real figures from DB
    val totalStudents = students.count { it.status == "ACTIVE" }
    val totalBuildings = buildings.size
    val totalRooms = rooms.size
    val totalBeds = beds.size
    val occupiedBeds = beds.count { it.isOccupied }
    val availableBeds = (totalBeds - occupiedBeds).coerceAtLeast(0)
    val occupancyPercent = if (totalBeds > 0) ((occupiedBeds.toDouble() / totalBeds) * 100).toInt() else 0

    val totalFeesCollected = payments.sumOf { it.amount }
    val totalInvoiced = invoices.sumOf { it.totalAmount }
    val outstandingBalance = (totalInvoiced - totalFeesCollected).coerceAtLeast(0.0)

    val openComplaints = complaints.count { it.status == "PENDING" || it.status == "IN_PROGRESS" }
    val pendingLeaves = leaveRequests.count { it.status == "PENDING" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // Hero Welcome Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NavyPrimary),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Welcome, ${currentUser?.fullName ?: "Resident"}!",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "HostelHub Management Console • Fall 2024",
                                style = MaterialTheme.typography.bodySmall,
                                color = SlateBorder
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(TealPrimary)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${currentRole.name} VIEW",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Occupancy Progress Meter
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Overall Bed Occupancy",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "$occupiedBeds / $totalBeds Beds ($occupancyPercent%)",
                            color = AmberAccent,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { occupancyPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = TealLight,
                        trackColor = NavyLight
                    )
                }
            }
        }

        // Quick Actions Row
        item {
            Text(
                text = "Quick Operations",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = SlateDark
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (currentRole == UserRole.ADMIN || currentRole == UserRole.WARDEN) {
                    QuickActionButton(
                        title = "New Student",
                        icon = Icons.Default.PersonAdd,
                        color = TealPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(AppNavDestination.STUDENTS) }
                    )
                    QuickActionButton(
                        title = "Allocate Bed",
                        icon = Icons.Default.Hotel,
                        color = NavySurface,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(AppNavDestination.ALLOCATION) }
                    )
                }
                if (currentRole == UserRole.ADMIN || currentRole == UserRole.ACCOUNTANT) {
                    QuickActionButton(
                        title = "New Invoice",
                        icon = Icons.Default.Receipt,
                        color = AmberDark,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(AppNavDestination.FEES) }
                    )
                }
                QuickActionButton(
                    title = "Complaint",
                    icon = Icons.Default.ReportProblem,
                    color = StatusRed,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(AppNavDestination.COMPLAINTS) }
                )
            }
        }

        // Metric Statistics Grid
        item {
            Text(
                text = "Key Operational Metrics",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = SlateDark
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Active Students",
                    value = totalStudents.toString(),
                    subtitle = "$totalBuildings Buildings Registered",
                    icon = Icons.Default.School,
                    accentColor = NavyPrimary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Available Beds",
                    value = availableBeds.toString(),
                    subtitle = "$occupiedBeds Beds Occupied",
                    icon = Icons.Default.MeetingRoom,
                    accentColor = TealPrimary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Fees Collected",
                    value = "Rs. ${String.format("%,.0f", totalFeesCollected)}",
                    subtitle = "Outstanding: Rs. ${String.format("%,.0f", outstandingBalance)}",
                    icon = Icons.Default.Payments,
                    accentColor = StatusGreen,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Open Tickets",
                    value = openComplaints.toString(),
                    subtitle = "$pendingLeaves Leaves Pending",
                    icon = Icons.Default.Build,
                    accentColor = StatusAmber,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Room Occupancy by Building
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Occupancy by Building",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    buildings.forEach { bld ->
                        val bldRooms = rooms.filter { it.buildingId == bld.id }
                        val bldRoomIds = bldRooms.map { it.id }.toSet()
                        val bldBeds = beds.filter { it.roomId in bldRoomIds }
                        val bldOcc = bldBeds.count { it.isOccupied }
                        val bldTotal = bldBeds.size
                        val bldPct = if (bldTotal > 0) ((bldOcc.toDouble() / bldTotal) * 100).toInt() else 0

                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${bld.name} (${bld.code})",
                                    fontWeight = FontWeight.SemiBold,
                                    color = SlateDark,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "$bldOcc / $bldTotal beds ($bldPct%)",
                                    color = SlateMuted,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { bldPct / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = TealPrimary,
                                trackColor = SlateLight
                            )
                        }
                    }
                }
            }
        }

        // Recent Payments
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Fee Payments",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                        TextButton(onClick = { onNavigate(AppNavDestination.FEES) }) {
                            Text("View All", color = TealPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (payments.isEmpty()) {
                        Text("No payment records yet.", color = SlateMuted, fontSize = 13.sp)
                    } else {
                        payments.take(3).forEach { pay ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(StatusGreenBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(text = pay.studentName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SlateDark)
                                        Text(text = "${pay.paymentDate} • ${pay.paymentReference}", fontSize = 11.sp, color = SlateMuted)
                                    }
                                }
                                Text(
                                    text = "Rs. ${String.format("%,.0f", pay.amount)}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    color = StatusGreen
                                )
                            }
                            if (pay != payments.take(3).last()) {
                                Divider(color = SlateBorder.copy(alpha = 0.6f))
                            }
                        }
                    }
                }
            }
        }

        // Pending Complaints
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pending Maintenance Tickets",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                        TextButton(onClick = { onNavigate(AppNavDestination.COMPLAINTS) }) {
                            Text("Manage", color = TealPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    val pending = complaints.filter { it.status == "PENDING" || it.status == "IN_PROGRESS" }
                    if (pending.isEmpty()) {
                        Text("No pending complaints at this time.", color = SlateMuted, fontSize = 13.sp)
                    } else {
                        pending.take(3).forEach { cmp ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = cmp.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SlateDark, maxLines = 1)
                                    Text(text = "Room ${cmp.roomNumber} (${cmp.buildingName}) • ${cmp.studentName}", fontSize = 11.sp, color = SlateMuted)
                                }
                                StatusBadge(status = cmp.status)
                            }
                            if (cmp != pending.take(3).last()) {
                                Divider(color = SlateBorder.copy(alpha = 0.6f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("quick_action_${title.lowercase().replace(" ", "_")}"),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}
