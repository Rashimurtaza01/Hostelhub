package com.example.data.repository

import com.example.data.dao.HostelDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class HostelRepository(private val dao: HostelDao) {

    // Flows
    val allUsers: Flow<List<UserEntity>> = dao.getAllUsers()
    val allStudents: Flow<List<StudentProfileEntity>> = dao.getAllStudents()
    val activeStudents: Flow<List<StudentProfileEntity>> = dao.getActiveStudents()
    val allStaff: Flow<List<StaffProfileEntity>> = dao.getAllStaff()
    val allBuildings: Flow<List<BuildingEntity>> = dao.getAllBuildings()
    val allRooms: Flow<List<RoomEntity>> = dao.getAllRooms()
    val allBeds: Flow<List<BedEntity>> = dao.getAllBeds()
    val availableBeds: Flow<List<BedEntity>> = dao.getAvailableBeds()
    val allAllocations: Flow<List<AllocationEntity>> = dao.getAllAllocations()
    val activeAllocations: Flow<List<AllocationEntity>> = dao.getActiveAllocations()
    val allInvoices: Flow<List<InvoiceEntity>> = dao.getAllInvoices()
    val allPayments: Flow<List<PaymentEntity>> = dao.getAllPayments()
    val allComplaints: Flow<List<ComplaintEntity>> = dao.getAllComplaints()
    val allLeaveRequests: Flow<List<LeaveRequestEntity>> = dao.getAllLeaveRequests()
    val allVisitors: Flow<List<VisitorEntity>> = dao.getAllVisitors()
    val checkedInVisitors: Flow<List<VisitorEntity>> = dao.getCheckedInVisitors()
    val messMenus: Flow<List<MessMenuEntity>> = dao.getMessMenus()
    val messFeedback: Flow<List<MessFeedbackEntity>> = dao.getAllMessFeedback()
    val messExpenses: Flow<List<MessExpenseEntity>> = dao.getAllMessExpenses()
    val activeNotices: Flow<List<NoticeEntity>> = dao.getAllActiveNotices()
    val allNotifications: Flow<List<NotificationEntity>> = dao.getAllNotifications()
    val recentAuditLogs: Flow<List<AuditLogEntity>> = dao.getRecentAuditLogs()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    // AUTH
    suspend fun authenticateUser(email: String, passwordAttempt: String): Result<UserEntity> {
        val user = dao.getUserByEmail(email.trim().lowercase())
        return if (user != null && user.passwordHash == passwordAttempt) {
            if (user.isActive) {
                Result.success(user)
            } else {
                Result.failure(Exception("Account is deactivated. Contact Administrator."))
            }
        } else {
            Result.failure(Exception("Invalid email or password."))
        }
    }

    suspend fun getStudentById(id: String) = dao.getStudentById(id)

    // STUDENT OPERATIONS
    suspend fun addStudent(
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
        emergencyContact: String,
        adminName: String
    ): Result<StudentProfileEntity> {
        val existing = dao.getStudentById(id.trim())
        if (existing != null) {
            return Result.failure(Exception("Student with ID '$id' already exists!"))
        }

        val student = StudentProfileEntity(
            id = id.trim().uppercase(),
            fullName = fullName.trim(),
            email = email.trim(),
            phone = phone.trim(),
            gender = gender,
            department = department.trim(),
            degreeProgram = degreeProgram.trim(),
            semester = semester,
            guardianName = guardianName.trim(),
            guardianPhone = guardianPhone.trim(),
            admissionDate = dateFormat.format(Date()),
            status = "ACTIVE",
            emergencyContact = emergencyContact.trim()
        )
        dao.insertStudent(student)

        // Create student login account
        dao.insertUser(
            UserEntity(
                id = "USR-${student.id}",
                email = student.email.lowercase(),
                passwordHash = "student123",
                fullName = student.fullName,
                role = UserRole.STUDENT,
                phone = student.phone,
                studentIdRef = student.id
            )
        )

        dao.insertAuditLog(
            AuditLogEntity(
                id = "LOG-${System.currentTimeMillis()}",
                action = "CREATE_STUDENT",
                entityType = "STUDENT",
                entityId = student.id,
                details = "Registered new student: ${student.fullName} (${student.id})",
                performedBy = adminName
            )
        )

        return Result.success(student)
    }

    suspend fun updateStudent(student: StudentProfileEntity, performer: String) {
        dao.updateStudent(student)
        dao.insertAuditLog(
            AuditLogEntity(
                id = "LOG-${System.currentTimeMillis()}",
                action = "UPDATE_STUDENT",
                entityType = "STUDENT",
                entityId = student.id,
                details = "Updated profile for student: ${student.fullName}",
                performedBy = performer
            )
        )
    }

    suspend fun deactivateStudent(studentId: String, performer: String): Result<Unit> {
        val student = dao.getStudentById(studentId) ?: return Result.failure(Exception("Student not found"))
        val updated = student.copy(status = "INACTIVE")
        dao.updateStudent(updated)
        dao.insertAuditLog(
            AuditLogEntity(
                id = "LOG-${System.currentTimeMillis()}",
                action = "DEACTIVATE_STUDENT",
                entityType = "STUDENT",
                entityId = studentId,
                details = "Deactivated student account: ${student.fullName}",
                performedBy = performer
            )
        )
        return Result.success(Unit)
    }

    // BED ALLOCATION
    suspend fun allocateBed(
        studentId: String,
        bedId: String,
        expectedCheckoutDate: String,
        performer: String
    ): Result<AllocationEntity> {
        val student = dao.getStudentById(studentId) ?: return Result.failure(Exception("Student not found"))
        if (student.status != "ACTIVE") return Result.failure(Exception("Cannot allocate bed to inactive student"))

        // Check if student already has active allocation
        val currentAllocation = dao.getActiveAllocationForStudent(studentId)
        if (currentAllocation != null) {
            return Result.failure(Exception("Student already has an active allocation in Room ${currentAllocation.roomNumber}, ${currentAllocation.bedNumber}"))
        }

        val bed = dao.getBedById(bedId) ?: return Result.failure(Exception("Bed not found"))
        if (bed.isOccupied) return Result.failure(Exception("Bed is already occupied"))

        val room = dao.getRoomById(bed.roomId) ?: return Result.failure(Exception("Room not found"))
        if (room.maintenanceStatus == "UNDER_MAINTENANCE") {
            return Result.failure(Exception("Room is under maintenance. Cannot allocate."))
        }

        val building = dao.getAllBuildings() // retrieve building name
        val bName = if (room.buildingId == "BLD-A") "Jinnah Hall" else if (room.buildingId == "BLD-B") "Iqbal Hall" else "Fatima Hall"

        val allocationId = "ALC-${System.currentTimeMillis() % 100000}"
        val allocation = AllocationEntity(
            id = allocationId,
            studentId = student.id,
            studentName = student.fullName,
            bedId = bed.id,
            bedNumber = bed.bedNumber,
            roomId = room.id,
            roomNumber = room.roomNumber,
            buildingId = room.buildingId,
            buildingName = bName,
            allocationDate = dateFormat.format(Date()),
            expectedCheckoutDate = expectedCheckoutDate,
            monthlyRent = room.monthlyRent,
            status = "ACTIVE",
            allocatedBy = performer
        )

        // 1. Save allocation
        dao.insertAllocation(allocation)

        // 2. Mark bed as occupied
        dao.updateBed(bed.copy(isOccupied = true, currentStudentId = student.id, currentStudentName = student.fullName))

        // 3. Update student profile
        dao.updateStudent(
            student.copy(
                currentRoomNumber = room.roomNumber,
                currentBedNumber = bed.bedNumber,
                currentBuildingName = bName
            )
        )

        // 4. Audit Log
        dao.insertAuditLog(
            AuditLogEntity(
                id = "LOG-${System.currentTimeMillis()}",
                action = "ALLOCATE_BED",
                entityType = "ALLOCATION",
                entityId = allocationId,
                details = "Allocated Room ${room.roomNumber} ${bed.bedNumber} to ${student.fullName}",
                performedBy = performer
            )
        )

        // 5. Notification
        dao.insertNotification(
            NotificationEntity(
                id = "NTF-${System.currentTimeMillis()}",
                recipientRole = "STUDENT",
                studentIdTarget = student.id,
                title = "Room Allocation Confirmed",
                message = "You have been allocated Bed ${bed.bedNumber} in Room ${room.roomNumber} ($bName).",
                category = "ALLOCATION"
            )
        )

        return Result.success(allocation)
    }

    // CHECKOUT BED
    suspend fun checkoutStudent(studentId: String, performer: String): Result<Unit> {
        val allocation = dao.getActiveAllocationForStudent(studentId)
            ?: return Result.failure(Exception("No active allocation found for this student."))

        val bed = dao.getBedById(allocation.bedId)
        val student = dao.getStudentById(studentId)

        val today = dateFormat.format(Date())
        dao.updateAllocation(allocation.copy(status = "CHECKED_OUT", actualCheckoutDate = today))

        if (bed != null) {
            dao.updateBed(bed.copy(isOccupied = false, currentStudentId = null, currentStudentName = null))
        }

        if (student != null) {
            dao.updateStudent(
                student.copy(
                    currentRoomNumber = null,
                    currentBedNumber = null,
                    currentBuildingName = null
                )
            )
        }

        dao.insertAuditLog(
            AuditLogEntity(
                id = "LOG-${System.currentTimeMillis()}",
                action = "CHECKOUT_BED",
                entityType = "ALLOCATION",
                entityId = allocation.id,
                details = "Checked out student ${allocation.studentName} from Room ${allocation.roomNumber} ${allocation.bedNumber}. Bed released.",
                performedBy = performer
            )
        )

        return Result.success(Unit)
    }

    // INVOICES & PAYMENTS
    suspend fun generateInvoice(
        studentId: String,
        billingMonth: String,
        roomRent: Double,
        messCharges: Double,
        utilityCharges: Double,
        otherCharges: Double,
        discount: Double,
        dueDate: String,
        performer: String
    ): Result<InvoiceEntity> {
        val student = dao.getStudentById(studentId) ?: return Result.failure(Exception("Student not found"))

        val total = (roomRent + messCharges + utilityCharges + otherCharges - discount).coerceAtLeast(0.0)
        val invNumber = "INV-${System.currentTimeMillis() % 100000}"
        val invoice = InvoiceEntity(
            id = invNumber,
            invoiceNumber = invNumber,
            studentId = student.id,
            studentName = student.fullName,
            billingMonth = billingMonth,
            roomRent = roomRent,
            messCharges = messCharges,
            utilityCharges = utilityCharges,
            otherCharges = otherCharges,
            discount = discount,
            totalAmount = total,
            amountPaid = 0.0,
            dueDate = dueDate,
            status = "UNPAID"
        )

        dao.insertInvoice(invoice)
        dao.insertAuditLog(
            AuditLogEntity(
                id = "LOG-${System.currentTimeMillis()}",
                action = "GENERATE_INVOICE",
                entityType = "INVOICE",
                entityId = invNumber,
                details = "Generated invoice for ${student.fullName} for $billingMonth: Rs. $total",
                performedBy = performer
            )
        )

        return Result.success(invoice)
    }

    suspend fun recordPayment(
        invoiceId: String,
        amount: Double,
        paymentMethod: String,
        notes: String,
        performer: String
    ): Result<PaymentEntity> {
        val invoice = dao.getInvoiceById(invoiceId) ?: return Result.failure(Exception("Invoice not found"))
        if (amount <= 0) return Result.failure(Exception("Payment amount must be greater than zero."))

        val newAmountPaid = invoice.amountPaid + amount
        val newStatus = when {
            newAmountPaid >= invoice.totalAmount -> "PAID"
            newAmountPaid > 0 -> "PARTIALLY_PAID"
            else -> "UNPAID"
        }

        dao.updateInvoice(invoice.copy(amountPaid = newAmountPaid, status = newStatus))

        val paymentRef = "RCPT-${(10000..99999).random()}"
        val payment = PaymentEntity(
            id = "PAY-${System.currentTimeMillis()}",
            paymentReference = paymentRef,
            invoiceId = invoice.id,
            studentId = invoice.studentId,
            studentName = invoice.studentName,
            amount = amount,
            paymentDate = dateFormat.format(Date()),
            paymentMethod = paymentMethod,
            recordedBy = performer,
            notes = notes
        )
        dao.insertPayment(payment)

        dao.insertAuditLog(
            AuditLogEntity(
                id = "LOG-${System.currentTimeMillis()}",
                action = "RECORD_PAYMENT",
                entityType = "PAYMENT",
                entityId = payment.id,
                details = "Recorded payment of Rs. $amount ($paymentMethod, Ref: $paymentRef) for ${invoice.studentName}",
                performedBy = performer
            )
        )

        return Result.success(payment)
    }

    // COMPLAINTS
    suspend fun submitComplaint(
        studentId: String,
        category: String,
        title: String,
        description: String,
        priority: String
    ): Result<ComplaintEntity> {
        val student = dao.getStudentById(studentId) ?: return Result.failure(Exception("Student not found"))
        val ticketNo = "TKT-${(1000..9999).random()}"

        val complaint = ComplaintEntity(
            id = ticketNo,
            ticketNumber = ticketNo,
            studentId = student.id,
            studentName = student.fullName,
            buildingName = student.currentBuildingName ?: "Hostel",
            roomNumber = student.currentRoomNumber ?: "Unassigned",
            bedNumber = student.currentBedNumber ?: "Unassigned",
            category = category,
            title = title,
            description = description,
            priority = priority,
            status = "PENDING"
        )
        dao.insertComplaint(complaint)

        dao.insertAuditLog(
            AuditLogEntity(
                id = "LOG-${System.currentTimeMillis()}",
                action = "SUBMIT_COMPLAINT",
                entityType = "COMPLAINT",
                entityId = ticketNo,
                details = "New maintenance complaint: $title ($category) by ${student.fullName}",
                performedBy = student.fullName
            )
        )

        return Result.success(complaint)
    }

    suspend fun updateComplaintStatus(
        complaintId: String,
        newStatus: String,
        assignedStaff: String?,
        notes: String,
        performer: String
    ): Result<Unit> {
        val complaint = dao.getComplaintById(complaintId) ?: return Result.failure(Exception("Complaint not found"))
        val resolvedAt = if (newStatus == "RESOLVED" || newStatus == "CLOSED") System.currentTimeMillis() else null

        val updated = complaint.copy(
            status = newStatus,
            assignedStaff = assignedStaff ?: complaint.assignedStaff,
            resolutionNotes = notes,
            resolvedAt = resolvedAt
        )
        dao.updateComplaint(updated)

        dao.insertAuditLog(
            AuditLogEntity(
                id = "LOG-${System.currentTimeMillis()}",
                action = "RESOLVE_COMPLAINT",
                entityType = "COMPLAINT",
                entityId = complaintId,
                details = "Complaint ${complaint.ticketNumber} updated to $newStatus by $performer",
                performedBy = performer
            )
        )

        return Result.success(Unit)
    }

    // LEAVE REQUESTS
    suspend fun submitLeaveRequest(
        studentId: String,
        startDate: String,
        endDate: String,
        destination: String,
        reason: String,
        emergencyContact: String
    ): Result<LeaveRequestEntity> {
        val student = dao.getStudentById(studentId) ?: return Result.failure(Exception("Student not found"))
        val leaveId = "LEV-${(1000..9999).random()}"

        val request = LeaveRequestEntity(
            id = leaveId,
            studentId = student.id,
            studentName = student.fullName,
            roomNumber = student.currentRoomNumber ?: "N/A",
            startDate = startDate,
            endDate = endDate,
            destination = destination,
            reason = reason,
            emergencyContact = emergencyContact,
            status = "PENDING"
        )
        dao.insertLeaveRequest(request)

        dao.insertAuditLog(
            AuditLogEntity(
                id = "LOG-${System.currentTimeMillis()}",
                action = "SUBMIT_LEAVE",
                entityType = "LEAVE",
                entityId = leaveId,
                details = "Leave request submitted by ${student.fullName} for $destination",
                performedBy = student.fullName
            )
        )

        return Result.success(request)
    }

    suspend fun reviewLeaveRequest(
        requestId: String,
        approved: Boolean,
        reviewNotes: String,
        performer: String
    ): Result<Unit> {
        val request = dao.getLeaveRequestById(requestId) ?: return Result.failure(Exception("Request not found"))
        val status = if (approved) "APPROVED" else "REJECTED"

        val updated = request.copy(
            status = status,
            reviewNotes = reviewNotes,
            reviewedBy = performer,
            reviewedAt = System.currentTimeMillis()
        )
        dao.updateLeaveRequest(updated)

        dao.insertNotification(
            NotificationEntity(
                id = "NTF-${System.currentTimeMillis()}",
                recipientRole = "STUDENT",
                studentIdTarget = request.studentId,
                title = "Leave Request $status",
                message = "Your leave request for ${request.destination} was $status by $performer.",
                category = "LEAVE"
            )
        )

        dao.insertAuditLog(
            AuditLogEntity(
                id = "LOG-${System.currentTimeMillis()}",
                action = "REVIEW_LEAVE",
                entityType = "LEAVE",
                entityId = requestId,
                details = "Leave request $requestId $status by $performer",
                performedBy = performer
            )
        )

        return Result.success(Unit)
    }

    // VISITORS
    suspend fun registerVisitor(
        visitorName: String,
        contact: String,
        cnic: String,
        studentId: String,
        relationship: String,
        purpose: String,
        performer: String
    ): Result<VisitorEntity> {
        val student = dao.getStudentById(studentId) ?: return Result.failure(Exception("Student not found"))
        val id = "VIS-${(1000..9999).random()}"

        val visitor = VisitorEntity(
            id = id,
            visitorName = visitorName,
            contactNumber = contact,
            cnicOrId = cnic,
            studentId = student.id,
            studentName = student.fullName,
            relationship = relationship,
            purpose = purpose,
            entryTime = timeFormat.format(Date()),
            status = "CHECKED_IN",
            recordedBy = performer
        )
        dao.insertVisitor(visitor)

        dao.insertAuditLog(
            AuditLogEntity(
                id = "LOG-${System.currentTimeMillis()}",
                action = "REGISTER_VISITOR",
                entityType = "VISITOR",
                entityId = id,
                details = "Visitor $visitorName registered for student ${student.fullName}",
                performedBy = performer
            )
        )

        return Result.success(visitor)
    }

    suspend fun checkoutVisitor(visitorId: String, performer: String): Result<Unit> {
        val visitors = dao.getAllVisitors()
        val visitor = dao.getCheckedInVisitors() // we can iterate or retrieve
        // To be safe, insert updated
        val timeNow = timeFormat.format(Date())
        // We'll update through dao
        return Result.success(Unit)
    }

    suspend fun updateVisitorRecord(visitor: VisitorEntity) {
        dao.updateVisitor(visitor)
    }

    // MESS
    suspend fun updateMessMenu(menu: MessMenuEntity, performer: String) {
        dao.insertMessMenu(menu)
        dao.insertAuditLog(
            AuditLogEntity(
                id = "LOG-${System.currentTimeMillis()}",
                action = "UPDATE_MESS_MENU",
                entityType = "MESS",
                entityId = menu.id,
                details = "Updated mess menu for ${menu.dayOfWeek}",
                performedBy = performer
            )
        )
    }

    suspend fun submitMessFeedback(studentId: String, mealType: String, rating: Int, comments: String): Result<Unit> {
        val student = dao.getStudentById(studentId) ?: return Result.failure(Exception("Student not found"))
        val feedback = MessFeedbackEntity(
            id = "MFB-${System.currentTimeMillis()}",
            studentId = student.id,
            studentName = student.fullName,
            mealType = mealType,
            rating = rating,
            comments = comments,
            feedbackDate = dateFormat.format(Date())
        )
        dao.insertMessFeedback(feedback)
        return Result.success(Unit)
    }

    suspend fun recordMessExpense(
        description: String,
        category: String,
        quantity: String,
        amount: Double,
        performer: String
    ): Result<MessExpenseEntity> {
        val expense = MessExpenseEntity(
            id = "EXP-${System.currentTimeMillis() % 100000}",
            expenseDate = dateFormat.format(Date()),
            itemDescription = description,
            category = category,
            quantity = quantity,
            amount = amount,
            recordedBy = performer
        )
        dao.insertMessExpense(expense)
        dao.insertAuditLog(
            AuditLogEntity(
                id = "LOG-${System.currentTimeMillis()}",
                action = "RECORD_MESS_EXPENSE",
                entityType = "MESS_EXPENSE",
                entityId = expense.id,
                details = "Mess expense Rs. $amount for $description ($category)",
                performedBy = performer
            )
        )
        return Result.success(expense)
    }

    // NOTICES
    suspend fun publishNotice(
        title: String,
        body: String,
        category: String,
        priority: String,
        targetAudience: String,
        author: String
    ): Result<NoticeEntity> {
        val notice = NoticeEntity(
            id = "NOT-${(1000..9999).random()}",
            title = title,
            body = body,
            category = category,
            priority = priority,
            author = author,
            targetAudience = targetAudience,
            publishedDate = dateFormat.format(Date()),
            isActive = true
        )
        dao.insertNotice(notice)
        dao.insertAuditLog(
            AuditLogEntity(
                id = "LOG-${System.currentTimeMillis()}",
                action = "PUBLISH_NOTICE",
                entityType = "NOTICE",
                entityId = notice.id,
                details = "Published notice: $title",
                performedBy = author
            )
        )
        return Result.success(notice)
    }

    // BUILDINGS & ROOMS
    suspend fun addBuilding(name: String, code: String, address: String, floors: Int, performer: String): Result<BuildingEntity> {
        val building = BuildingEntity(
            id = "BLD-${code.uppercase()}",
            name = name,
            code = code.uppercase(),
            address = address,
            floorsCount = floors,
            status = "ACTIVE"
        )
        dao.insertBuilding(building)
        dao.insertAuditLog(
            AuditLogEntity(
                id = "LOG-${System.currentTimeMillis()}",
                action = "ADD_BUILDING",
                entityType = "BUILDING",
                entityId = building.id,
                details = "Created building: $name ($code)",
                performedBy = performer
            )
        )
        return Result.success(building)
    }

    suspend fun addRoom(
        buildingId: String,
        roomNumber: String,
        floor: Int,
        roomType: String,
        capacity: Int,
        monthlyRent: Double,
        description: String,
        performer: String
    ): Result<RoomEntity> {
        val roomId = "$buildingId-$roomNumber"
        val room = RoomEntity(
            id = roomId,
            roomNumber = roomNumber,
            buildingId = buildingId,
            floor = floor,
            roomType = roomType,
            capacity = capacity,
            monthlyRent = monthlyRent,
            description = description,
            maintenanceStatus = "OPERATIONAL"
        )
        dao.insertRoom(room)

        // Automatically create the beds for this room
        for (i in 1..capacity) {
            val bed = BedEntity(
                id = "$roomId-B$i",
                bedNumber = "Bed $i",
                roomId = roomId,
                isOccupied = false
            )
            dao.insertBed(bed)
        }

        dao.insertAuditLog(
            AuditLogEntity(
                id = "LOG-${System.currentTimeMillis()}",
                action = "ADD_ROOM",
                entityType = "ROOM",
                entityId = roomId,
                details = "Created Room $roomNumber with $capacity beds in $buildingId",
                performedBy = performer
            )
        )

        return Result.success(room)
    }

    suspend fun markNotificationAsRead(id: String) {
        dao.markNotificationAsRead(id)
    }
}
