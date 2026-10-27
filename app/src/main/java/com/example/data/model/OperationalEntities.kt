package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey val id: String, // e.g. "INV-2024-10-001"
    val invoiceNumber: String,
    val studentId: String,
    val studentName: String,
    val billingMonth: String, // "October 2024"
    val roomRent: Double,
    val messCharges: Double,
    val utilityCharges: Double,
    val otherCharges: Double = 0.0,
    val discount: Double = 0.0,
    val totalAmount: Double,
    val amountPaid: Double = 0.0,
    val dueDate: String, // "2024-10-10"
    val status: String = "UNPAID", // "UNPAID", "PARTIALLY_PAID", "PAID", "OVERDUE"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey val id: String, // e.g. "PAY-2024-10-001"
    val paymentReference: String, // "RCPT-98124"
    val invoiceId: String,
    val studentId: String,
    val studentName: String,
    val amount: Double,
    val paymentDate: String, // "2024-10-05"
    val paymentMethod: String, // "CASH", "BANK_TRANSFER", "ONLINE_PORTAL"
    val recordedBy: String,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "complaints")
data class ComplaintEntity(
    @PrimaryKey val id: String, // e.g. "CMP-1001"
    val ticketNumber: String,
    val studentId: String,
    val studentName: String,
    val buildingName: String,
    val roomNumber: String,
    val bedNumber: String,
    val category: String, // "ELECTRICITY", "WATER_SUPPLY", "PLUMBING", "BATHROOM", "FURNITURE", "INTERNET", "CLEANING", "OTHER"
    val title: String,
    val description: String,
    val priority: String = "MEDIUM", // "LOW", "MEDIUM", "HIGH", "URGENT"
    val status: String = "PENDING", // "PENDING", "IN_PROGRESS", "RESOLVED", "CLOSED"
    val assignedStaff: String? = null,
    val resolutionNotes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val resolvedAt: Long? = null
)

@Entity(tableName = "leave_requests")
data class LeaveRequestEntity(
    @PrimaryKey val id: String, // e.g. "LEV-1001"
    val studentId: String,
    val studentName: String,
    val roomNumber: String,
    val startDate: String, // "2024-10-25 10:00"
    val endDate: String,   // "2024-10-28 18:00"
    val destination: String,
    val reason: String,
    val emergencyContact: String,
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED", "CANCELLED", "COMPLETED"
    val reviewNotes: String = "",
    val reviewedBy: String? = null,
    val reviewedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "visitors")
data class VisitorEntity(
    @PrimaryKey val id: String, // e.g. "VIS-1001"
    val visitorName: String,
    val contactNumber: String,
    val cnicOrId: String,
    val studentId: String,
    val studentName: String,
    val relationship: String,
    val purpose: String,
    val entryTime: String, // "2024-10-26 14:30"
    val exitTime: String? = null, // "2024-10-26 16:45"
    val status: String = "CHECKED_IN", // "CHECKED_IN", "CHECKED_OUT"
    val recordedBy: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "mess_menus")
data class MessMenuEntity(
    @PrimaryKey val id: String, // e.g. "MENU-MON"
    val dayOfWeek: String, // "MONDAY", "TUESDAY", ... "SUNDAY"
    val breakfast: String,
    val lunch: String,
    val dinner: String,
    val specialNotes: String = ""
)

@Entity(tableName = "mess_feedback")
data class MessFeedbackEntity(
    @PrimaryKey val id: String,
    val studentId: String,
    val studentName: String,
    val mealType: String, // "BREAKFAST", "LUNCH", "DINNER"
    val rating: Int, // 1 to 5
    val comments: String,
    val feedbackDate: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "mess_expenses")
data class MessExpenseEntity(
    @PrimaryKey val id: String, // e.g. "EXP-101"
    val expenseDate: String, // "2024-10-20"
    val itemDescription: String,
    val category: String, // "GROCERIES", "VEGETABLES", "DAIRY", "MEAT", "GAS_UTILITIES", "CLEANING"
    val quantity: String,
    val amount: Double,
    val recordedBy: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notices")
data class NoticeEntity(
    @PrimaryKey val id: String, // e.g. "NOT-101"
    val title: String,
    val body: String,
    val category: String, // "ANNOUNCEMENT", "FEE_REMINDER", "MAINTENANCE", "MESS", "RULES"
    val priority: String = "NORMAL", // "NORMAL", "HIGH", "URGENT"
    val author: String,
    val targetAudience: String = "ALL_STUDENTS", // "ALL_STUDENTS", "BUILDING_A", "STAFF"
    val publishedDate: String,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val recipientRole: String, // "ALL", "ADMIN", "WARDEN", "ACCOUNTANT", "STUDENT"
    val studentIdTarget: String? = null,
    val title: String,
    val message: String,
    val category: String,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey val id: String,
    val action: String, // "ALLOCATE_BED", "RECORD_PAYMENT", "RESOLVE_COMPLAINT", "APPROVE_LEAVE", "CREATE_STUDENT", "CHECKOUT_BED"
    val entityType: String,
    val entityId: String,
    val details: String,
    val performedBy: String,
    val timestamp: Long = System.currentTimeMillis()
)
