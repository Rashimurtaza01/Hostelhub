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
import com.example.data.model.InvoiceEntity
import com.example.data.model.PaymentEntity
import com.example.data.model.StudentProfileEntity
import com.example.data.model.UserRole
import com.example.ui.HostelHubViewModel
import com.example.ui.components.PrintableReceiptDialog
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeeManagementScreen(
    viewModel: HostelHubViewModel
) {
    val currentRole by viewModel.currentRole.collectAsState()
    val allInvoices by viewModel.allInvoices.collectAsState()
    val studentInvoices by viewModel.studentInvoices.collectAsState()
    val allPayments by viewModel.allPayments.collectAsState()
    val students by viewModel.allStudents.collectAsState()

    // Determine invoices to show depending on role
    val invoices = if (currentRole == UserRole.STUDENT) studentInvoices else allInvoices

    var selectedFilter by remember { mutableStateOf("ALL") }
    var showGenerateInvoiceDialog by remember { mutableStateOf(false) }
    var invoiceToPay by remember { mutableStateOf<InvoiceEntity?>(null) }
    var receiptToView by remember { mutableStateOf<PaymentEntity?>(null) }

    val filteredInvoices = invoices.filter { inv ->
        when (selectedFilter) {
            "UNPAID" -> inv.status == "UNPAID" || inv.status == "PARTIALLY_PAID" || inv.status == "OVERDUE"
            "PAID" -> inv.status == "PAID"
            else -> true
        }
    }

    val totalInvoiced = invoices.sumOf { it.totalAmount }
    val totalCollected = invoices.sumOf { it.amountPaid }
    val outstanding = (totalInvoiced - totalCollected).coerceAtLeast(0.0)

    Scaffold(
        floatingActionButton = {
            if (currentRole == UserRole.ADMIN || currentRole == UserRole.ACCOUNTANT) {
                ExtendedFloatingActionButton(
                    onClick = { showGenerateInvoiceDialog = true },
                    containerColor = TealPrimary,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.PostAdd, contentDescription = null) },
                    text = { Text("Generate Invoice") },
                    modifier = Modifier.testTag("generate_invoice_fab")
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

            // Financial Summary Banner
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (currentRole == UserRole.STUDENT) "My Fee Summary" else "Hostel Financial Overview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Billed", style = MaterialTheme.typography.labelSmall, color = SlateBorder)
                            Text("Rs. ${String.format("%,.0f", totalInvoiced)}", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                        }
                        Column {
                            Text("Paid", style = MaterialTheme.typography.labelSmall, color = SlateBorder)
                            Text("Rs. ${String.format("%,.0f", totalCollected)}", fontWeight = FontWeight.Bold, color = StatusGreen, fontSize = 14.sp)
                        }
                        Column {
                            Text("Outstanding", style = MaterialTheme.typography.labelSmall, color = SlateBorder)
                            Text("Rs. ${String.format("%,.0f", outstanding)}", fontWeight = FontWeight.Bold, color = AmberAccent, fontSize = 14.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filters
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val filters = listOf("ALL", "UNPAID", "PAID")
                items(filters) { f ->
                    FilterChip(
                        selected = selectedFilter == f,
                        onClick = { selectedFilter = f },
                        label = { Text(if (f == "UNPAID") "Pending & Overdue" else f) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = TealContainer)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (filteredInvoices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.ReceiptLong, contentDescription = null, tint = SlateMuted, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No fee invoices found", fontWeight = FontWeight.Bold, color = SlateDark)
                        Text("Invoices and payment receipts will appear here.", color = SlateMuted, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredInvoices, key = { it.id }) { invoice ->
                        val matchingPayments = allPayments.filter { it.invoiceId == invoice.id }
                        InvoiceCard(
                            invoice = invoice,
                            payments = matchingPayments,
                            canRecordPayment = currentRole == UserRole.ADMIN || currentRole == UserRole.ACCOUNTANT,
                            onPayClick = { invoiceToPay = invoice },
                            onViewReceipt = { p -> receiptToView = p }
                        )
                    }
                }
            }
        }
    }

    // Generate Invoice Dialog
    if (showGenerateInvoiceDialog) {
        GenerateInvoiceDialog(
            students = students.filter { it.status == "ACTIVE" },
            onDismiss = { showGenerateInvoiceDialog = false },
            onGenerate = { studentId, month, rent, mess, util, other, discount, due ->
                viewModel.generateInvoice(studentId, month, rent, mess, util, other, discount, due)
                showGenerateInvoiceDialog = false
            }
        )
    }

    // Record Payment Dialog
    if (invoiceToPay != null) {
        RecordPaymentDialog(
            invoice = invoiceToPay!!,
            onDismiss = { invoiceToPay = null },
            onRecord = { invId, amount, method, notes ->
                viewModel.recordPayment(invId, amount, method, notes)
                invoiceToPay = null
            }
        )
    }

    // View Official Receipt Dialog
    if (receiptToView != null) {
        PrintableReceiptDialog(
            payment = receiptToView!!,
            onDismiss = { receiptToView = null }
        )
    }
}

@Composable
fun InvoiceCard(
    invoice: InvoiceEntity,
    payments: List<PaymentEntity>,
    canRecordPayment: Boolean,
    onPayClick: () -> Unit,
    onViewReceipt: (PaymentEntity) -> Unit
) {
    val remainingBalance = (invoice.totalAmount - invoice.amountPaid).coerceAtLeast(0.0)

    Card(
        modifier = Modifier.fillMaxWidth().testTag("invoice_card_${invoice.id.lowercase()}"),
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
                        Icon(Icons.Default.Receipt, contentDescription = null, tint = OnTealContainer, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "${invoice.studentName} (${invoice.studentId})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = SlateDark
                        )
                        Text(
                            text = "${invoice.invoiceNumber} • ${invoice.billingMonth}",
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateMuted
                        )
                    }
                }
                StatusBadge(status = invoice.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Breakdown Table
            Surface(
                color = SlateLight,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Room Rent: Rs. ${String.format("%,.0f", invoice.roomRent)}", fontSize = 11.sp, color = SlateDark)
                        Text("Mess: Rs. ${String.format("%,.0f", invoice.messCharges)}", fontSize = 11.sp, color = SlateDark)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Utilities: Rs. ${String.format("%,.0f", invoice.utilityCharges)}", fontSize = 11.sp, color = SlateDark)
                        if (invoice.discount > 0) {
                            Text("Discount: -Rs. ${String.format("%,.0f", invoice.discount)}", fontSize = 11.sp, color = StatusGreen)
                        }
                    }
                    Divider(modifier = Modifier.padding(vertical = 6.dp), color = SlateBorder)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total: Rs. ${String.format("%,.0f", invoice.totalAmount)}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyPrimary)
                        Text("Paid: Rs. ${String.format("%,.0f", invoice.amountPaid)}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = StatusGreen)
                    }
                    if (remainingBalance > 0) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Due Date: ${invoice.dueDate}", fontSize = 11.sp, color = StatusRed)
                            Text("Balance Due: Rs. ${String.format("%,.0f", remainingBalance)}", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = StatusRed)
                        }
                    }
                }
            }

            // Payments and Receipts
            if (payments.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Payment Receipts:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SlateMuted)
                payments.forEach { pay ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${pay.paymentDate} • ${pay.paymentReference} (Rs. ${String.format("%,.0f", pay.amount)})",
                            fontSize = 11.sp,
                            color = SlateDark
                        )
                        TextButton(
                            onClick = { onViewReceipt(pay) },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                            modifier = Modifier.height(28.dp).testTag("view_receipt_${pay.paymentReference.lowercase()}")
                        ) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("View Receipt", fontSize = 11.sp, color = TealPrimary)
                        }
                    }
                }
            }

            if (canRecordPayment && remainingBalance > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onPayClick,
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        modifier = Modifier.height(34.dp).testTag("record_payment_button_${invoice.id.lowercase()}")
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Record Payment", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun GenerateInvoiceDialog(
    students: List<StudentProfileEntity>,
    onDismiss: () -> Unit,
    onGenerate: (studentId: String, month: String, rent: Double, mess: Double, util: Double, other: Double, discount: Double, due: String) -> Unit
) {
    var selectedStudentId by remember { mutableStateOf(students.firstOrNull()?.id ?: "") }
    var month by remember { mutableStateOf("November 2024") }
    var rentText by remember { mutableStateOf("18000") }
    var messText by remember { mutableStateOf("9000") }
    var utilText by remember { mutableStateOf("3500") }
    var otherText by remember { mutableStateOf("0") }
    var discountText by remember { mutableStateOf("0") }
    var dueDate by remember { mutableStateOf("2024-11-10") }

    val total = (rentText.toDoubleOrNull() ?: 0.0) +
            (messText.toDoubleOrNull() ?: 0.0) +
            (utilText.toDoubleOrNull() ?: 0.0) +
            (otherText.toDoubleOrNull() ?: 0.0) -
            (discountText.toDoubleOrNull() ?: 0.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (selectedStudentId.isNotBlank()) {
                        onGenerate(
                            selectedStudentId,
                            month,
                            rentText.toDoubleOrNull() ?: 18000.0,
                            messText.toDoubleOrNull() ?: 9000.0,
                            utilText.toDoubleOrNull() ?: 3500.0,
                            otherText.toDoubleOrNull() ?: 0.0,
                            discountText.toDoubleOrNull() ?: 0.0,
                            dueDate
                        )
                    }
                },
                enabled = selectedStudentId.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                modifier = Modifier.testTag("submit_generate_invoice")
            ) {
                Text("Generate Invoice")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = {
            Text("Generate Monthly Fee Invoice", fontWeight = FontWeight.Bold, color = NavyPrimary)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Select Student:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SlateDark)
                students.take(5).forEach { s ->
                    Surface(
                        onClick = { selectedStudentId = s.id },
                        shape = RoundedCornerShape(8.dp),
                        color = if (selectedStudentId == s.id) TealContainer else SlateLight,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${s.fullName} (${s.id})", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            RadioButton(selected = selectedStudentId == s.id, onClick = { selectedStudentId = s.id })
                        }
                    }
                }

                OutlinedTextField(
                    value = month,
                    onValueChange = { month = it },
                    label = { Text("Billing Month") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = rentText,
                        onValueChange = { rentText = it },
                        label = { Text("Room Rent (PKR)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = messText,
                        onValueChange = { messText = it },
                        label = { Text("Mess Charges") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = utilText,
                        onValueChange = { utilText = it },
                        label = { Text("Utilities") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = discountText,
                        onValueChange = { discountText = it },
                        label = { Text("Discount") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Due Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Surface(
                    color = SlateLight,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Calculated Total:", fontWeight = FontWeight.Bold)
                        Text("Rs. ${String.format("%,.0f", total.coerceAtLeast(0.0))}", fontWeight = FontWeight.ExtraBold, color = TealPrimary)
                    }
                }
            }
        }
    )
}

@Composable
fun RecordPaymentDialog(
    invoice: InvoiceEntity,
    onDismiss: () -> Unit,
    onRecord: (invoiceId: String, amount: Double, method: String, notes: String) -> Unit
) {
    val remaining = (invoice.totalAmount - invoice.amountPaid).coerceAtLeast(0.0)
    var amountText by remember { mutableStateOf(remaining.toInt().toString()) }
    var method by remember { mutableStateOf("BANK_TRANSFER") }
    var notes by remember { mutableStateOf("Bank Alfalah Transfer") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount > 0) {
                        onRecord(invoice.id, amount, method, notes)
                    }
                },
                enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0,
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                modifier = Modifier.testTag("submit_record_payment")
            ) {
                Text("Confirm Payment")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = {
            Text("Record Fee Payment", fontWeight = FontWeight.Bold, color = NavyPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Student: ${invoice.studentName} (${invoice.studentId})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = SlateDark
                )
                Text(
                    "Invoice: ${invoice.invoiceNumber} • Remaining Balance: Rs. ${String.format("%,.0f", remaining)}",
                    fontSize = 12.sp,
                    color = SlateMuted
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Payment Amount (PKR) *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Text("Payment Method:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SlateDark)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("BANK_TRANSFER", "CASH", "ONLINE").forEach { m ->
                        FilterChip(
                            selected = method == m,
                            onClick = { method = m },
                            label = { Text(m.replace("_", " "), fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = TealContainer)
                        )
                    }
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Transaction Reference / Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }
    )
}
