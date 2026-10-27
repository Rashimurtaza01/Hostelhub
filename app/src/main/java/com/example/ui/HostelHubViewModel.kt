package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.HostelHubDatabase
import com.example.data.model.*
import com.example.data.repository.HostelRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppNavDestination(val title: String) {
    DASHBOARD("Dashboard"),
    STUDENTS("Student Management"),
    BUILDINGS("Buildings & Rooms"),
    ALLOCATION("Bed Allocation"),
    FEES("Fees & Payments"),
    COMPLAINTS("Complaints"),
    LEAVE("Leave Management"),
    VISITORS("Visitor Register"),
    MESS("Mess Management"),
    STAFF("Staff Directory"),
    NOTICES("Notices & Alerts"),
    REPORTS("Reports & Analytics"),
    AUDIT_LOGS("Audit Logs"),
    PROFILE("Profile & Settings")
}

data class UiFeedback(
    val message: String,
    val isError: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

class HostelHubViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HostelRepository

    init {
        val database = HostelHubDatabase.getDatabase(application)
        repository = HostelRepository(database.hostelDao())
    }

    // Active User & Authentication
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    // Current active role (defaults to currentUser.role, but can quick-switch for FYP demonstration)
    private val _currentRole = MutableStateFlow(UserRole.ADMIN)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    // Current Navigation Screen
    private val _currentDestination = MutableStateFlow(AppNavDestination.DASHBOARD)
    val currentDestination: StateFlow<AppNavDestination> = _currentDestination.asStateFlow()

    // Feedback Toast / Snackbar
    private val _uiFeedback = MutableStateFlow<UiFeedback?>(null)
    val uiFeedback: StateFlow<UiFeedback?> = _uiFeedback.asStateFlow()

    fun clearFeedback() {
        _uiFeedback.value = null
    }

    // Data streams from repository
    val allUsers = repository.allUsers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allStudents = repository.allStudents.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val activeStudents = repository.activeStudents.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allStaff = repository.allStaff.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allBuildings = repository.allBuildings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allRooms = repository.allRooms.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allBeds = repository.allBeds.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val availableBeds = repository.availableBeds.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allAllocations = repository.allAllocations.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val activeAllocations = repository.activeAllocations.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allInvoices = repository.allInvoices.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allPayments = repository.allPayments.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allComplaints = repository.allComplaints.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allLeaveRequests = repository.allLeaveRequests.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allVisitors = repository.allVisitors.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val checkedInVisitors = repository.checkedInVisitors.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val messMenus = repository.messMenus.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val messFeedback = repository.messFeedback.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val messExpenses = repository.messExpenses.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val activeNotices = repository.activeNotices.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allNotifications = repository.allNotifications.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val recentAuditLogs = repository.recentAuditLogs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered / Role-specific states
    val studentComplaints = combine(allComplaints, _currentUser) { complaints, user ->
        if (user?.role == UserRole.STUDENT && user.studentIdRef != null) {
            complaints.filter { it.studentId == user.studentIdRef }
        } else complaints
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studentLeaveRequests = combine(allLeaveRequests, _currentUser) { requests, user ->
        if (user?.role == UserRole.STUDENT && user.studentIdRef != null) {
            requests.filter { it.studentId == user.studentIdRef }
        } else requests
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studentInvoices = combine(allInvoices, _currentUser) { invoices, user ->
        if (user?.role == UserRole.STUDENT && user.studentIdRef != null) {
            invoices.filter { it.studentId == user.studentIdRef }
        } else invoices
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationsCount = allNotifications.map { list ->
        list.count { !it.isRead }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        // Set default initial user as Admin for immediate full access, can be switched anytime
        setDemoUser(UserRole.ADMIN)
    }

    fun setDemoUser(role: UserRole) {
        val user = when (role) {
            UserRole.ADMIN -> UserEntity(
                id = "USR-ADM-01",
                email = "admin@hostelhub.com",
                passwordHash = "admin123",
                fullName = "Dr. Tariq Mehmood",
                role = UserRole.ADMIN,
                phone = "+92 300 1234567"
            )
            UserRole.WARDEN -> UserEntity(
                id = "USR-WRD-01",
                email = "warden@hostelhub.com",
                passwordHash = "warden123",
                fullName = "Maj. (R) Asim Javed",
                role = UserRole.WARDEN,
                phone = "+92 321 7654321",
                staffIdRef = "STF-101"
            )
            UserRole.ACCOUNTANT -> UserEntity(
                id = "USR-ACT-01",
                email = "accountant@hostelhub.com",
                passwordHash = "acct123",
                fullName = "Sohail Qureshi",
                role = UserRole.ACCOUNTANT,
                phone = "+92 333 5556677",
                staffIdRef = "STF-102"
            )
            UserRole.STUDENT -> UserEntity(
                id = "USR-STD-01",
                email = "student@hostelhub.com",
                passwordHash = "student123",
                fullName = "Muhammad Ali Khan",
                role = UserRole.STUDENT,
                phone = "+92 345 9876543",
                studentIdRef = "STD-2024-001"
            )
        }
        _currentUser.value = user
        _currentRole.value = role
        _currentDestination.value = AppNavDestination.DASHBOARD
        _uiFeedback.value = UiFeedback("Logged in as ${user.fullName} (${role.name})")
    }

    fun login(email: String, passwordAttempt: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.authenticateUser(email, passwordAttempt)
            if (result.isSuccess) {
                val user = result.getOrNull()
                _currentUser.value = user
                _currentRole.value = user?.role ?: UserRole.STUDENT
                _uiFeedback.value = UiFeedback("Welcome back, ${user?.fullName}!")
                onComplete(true)
            } else {
                _uiFeedback.value = UiFeedback(result.exceptionOrNull()?.message ?: "Login failed", isError = true)
                onComplete(false)
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _uiFeedback.value = UiFeedback("Logged out successfully.")
    }

    fun navigateTo(destination: AppNavDestination) {
        _currentDestination.value = destination
    }

    // Role Permissions check
    fun isAuthorizedFor(destination: AppNavDestination): Boolean {
        val role = _currentRole.value
        return when (destination) {
            AppNavDestination.DASHBOARD -> true
            AppNavDestination.STUDENTS -> role == UserRole.ADMIN || role == UserRole.WARDEN
            AppNavDestination.BUILDINGS -> role == UserRole.ADMIN || role == UserRole.WARDEN
            AppNavDestination.ALLOCATION -> role == UserRole.ADMIN || role == UserRole.WARDEN
            AppNavDestination.FEES -> role == UserRole.ADMIN || role == UserRole.ACCOUNTANT || role == UserRole.STUDENT
            AppNavDestination.COMPLAINTS -> true
            AppNavDestination.LEAVE -> true
            AppNavDestination.VISITORS -> role == UserRole.ADMIN || role == UserRole.WARDEN
            AppNavDestination.MESS -> true
            AppNavDestination.STAFF -> role == UserRole.ADMIN
            AppNavDestination.NOTICES -> true
            AppNavDestination.REPORTS -> role == UserRole.ADMIN || role == UserRole.ACCOUNTANT
            AppNavDestination.AUDIT_LOGS -> role == UserRole.ADMIN
            AppNavDestination.PROFILE -> true
        }
    }

    // STUDENT ACTIONS
    fun addStudent(
        id: String,
        fullName: String,
        email: String,
        phone: String,
        gender: String,
        department: String,
        degreeProgram: String,
        semester: Int,
        guardianName: String,
        guardianPhone: String,
        emergencyContact: String
    ) {
        viewModelScope.launch {
            val performer = _currentUser.value?.fullName ?: "Administrator"
            val result = repository.addStudent(
                id = id,
                fullName = fullName,
                email = email,
                phone = phone,
                gender = gender,
                department = department,
                degreeProgram = degreeProgram,
                semester = semester,
                guardianName = guardianName,
                guardianPhone = guardianPhone,
                emergencyContact = emergencyContact,
                adminName = performer
            )
            if (result.isSuccess) {
                _uiFeedback.value = UiFeedback("Student '${fullName}' registered successfully!")
            } else {
                _uiFeedback.value = UiFeedback(result.exceptionOrNull()?.message ?: "Failed to add student", isError = true)
            }
        }
    }

    fun deactivateStudent(studentId: String) {
        viewModelScope.launch {
            val performer = _currentUser.value?.fullName ?: "Administrator"
            val result = repository.deactivateStudent(studentId, performer)
            if (result.isSuccess) {
                _uiFeedback.value = UiFeedback("Student $studentId has been deactivated.")
            } else {
                _uiFeedback.value = UiFeedback(result.exceptionOrNull()?.message ?: "Error deactivating student", isError = true)
            }
        }
    }

    // BED ALLOCATION
    fun allocateBed(studentId: String, bedId: String, expectedCheckoutDate: String) {
        viewModelScope.launch {
            val performer = _currentUser.value?.fullName ?: "Hostel Warden"
            val result = repository.allocateBed(studentId, bedId, expectedCheckoutDate, performer)
            if (result.isSuccess) {
                _uiFeedback.value = UiFeedback("Bed successfully allocated to student!")
            } else {
                _uiFeedback.value = UiFeedback(result.exceptionOrNull()?.message ?: "Allocation failed", isError = true)
            }
        }
    }

    fun checkoutStudent(studentId: String) {
        viewModelScope.launch {
            val performer = _currentUser.value?.fullName ?: "Hostel Warden"
            val result = repository.checkoutStudent(studentId, performer)
            if (result.isSuccess) {
                _uiFeedback.value = UiFeedback("Student checked out successfully. Bed released.")
            } else {
                _uiFeedback.value = UiFeedback(result.exceptionOrNull()?.message ?: "Checkout failed", isError = true)
            }
        }
    }

    // INVOICES & PAYMENTS
    fun generateInvoice(
        studentId: String,
        billingMonth: String,
        roomRent: Double,
        messCharges: Double,
        utilityCharges: Double,
        otherCharges: Double,
        discount: Double,
        dueDate: String
    ) {
        viewModelScope.launch {
            val performer = _currentUser.value?.fullName ?: "Accounts Officer"
            val result = repository.generateInvoice(
                studentId = studentId,
                billingMonth = billingMonth,
                roomRent = roomRent,
                messCharges = messCharges,
                utilityCharges = utilityCharges,
                otherCharges = otherCharges,
                discount = discount,
                dueDate = dueDate,
                performer = performer
            )
            if (result.isSuccess) {
                _uiFeedback.value = UiFeedback("Invoice generated for $billingMonth!")
            } else {
                _uiFeedback.value = UiFeedback(result.exceptionOrNull()?.message ?: "Invoice generation failed", isError = true)
            }
        }
    }

    fun recordPayment(invoiceId: String, amount: Double, paymentMethod: String, notes: String) {
        viewModelScope.launch {
            val performer = _currentUser.value?.fullName ?: "Accounts Officer"
            val result = repository.recordPayment(invoiceId, amount, paymentMethod, notes, performer)
            if (result.isSuccess) {
                val receipt = result.getOrNull()?.paymentReference ?: ""
                _uiFeedback.value = UiFeedback("Payment of Rs. $amount recorded! Receipt: $receipt")
            } else {
                _uiFeedback.value = UiFeedback(result.exceptionOrNull()?.message ?: "Payment recording failed", isError = true)
            }
        }
    }

    // COMPLAINTS
    fun submitComplaint(category: String, title: String, description: String, priority: String) {
        viewModelScope.launch {
            val studentId = _currentUser.value?.studentIdRef ?: "STD-2024-001"
            val result = repository.submitComplaint(studentId, category, title, description, priority)
            if (result.isSuccess) {
                val ticket = result.getOrNull()?.ticketNumber ?: ""
                _uiFeedback.value = UiFeedback("Complaint ticket $ticket created!")
            } else {
                _uiFeedback.value = UiFeedback(result.exceptionOrNull()?.message ?: "Complaint submission failed", isError = true)
            }
        }
    }

    fun updateComplaintStatus(complaintId: String, newStatus: String, assignedStaff: String?, notes: String) {
        viewModelScope.launch {
            val performer = _currentUser.value?.fullName ?: "Hostel Warden"
            val result = repository.updateComplaintStatus(complaintId, newStatus, assignedStaff, notes, performer)
            if (result.isSuccess) {
                _uiFeedback.value = UiFeedback("Complaint status updated to $newStatus!")
            } else {
                _uiFeedback.value = UiFeedback(result.exceptionOrNull()?.message ?: "Update failed", isError = true)
            }
        }
    }

    // LEAVE REQUESTS
    fun submitLeaveRequest(startDate: String, endDate: String, destination: String, reason: String, emergencyContact: String) {
        viewModelScope.launch {
            val studentId = _currentUser.value?.studentIdRef ?: "STD-2024-001"
            val result = repository.submitLeaveRequest(studentId, startDate, endDate, destination, reason, emergencyContact)
            if (result.isSuccess) {
                _uiFeedback.value = UiFeedback("Leave application submitted for approval!")
            } else {
                _uiFeedback.value = UiFeedback(result.exceptionOrNull()?.message ?: "Submission failed", isError = true)
            }
        }
    }

    fun reviewLeaveRequest(requestId: String, approved: Boolean, reviewNotes: String) {
        viewModelScope.launch {
            val performer = _currentUser.value?.fullName ?: "Chief Warden"
            val result = repository.reviewLeaveRequest(requestId, approved, reviewNotes, performer)
            if (result.isSuccess) {
                val action = if (approved) "approved" else "rejected"
                _uiFeedback.value = UiFeedback("Leave request $action successfully.")
            } else {
                _uiFeedback.value = UiFeedback(result.exceptionOrNull()?.message ?: "Review failed", isError = true)
            }
        }
    }

    // VISITORS
    fun registerVisitor(
        visitorName: String,
        contact: String,
        cnic: String,
        studentId: String,
        relationship: String,
        purpose: String
    ) {
        viewModelScope.launch {
            val performer = _currentUser.value?.fullName ?: "Gate Security"
            val result = repository.registerVisitor(visitorName, contact, cnic, studentId, relationship, purpose, performer)
            if (result.isSuccess) {
                _uiFeedback.value = UiFeedback("Visitor $visitorName logged successfully.")
            } else {
                _uiFeedback.value = UiFeedback(result.exceptionOrNull()?.message ?: "Failed to log visitor", isError = true)
            }
        }
    }

    fun checkoutVisitor(visitor: VisitorEntity) {
        viewModelScope.launch {
            val updated = visitor.copy(
                status = "CHECKED_OUT",
                exitTime = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
            )
            repository.updateVisitorRecord(updated)
            _uiFeedback.value = UiFeedback("Visitor ${visitor.visitorName} checked out.")
        }
    }

    // MESS
    fun updateMessMenu(menu: MessMenuEntity) {
        viewModelScope.launch {
            val performer = _currentUser.value?.fullName ?: "Mess Warden"
            repository.updateMessMenu(menu, performer)
            _uiFeedback.value = UiFeedback("Mess menu updated for ${menu.dayOfWeek}!")
        }
    }

    fun submitMessFeedback(mealType: String, rating: Int, comments: String) {
        viewModelScope.launch {
            val studentId = _currentUser.value?.studentIdRef ?: "STD-2024-001"
            val result = repository.submitMessFeedback(studentId, mealType, rating, comments)
            if (result.isSuccess) {
                _uiFeedback.value = UiFeedback("Feedback submitted. Thank you!")
            }
        }
    }

    fun recordMessExpense(description: String, category: String, quantity: String, amount: Double) {
        viewModelScope.launch {
            val performer = _currentUser.value?.fullName ?: "Mess Accountant"
            val result = repository.recordMessExpense(description, category, quantity, amount, performer)
            if (result.isSuccess) {
                _uiFeedback.value = UiFeedback("Mess expense Rs. $amount recorded!")
            } else {
                _uiFeedback.value = UiFeedback(result.exceptionOrNull()?.message ?: "Expense recording failed", isError = true)
            }
        }
    }

    // NOTICES
    fun publishNotice(title: String, body: String, category: String, priority: String, targetAudience: String) {
        viewModelScope.launch {
            val author = _currentUser.value?.fullName ?: "Hostel Administration"
            val result = repository.publishNotice(title, body, category, priority, targetAudience, author)
            if (result.isSuccess) {
                _uiFeedback.value = UiFeedback("Notice published successfully!")
            } else {
                _uiFeedback.value = UiFeedback(result.exceptionOrNull()?.message ?: "Publishing failed", isError = true)
            }
        }
    }

    // BUILDINGS & ROOMS
    fun addBuilding(name: String, code: String, address: String, floors: Int) {
        viewModelScope.launch {
            val performer = _currentUser.value?.fullName ?: "Administrator"
            val result = repository.addBuilding(name, code, address, floors, performer)
            if (result.isSuccess) {
                _uiFeedback.value = UiFeedback("Building '$name' added!")
            } else {
                _uiFeedback.value = UiFeedback(result.exceptionOrNull()?.message ?: "Failed to add building", isError = true)
            }
        }
    }

    fun addRoom(
        buildingId: String,
        roomNumber: String,
        floor: Int,
        roomType: String,
        capacity: Int,
        monthlyRent: Double,
        description: String
    ) {
        viewModelScope.launch {
            val performer = _currentUser.value?.fullName ?: "Administrator"
            val result = repository.addRoom(buildingId, roomNumber, floor, roomType, capacity, monthlyRent, description, performer)
            if (result.isSuccess) {
                _uiFeedback.value = UiFeedback("Room $roomNumber added with $capacity beds!")
            } else {
                _uiFeedback.value = UiFeedback(result.exceptionOrNull()?.message ?: "Failed to add room", isError = true)
            }
        }
    }

    fun markNotificationAsRead(id: String) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }
}
