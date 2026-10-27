package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HostelDao {

    // USERS
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    // STUDENTS
    @Query("SELECT * FROM student_profiles ORDER BY admissionDate DESC")
    fun getAllStudents(): Flow<List<StudentProfileEntity>>

    @Query("SELECT * FROM student_profiles WHERE id = :id LIMIT 1")
    suspend fun getStudentById(id: String): StudentProfileEntity?

    @Query("SELECT * FROM student_profiles WHERE status = 'ACTIVE'")
    fun getActiveStudents(): Flow<List<StudentProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentProfileEntity)

    @Update
    suspend fun updateStudent(student: StudentProfileEntity)

    // STAFF
    @Query("SELECT * FROM staff_profiles ORDER BY fullName ASC")
    fun getAllStaff(): Flow<List<StaffProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaff(staff: StaffProfileEntity)

    @Update
    suspend fun updateStaff(staff: StaffProfileEntity)

    // BUILDINGS
    @Query("SELECT * FROM buildings ORDER BY name ASC")
    fun getAllBuildings(): Flow<List<BuildingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuilding(building: BuildingEntity)

    // ROOMS
    @Query("SELECT * FROM rooms ORDER BY roomNumber ASC")
    fun getAllRooms(): Flow<List<RoomEntity>>

    @Query("SELECT * FROM rooms WHERE buildingId = :buildingId ORDER BY roomNumber ASC")
    fun getRoomsByBuilding(buildingId: String): Flow<List<RoomEntity>>

    @Query("SELECT * FROM rooms WHERE id = :id LIMIT 1")
    suspend fun getRoomById(id: String): RoomEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoom(room: RoomEntity)

    @Update
    suspend fun updateRoom(room: RoomEntity)

    // BEDS
    @Query("SELECT * FROM beds")
    fun getAllBeds(): Flow<List<BedEntity>>

    @Query("SELECT * FROM beds WHERE roomId = :roomId")
    fun getBedsByRoom(roomId: String): Flow<List<BedEntity>>

    @Query("SELECT * FROM beds WHERE isOccupied = 0")
    fun getAvailableBeds(): Flow<List<BedEntity>>

    @Query("SELECT * FROM beds WHERE id = :id LIMIT 1")
    suspend fun getBedById(id: String): BedEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBed(bed: BedEntity)

    @Update
    suspend fun updateBed(bed: BedEntity)

    // ALLOCATIONS
    @Query("SELECT * FROM allocations ORDER BY allocationDate DESC")
    fun getAllAllocations(): Flow<List<AllocationEntity>>

    @Query("SELECT * FROM allocations WHERE status = 'ACTIVE'")
    fun getActiveAllocations(): Flow<List<AllocationEntity>>

    @Query("SELECT * FROM allocations WHERE studentId = :studentId AND status = 'ACTIVE' LIMIT 1")
    suspend fun getActiveAllocationForStudent(studentId: String): AllocationEntity?

    @Query("SELECT * FROM allocations WHERE id = :id LIMIT 1")
    suspend fun getAllocationById(id: String): AllocationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllocation(allocation: AllocationEntity)

    @Update
    suspend fun updateAllocation(allocation: AllocationEntity)

    // INVOICES
    @Query("SELECT * FROM invoices ORDER BY createdAt DESC")
    fun getAllInvoices(): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE studentId = :studentId ORDER BY createdAt DESC")
    fun getInvoicesByStudent(studentId: String): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE id = :id LIMIT 1")
    suspend fun getInvoiceById(id: String): InvoiceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity)

    @Update
    suspend fun updateInvoice(invoice: InvoiceEntity)

    // PAYMENTS
    @Query("SELECT * FROM payments ORDER BY createdAt DESC")
    fun getAllPayments(): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE studentId = :studentId ORDER BY createdAt DESC")
    fun getPaymentsByStudent(studentId: String): Flow<List<PaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity)

    // COMPLAINTS
    @Query("SELECT * FROM complaints ORDER BY createdAt DESC")
    fun getAllComplaints(): Flow<List<ComplaintEntity>>

    @Query("SELECT * FROM complaints WHERE studentId = :studentId ORDER BY createdAt DESC")
    fun getComplaintsByStudent(studentId: String): Flow<List<ComplaintEntity>>

    @Query("SELECT * FROM complaints WHERE id = :id LIMIT 1")
    suspend fun getComplaintById(id: String): ComplaintEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComplaint(complaint: ComplaintEntity)

    @Update
    suspend fun updateComplaint(complaint: ComplaintEntity)

    // LEAVE REQUESTS
    @Query("SELECT * FROM leave_requests ORDER BY createdAt DESC")
    fun getAllLeaveRequests(): Flow<List<LeaveRequestEntity>>

    @Query("SELECT * FROM leave_requests WHERE studentId = :studentId ORDER BY createdAt DESC")
    fun getLeaveRequestsByStudent(studentId: String): Flow<List<LeaveRequestEntity>>

    @Query("SELECT * FROM leave_requests WHERE id = :id LIMIT 1")
    suspend fun getLeaveRequestById(id: String): LeaveRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaveRequest(request: LeaveRequestEntity)

    @Update
    suspend fun updateLeaveRequest(request: LeaveRequestEntity)

    // VISITORS
    @Query("SELECT * FROM visitors ORDER BY createdAt DESC")
    fun getAllVisitors(): Flow<List<VisitorEntity>>

    @Query("SELECT * FROM visitors WHERE status = 'CHECKED_IN' ORDER BY createdAt DESC")
    fun getCheckedInVisitors(): Flow<List<VisitorEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisitor(visitor: VisitorEntity)

    @Update
    suspend fun updateVisitor(visitor: VisitorEntity)

    // MESS
    @Query("SELECT * FROM mess_menus ORDER BY id ASC")
    fun getMessMenus(): Flow<List<MessMenuEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessMenu(menu: MessMenuEntity)

    @Query("SELECT * FROM mess_feedback ORDER BY createdAt DESC")
    fun getAllMessFeedback(): Flow<List<MessFeedbackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessFeedback(feedback: MessFeedbackEntity)

    @Query("SELECT * FROM mess_expenses ORDER BY createdAt DESC")
    fun getAllMessExpenses(): Flow<List<MessExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessExpense(expense: MessExpenseEntity)

    // NOTICES
    @Query("SELECT * FROM notices WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getAllActiveNotices(): Flow<List<NoticeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotice(notice: NoticeEntity)

    // NOTIFICATIONS
    @Query("SELECT * FROM notifications ORDER BY createdAt DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: String)

    // AUDIT LOGS
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 100")
    fun getRecentAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)
}
