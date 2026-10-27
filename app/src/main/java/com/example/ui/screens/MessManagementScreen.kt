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
import com.example.data.model.MessExpenseEntity
import com.example.data.model.MessFeedbackEntity
import com.example.data.model.MessMenuEntity
import com.example.data.model.UserRole
import com.example.ui.HostelHubViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessManagementScreen(
    viewModel: HostelHubViewModel
) {
    val currentRole by viewModel.currentRole.collectAsState()
    val menus by viewModel.messMenus.collectAsState()
    val feedbacks by viewModel.messFeedback.collectAsState()
    val expenses by viewModel.messExpenses.collectAsState()

    var selectedSection by remember { mutableStateOf(0) } // 0: Weekly Menu, 1: Student Feedback, 2: Mess Expenses
    var selectedDay by remember { mutableStateOf("MONDAY") }
    var menuToEdit by remember { mutableStateOf<MessMenuEntity?>(null) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }

    val daysOfWeek = listOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY")
    val currentMenu = menus.find { it.dayOfWeek.equals(selectedDay, ignoreCase = true) }

    Scaffold(
        floatingActionButton = {
            when (selectedSection) {
                0 -> {
                    if (currentRole == UserRole.ADMIN || currentRole == UserRole.WARDEN) {
                        ExtendedFloatingActionButton(
                            onClick = { menuToEdit = currentMenu },
                            containerColor = TealPrimary,
                            contentColor = Color.White,
                            icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            text = { Text("Edit $selectedDay Menu") },
                            modifier = Modifier.testTag("edit_menu_fab")
                        )
                    }
                }
                1 -> {
                    ExtendedFloatingActionButton(
                        onClick = { showFeedbackDialog = true },
                        containerColor = AmberAccent,
                        contentColor = NavyDark,
                        icon = { Icon(Icons.Default.Star, contentDescription = null) },
                        text = { Text("Rate Today's Meal") },
                        modifier = Modifier.testTag("rate_meal_fab")
                    )
                }
                2 -> {
                    if (currentRole == UserRole.ADMIN || currentRole == UserRole.ACCOUNTANT) {
                        ExtendedFloatingActionButton(
                            onClick = { showAddExpenseDialog = true },
                            containerColor = NavyPrimary,
                            contentColor = Color.White,
                            icon = { Icon(Icons.Default.AddShoppingCart, contentDescription = null) },
                            text = { Text("Record Mess Expense") },
                            modifier = Modifier.testTag("add_expense_fab")
                        )
                    }
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

            // Navigation Tabs for Mess Sub-modules
            TabRow(
                selectedTabIndex = selectedSection,
                containerColor = CardSurface,
                contentColor = NavyPrimary
            ) {
                Tab(
                    selected = selectedSection == 0,
                    onClick = { selectedSection = 0 },
                    text = { Text("Weekly Menu", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedSection == 1,
                    onClick = { selectedSection = 1 },
                    text = { Text("Meal Feedback", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                if (currentRole != UserRole.STUDENT) {
                    Tab(
                        selected = selectedSection == 2,
                        onClick = { selectedSection = 2 },
                        text = { Text("Expenses", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            when (selectedSection) {
                0 -> {
                    // WEEKLY MENU TAB
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(daysOfWeek) { day ->
                            FilterChip(
                                selected = selectedDay == day,
                                onClick = { selectedDay = day },
                                label = { Text(day.take(3)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NavyPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (currentMenu != null) {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = TealContainer.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Restaurant, contentDescription = null, tint = OnTealContainer)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "${currentMenu.dayOfWeek} DINING SCHEDULE",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = OnTealContainer
                                            )
                                            if (currentMenu.specialNotes.isNotEmpty()) {
                                                Text(
                                                    text = currentMenu.specialNotes,
                                                    fontSize = 11.sp,
                                                    color = SlateDark
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            item {
                                MealCard(
                                    mealName = "Breakfast",
                                    time = "07:30 AM – 09:30 AM",
                                    icon = Icons.Default.WbSunny,
                                    iconColor = AmberDark,
                                    items = currentMenu.breakfast
                                )
                            }

                            item {
                                MealCard(
                                    mealName = "Lunch",
                                    time = "12:30 PM – 02:30 PM",
                                    icon = Icons.Default.LunchDining,
                                    iconColor = TealPrimary,
                                    items = currentMenu.lunch
                                )
                            }

                            item {
                                MealCard(
                                    mealName = "Dinner",
                                    time = "08:00 PM – 10:00 PM",
                                    icon = Icons.Default.DinnerDining,
                                    iconColor = NavyPrimary,
                                    items = currentMenu.dinner
                                )
                            }
                        }
                    }
                }

                1 -> {
                    // FEEDBACK TAB
                    if (feedbacks.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Outlined.RateReview, contentDescription = null, tint = SlateMuted, modifier = Modifier.size(56.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No meal feedback yet", fontWeight = FontWeight.Bold, color = SlateDark)
                                Text("Tap 'Rate Today's Meal' to submit feedback.", color = SlateMuted, fontSize = 12.sp)
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(feedbacks, key = { it.id }) { fb ->
                                FeedbackCard(feedback = fb)
                            }
                        }
                    }
                }

                2 -> {
                    // MESS EXPENSES TAB
                    val totalExpenses = expenses.sumOf { it.amount }
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = NavyPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Total Mess Purchases", color = SlateBorder, fontSize = 12.sp)
                                Text("Rs. ${String.format("%,.0f", totalExpenses)}", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(TealPrimary)
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("${expenses.size} Items", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(expenses, key = { it.id }) { exp ->
                            MessExpenseCard(expense = exp)
                        }
                    }
                }
            }
        }
    }

    // Edit Menu Dialog
    if (menuToEdit != null) {
        EditMenuDialog(
            menu = menuToEdit!!,
            onDismiss = { menuToEdit = null },
            onSave = { updated ->
                viewModel.updateMessMenu(updated)
                menuToEdit = null
            }
        )
    }

    // Submit Feedback Dialog
    if (showFeedbackDialog) {
        SubmitFeedbackDialog(
            onDismiss = { showFeedbackDialog = false },
            onSubmit = { mealType, rating, comments ->
                viewModel.submitMessFeedback(mealType, rating, comments)
                showFeedbackDialog = false
            }
        )
    }

    // Add Mess Expense Dialog
    if (showAddExpenseDialog) {
        AddMessExpenseDialog(
            onDismiss = { showAddExpenseDialog = false },
            onAdd = { desc, cat, qty, amount ->
                viewModel.recordMessExpense(desc, cat, qty, amount)
                showAddExpenseDialog = false
            }
        )
    }
}

@Composable
fun MealCard(
    mealName: String,
    time: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    items: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                            .clip(CircleShape)
                            .background(iconColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = mealName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SlateDark
                    )
                }
                Text(
                    text = time,
                    style = MaterialTheme.typography.labelSmall,
                    color = SlateMuted
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                color = SlateLight,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = items,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = SlateDark,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

@Composable
fun FeedbackCard(feedback: MessFeedbackEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                Text(
                    text = "${feedback.studentName} (${feedback.mealType})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = SlateDark
                )
                Row {
                    repeat(5) { i ->
                        Icon(
                            imageVector = if (i < feedback.rating) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = null,
                            tint = AmberAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "\"${feedback.comments}\"", style = MaterialTheme.typography.bodySmall, color = SlateDark)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = feedback.feedbackDate, style = MaterialTheme.typography.labelSmall, color = SlateMuted)
        }
    }
}

@Composable
fun MessExpenseCard(expense: MessExpenseEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = expense.itemDescription, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SlateDark)
                Text(text = "${expense.category} • ${expense.quantity} • By ${expense.recordedBy}", fontSize = 11.sp, color = SlateMuted)
                Text(text = expense.expenseDate, fontSize = 10.sp, color = SlateMuted)
            }
            Text(
                text = "Rs. ${String.format("%,.0f", expense.amount)}",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                color = NavyPrimary
            )
        }
    }
}

@Composable
fun EditMenuDialog(
    menu: MessMenuEntity,
    onDismiss: () -> Unit,
    onSave: (MessMenuEntity) -> Unit
) {
    var breakfast by remember { mutableStateOf(menu.breakfast) }
    var lunch by remember { mutableStateOf(menu.lunch) }
    var dinner by remember { mutableStateOf(menu.dinner) }
    var notes by remember { mutableStateOf(menu.specialNotes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    onSave(menu.copy(breakfast = breakfast, lunch = lunch, dinner = dinner, specialNotes = notes))
                },
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
                Text("Save Menu")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = {
            Text("Edit ${menu.dayOfWeek} Menu", fontWeight = FontWeight.Bold, color = NavyPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = breakfast,
                    onValueChange = { breakfast = it },
                    label = { Text("Breakfast Menu") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = lunch,
                    onValueChange = { lunch = it },
                    label = { Text("Lunch Menu") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dinner,
                    onValueChange = { dinner = it },
                    label = { Text("Dinner Menu") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Special Diet / Feast Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}

@Composable
fun SubmitFeedbackDialog(
    onDismiss: () -> Unit,
    onSubmit: (mealType: String, rating: Int, comments: String) -> Unit
) {
    var mealType by remember { mutableStateOf("DINNER") }
    var rating by remember { mutableStateOf(5) }
    var comments by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (comments.isNotBlank()) onSubmit(mealType, rating, comments)
                },
                enabled = comments.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AmberDark)
            ) {
                Text("Submit Review")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = {
            Text("Meal Rating & Feedback", fontWeight = FontWeight.Bold, color = NavyPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Select Meal:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("BREAKFAST", "LUNCH", "DINNER").forEach { m ->
                        FilterChip(
                            selected = mealType == m,
                            onClick = { mealType = m },
                            label = { Text(m, fontSize = 11.sp) }
                        )
                    }
                }

                Text("Rating (1-5 Stars):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    (1..5).forEach { star ->
                        IconButton(onClick = { rating = star }) {
                            Icon(
                                imageVector = if (star <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                                contentDescription = null,
                                tint = AmberAccent,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = comments,
                    onValueChange = { comments = it },
                    label = { Text("Your Comments & Taste Feedback *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        }
    )
}

@Composable
fun AddMessExpenseDialog(
    onDismiss: () -> Unit,
    onAdd: (description: String, category: String, quantity: String, amount: Double) -> Unit
) {
    var desc by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("GROCERIES") }
    var quantity by remember { mutableStateOf("50 Kg") }
    var amountText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (desc.isNotBlank() && amt > 0) {
                        onAdd(desc, category, quantity, amt)
                    }
                },
                enabled = desc.isNotBlank() && (amountText.toDoubleOrNull() ?: 0.0) > 0,
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
                Text("Record Expense")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = {
            Text("Record Mess Purchase", fontWeight = FontWeight.Bold, color = NavyPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Item Description (e.g. Fresh Milk Supplies)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Category:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(listOf("GROCERIES", "DAIRY", "MEAT", "VEGETABLES", "GAS_UTILITIES", "CLEANING")) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.replace("_", " "), fontSize = 10.sp) }
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Quantity") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Amount (PKR) *") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    )
}
