package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.HostelDao
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        StudentProfileEntity::class,
        StaffProfileEntity::class,
        BuildingEntity::class,
        RoomEntity::class,
        BedEntity::class,
        AllocationEntity::class,
        InvoiceEntity::class,
        PaymentEntity::class,
        ComplaintEntity::class,
        LeaveRequestEntity::class,
        VisitorEntity::class,
        MessMenuEntity::class,
        MessFeedbackEntity::class,
        MessExpenseEntity::class,
        NoticeEntity::class,
        NotificationEntity::class,
        AuditLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class HostelHubDatabase : RoomDatabase() {

    abstract fun hostelDao(): HostelDao

    companion object {
        @Volatile
        private var INSTANCE: HostelHubDatabase? = null

        fun getDatabase(context: Context): HostelHubDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HostelHubDatabase::class.java,
                    "hostelhub_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database.hostelDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(dao: HostelDao) {
            // 1. Initial Users
            dao.insertUser(
                UserEntity(
                    id = "USR-ADM-01",
                    email = "admin@hostelhub.com",
                    passwordHash = "admin123",
                    fullName = "Dr. Tariq Mehmood",
                    role = UserRole.ADMIN,
                    phone = "+92 300 1234567"
                )
            )
            dao.insertUser(
                UserEntity(
                    id = "USR-WRD-01",
                    email = "warden@hostelhub.com",
                    passwordHash = "warden123",
                    fullName = "Maj. (R) Asim Javed",
                    role = UserRole.WARDEN,
                    phone = "+92 321 7654321",
                    staffIdRef = "STF-101"
                )
            )
            dao.insertUser(
                UserEntity(
                    id = "USR-ACT-01",
                    email = "accountant@hostelhub.com",
                    passwordHash = "acct123",
                    fullName = "Sohail Qureshi",
                    role = UserRole.ACCOUNTANT,
                    phone = "+92 333 5556677",
                    staffIdRef = "STF-102"
                )
            )
            dao.insertUser(
                UserEntity(
                    id = "USR-STD-01",
                    email = "student@hostelhub.com",
                    passwordHash = "student123",
                    fullName = "Muhammad Ali Khan",
                    role = UserRole.STUDENT,
                    phone = "+92 345 9876543",
                    studentIdRef = "STD-2024-001"
                )
            )

            // 2. Staff Profiles
            dao.insertStaff(
                StaffProfileEntity(
                    id = "STF-101",
                    fullName = "Maj. (R) Asim Javed",
                    email = "warden@hostelhub.com",
                    phone = "+92 321 7654321",
                    role = UserRole.WARDEN,
                    assignedBuildingId = "BLD-A",
                    status = "ACTIVE",
                    joiningDate = "2021-08-15"
                )
            )
            dao.insertStaff(
                StaffProfileEntity(
                    id = "STF-102",
                    fullName = "Sohail Qureshi",
                    email = "accountant@hostelhub.com",
                    phone = "+92 333 5556677",
                    role = UserRole.ACCOUNTANT,
                    assignedBuildingId = null,
                    status = "ACTIVE",
                    joiningDate = "2022-03-01"
                )
            )
            dao.insertStaff(
                StaffProfileEntity(
                    id = "STF-103",
                    fullName = "Raza Haider",
                    email = "raza.caretaker@hostelhub.com",
                    phone = "+92 312 4443322",
                    role = UserRole.WARDEN,
                    assignedBuildingId = "BLD-B",
                    status = "ACTIVE",
                    joiningDate = "2023-01-10"
                )
            )

            // 3. Buildings
            dao.insertBuilding(
                BuildingEntity(
                    id = "BLD-A",
                    name = "Jinnah Hall",
                    code = "JH-A",
                    address = "North Campus, Gate 2",
                    floorsCount = 4,
                    status = "ACTIVE"
                )
            )
            dao.insertBuilding(
                BuildingEntity(
                    id = "BLD-B",
                    name = "Iqbal Hall",
                    code = "IH-B",
                    address = "East Wing, Sector 7",
                    floorsCount = 3,
                    status = "ACTIVE"
                )
            )
            dao.insertBuilding(
                BuildingEntity(
                    id = "BLD-C",
                    name = "Fatima Hall",
                    code = "FH-C",
                    address = "South Compound, Executive Enclave",
                    floorsCount = 3,
                    status = "ACTIVE"
                )
            )

            // 4. Rooms & Beds for Jinnah Hall (BLD-A)
            val roomsA = listOf(
                RoomEntity("JH-101", "101", "BLD-A", 1, "DOUBLE", 2, 18000.0, "Spacious room with balcony and attached bath"),
                RoomEntity("JH-102", "102", "BLD-A", 1, "DOUBLE", 2, 18000.0, "East-facing double bed room"),
                RoomEntity("JH-103", "103", "BLD-A", 1, "SINGLE", 1, 26000.0, "Executive single deluxe room"),
                RoomEntity("JH-201", "201", "BLD-A", 2, "TRIPLE", 3, 14000.0, "Standard 3-seater room with study desks"),
                RoomEntity("JH-202", "202", "BLD-A", 2, "DOUBLE", 2, 18000.0, "Quiet room near reading lounge")
            )
            roomsA.forEach { dao.insertRoom(it) }

            // Beds for JH-101
            dao.insertBed(BedEntity("JH-101-B1", "Bed 1", "JH-101", isOccupied = true, currentStudentId = "STD-2024-001", currentStudentName = "Muhammad Ali Khan"))
            dao.insertBed(BedEntity("JH-101-B2", "Bed 2", "JH-101", isOccupied = true, currentStudentId = "STD-2024-002", currentStudentName = "Hamza Tariq"))

            // Beds for JH-102
            dao.insertBed(BedEntity("JH-102-B1", "Bed 1", "JH-102", isOccupied = true, currentStudentId = "STD-2024-003", currentStudentName = "Bilal Ahmed"))
            dao.insertBed(BedEntity("JH-102-B2", "Bed 2", "JH-102", isOccupied = false))

            // Beds for JH-103
            dao.insertBed(BedEntity("JH-103-B1", "Bed 1", "JH-103", isOccupied = false))

            // Beds for JH-201
            dao.insertBed(BedEntity("JH-201-B1", "Bed 1", "JH-201", isOccupied = true, currentStudentId = "STD-2024-004", currentStudentName = "Usman Farooq"))
            dao.insertBed(BedEntity("JH-201-B2", "Bed 2", "JH-201", isOccupied = false))
            dao.insertBed(BedEntity("JH-201-B3", "Bed 3", "JH-201", isOccupied = false))

            // Beds for JH-202
            dao.insertBed(BedEntity("JH-202-B1", "Bed 1", "JH-202", isOccupied = false))
            dao.insertBed(BedEntity("JH-202-B2", "Bed 2", "JH-202", isOccupied = false))

            // Rooms & Beds for Iqbal Hall (BLD-B)
            val roomsB = listOf(
                RoomEntity("IH-101", "101", "BLD-B", 1, "DOUBLE", 2, 17500.0, "Ground floor standard double room"),
                RoomEntity("IH-102", "102", "BLD-B", 1, "DOUBLE", 2, 17500.0, "Garden facing double room"),
                RoomEntity("IH-201", "201", "BLD-B", 2, "SINGLE", 1, 25000.0, "Single private room with AC")
            )
            roomsB.forEach { dao.insertRoom(it) }
            dao.insertBed(BedEntity("IH-101-B1", "Bed 1", "IH-101", isOccupied = false))
            dao.insertBed(BedEntity("IH-101-B2", "Bed 2", "IH-101", isOccupied = false))
            dao.insertBed(BedEntity("IH-102-B1", "Bed 1", "IH-102", isOccupied = false))
            dao.insertBed(BedEntity("IH-102-B2", "Bed 2", "IH-102", isOccupied = false))
            dao.insertBed(BedEntity("IH-201-B1", "Bed 1", "IH-201", isOccupied = false))

            // 5. Students
            dao.insertStudent(
                StudentProfileEntity(
                    id = "STD-2024-001",
                    fullName = "Muhammad Ali Khan",
                    email = "student@hostelhub.com",
                    phone = "+92 345 9876543",
                    gender = "Male",
                    department = "Computer Science",
                    degreeProgram = "BS Software Engineering",
                    semester = 6,
                    guardianName = "Tariq Khan",
                    guardianPhone = "+92 301 2233445",
                    admissionDate = "2024-01-10",
                    status = "ACTIVE",
                    emergencyContact = "+92 301 2233445 (Father)",
                    currentRoomNumber = "101",
                    currentBedNumber = "Bed 1",
                    currentBuildingName = "Jinnah Hall"
                )
            )
            dao.insertStudent(
                StudentProfileEntity(
                    id = "STD-2024-002",
                    fullName = "Hamza Tariq",
                    email = "hamza.t@student.edu",
                    phone = "+92 321 8899776",
                    gender = "Male",
                    department = "Electrical Engineering",
                    degreeProgram = "BS Electrical Eng",
                    semester = 4,
                    guardianName = "Tariq Bashir",
                    guardianPhone = "+92 302 7788990",
                    admissionDate = "2024-02-01",
                    status = "ACTIVE",
                    emergencyContact = "+92 302 7788990 (Father)",
                    currentRoomNumber = "101",
                    currentBedNumber = "Bed 2",
                    currentBuildingName = "Jinnah Hall"
                )
            )
            dao.insertStudent(
                StudentProfileEntity(
                    id = "STD-2024-003",
                    fullName = "Bilal Ahmed",
                    email = "bilal.ahmed@student.edu",
                    phone = "+92 334 1122334",
                    gender = "Male",
                    department = "Business Administration",
                    degreeProgram = "BBA Marketing",
                    semester = 2,
                    guardianName = "Ahmed Kamal",
                    guardianPhone = "+92 303 4455667",
                    admissionDate = "2024-03-15",
                    status = "ACTIVE",
                    emergencyContact = "+92 303 4455667 (Guardian)",
                    currentRoomNumber = "102",
                    currentBedNumber = "Bed 1",
                    currentBuildingName = "Jinnah Hall"
                )
            )
            dao.insertStudent(
                StudentProfileEntity(
                    id = "STD-2024-004",
                    fullName = "Usman Farooq",
                    email = "usman.f@student.edu",
                    phone = "+92 315 9988776",
                    gender = "Male",
                    department = "Mechanical Engineering",
                    degreeProgram = "BS Mechanical Eng",
                    semester = 8,
                    guardianName = "Farooq Azam",
                    guardianPhone = "+92 304 6677889",
                    admissionDate = "2023-09-01",
                    status = "ACTIVE",
                    emergencyContact = "+92 304 6677889 (Father)",
                    currentRoomNumber = "201",
                    currentBedNumber = "Bed 1",
                    currentBuildingName = "Jinnah Hall"
                )
            )
            dao.insertStudent(
                StudentProfileEntity(
                    id = "STD-2024-005",
                    fullName = "Saad Munir",
                    email = "saad.m@student.edu",
                    phone = "+92 300 4433221",
                    gender = "Male",
                    department = "Data Science",
                    degreeProgram = "BS Data Science",
                    semester = 3,
                    guardianName = "Munir Hussain",
                    guardianPhone = "+92 305 8899001",
                    admissionDate = "2024-04-10",
                    status = "ACTIVE",
                    emergencyContact = "+92 305 8899001",
                    currentRoomNumber = null,
                    currentBedNumber = null,
                    currentBuildingName = null
                )
            )

            // 6. Allocations
            dao.insertAllocation(
                AllocationEntity(
                    id = "ALC-1001",
                    studentId = "STD-2024-001",
                    studentName = "Muhammad Ali Khan",
                    bedId = "JH-101-B1",
                    bedNumber = "Bed 1",
                    roomId = "JH-101",
                    roomNumber = "101",
                    buildingId = "BLD-A",
                    buildingName = "Jinnah Hall",
                    allocationDate = "2024-01-10",
                    expectedCheckoutDate = "2024-12-31",
                    monthlyRent = 18000.0,
                    status = "ACTIVE",
                    allocatedBy = "Maj. (R) Asim Javed"
                )
            )
            dao.insertAllocation(
                AllocationEntity(
                    id = "ALC-1002",
                    studentId = "STD-2024-002",
                    studentName = "Hamza Tariq",
                    bedId = "JH-101-B2",
                    bedNumber = "Bed 2",
                    roomId = "JH-101",
                    roomNumber = "101",
                    buildingId = "BLD-A",
                    buildingName = "Jinnah Hall",
                    allocationDate = "2024-02-01",
                    expectedCheckoutDate = "2024-12-31",
                    monthlyRent = 18000.0,
                    status = "ACTIVE",
                    allocatedBy = "Maj. (R) Asim Javed"
                )
            )

            // 7. Invoices & Payments
            dao.insertInvoice(
                InvoiceEntity(
                    id = "INV-2024-10-001",
                    invoiceNumber = "INV-1001",
                    studentId = "STD-2024-001",
                    studentName = "Muhammad Ali Khan",
                    billingMonth = "October 2024",
                    roomRent = 18000.0,
                    messCharges = 9000.0,
                    utilityCharges = 3500.0,
                    otherCharges = 500.0,
                    discount = 0.0,
                    totalAmount = 31000.0,
                    amountPaid = 31000.0,
                    dueDate = "2024-10-10",
                    status = "PAID"
                )
            )
            dao.insertPayment(
                PaymentEntity(
                    id = "PAY-1001",
                    paymentReference = "RCPT-98124",
                    invoiceId = "INV-2024-10-001",
                    studentId = "STD-2024-001",
                    studentName = "Muhammad Ali Khan",
                    amount = 31000.0,
                    paymentDate = "2024-10-06",
                    paymentMethod = "BANK_TRANSFER",
                    recordedBy = "Sohail Qureshi",
                    notes = "Bank Alfalah Transfer #TXN908123"
                )
            )

            dao.insertInvoice(
                InvoiceEntity(
                    id = "INV-2024-10-002",
                    invoiceNumber = "INV-1002",
                    studentId = "STD-2024-002",
                    studentName = "Hamza Tariq",
                    billingMonth = "October 2024",
                    roomRent = 18000.0,
                    messCharges = 9000.0,
                    utilityCharges = 3500.0,
                    otherCharges = 0.0,
                    discount = 1000.0,
                    totalAmount = 29500.0,
                    amountPaid = 15000.0,
                    dueDate = "2024-10-10",
                    status = "PARTIALLY_PAID"
                )
            )
            dao.insertPayment(
                PaymentEntity(
                    id = "PAY-1002",
                    paymentReference = "RCPT-98125",
                    invoiceId = "INV-2024-10-002",
                    studentId = "STD-2024-002",
                    studentName = "Hamza Tariq",
                    amount = 15000.0,
                    paymentDate = "2024-10-08",
                    paymentMethod = "CASH",
                    recordedBy = "Sohail Qureshi",
                    notes = "First installment paid in cash"
                )
            )

            dao.insertInvoice(
                InvoiceEntity(
                    id = "INV-2024-10-003",
                    invoiceNumber = "INV-1003",
                    studentId = "STD-2024-003",
                    studentName = "Bilal Ahmed",
                    billingMonth = "October 2024",
                    roomRent = 18000.0,
                    messCharges = 9000.0,
                    utilityCharges = 3500.0,
                    otherCharges = 0.0,
                    discount = 0.0,
                    totalAmount = 30500.0,
                    amountPaid = 0.0,
                    dueDate = "2024-10-10",
                    status = "OVERDUE"
                )
            )

            // 8. Complaints
            dao.insertComplaint(
                ComplaintEntity(
                    id = "CMP-1001",
                    ticketNumber = "TKT-1001",
                    studentId = "STD-2024-001",
                    studentName = "Muhammad Ali Khan",
                    buildingName = "Jinnah Hall",
                    roomNumber = "101",
                    bedNumber = "Bed 1",
                    category = "INTERNET",
                    title = "Wi-Fi Access Point Frequent Disconnection",
                    description = "The 2nd floor router signal is dropping packets during evening study hours (8 PM - 11 PM).",
                    priority = "HIGH",
                    status = "IN_PROGRESS",
                    assignedStaff = "IT Support - Asif",
                    resolutionNotes = "Replacement Cisco AP requested from central store."
                )
            )
            dao.insertComplaint(
                ComplaintEntity(
                    id = "CMP-1002",
                    ticketNumber = "TKT-1002",
                    studentId = "STD-2024-002",
                    studentName = "Hamza Tariq",
                    buildingName = "Jinnah Hall",
                    roomNumber = "101",
                    bedNumber = "Bed 2",
                    category = "PLUMBING",
                    title = "Bathroom Sink Tap Leaking",
                    description = "Continuous water dripping in attached bathroom sink causing water wastage.",
                    priority = "MEDIUM",
                    status = "RESOLVED",
                    assignedStaff = "Plumber - Tariq",
                    resolutionNotes = "Rubber washer replaced and pipe tightened.",
                    resolvedAt = System.currentTimeMillis() - 86400000L
                )
            )
            dao.insertComplaint(
                ComplaintEntity(
                    id = "CMP-1003",
                    ticketNumber = "TKT-1003",
                    studentId = "STD-2024-004",
                    studentName = "Usman Farooq",
                    buildingName = "Jinnah Hall",
                    roomNumber = "201",
                    bedNumber = "Bed 1",
                    category = "ELECTRICITY",
                    title = "Ceiling Fan Regulator Not Working",
                    description = "Fan stuck at maximum speed, switch sparking occasionally.",
                    priority = "URGENT",
                    status = "PENDING",
                    assignedStaff = null,
                    resolutionNotes = ""
                )
            )

            // 9. Leave Requests
            dao.insertLeaveRequest(
                LeaveRequestEntity(
                    id = "LEV-1001",
                    studentId = "STD-2024-001",
                    studentName = "Muhammad Ali Khan",
                    roomNumber = "101",
                    startDate = "2024-10-27 09:00",
                    endDate = "2024-10-29 18:00",
                    destination = "Lahore (Hometown)",
                    reason = "Family wedding ceremony of cousin.",
                    emergencyContact = "+92 301 2233445 (Father)",
                    status = "APPROVED",
                    reviewNotes = "Granted by Warden. Gate pass issued.",
                    reviewedBy = "Maj. (R) Asim Javed",
                    reviewedAt = System.currentTimeMillis() - 43200000L
                )
            )
            dao.insertLeaveRequest(
                LeaveRequestEntity(
                    id = "LEV-1002",
                    studentId = "STD-2024-003",
                    studentName = "Bilal Ahmed",
                    roomNumber = "102",
                    startDate = "2024-10-30 14:00",
                    endDate = "2024-11-02 20:00",
                    destination = "Islamabad",
                    reason = "Attending National Business Case Competition.",
                    emergencyContact = "+92 303 4455667",
                    status = "PENDING"
                )
            )

            // 10. Visitors
            dao.insertVisitor(
                VisitorEntity(
                    id = "VIS-1001",
                    visitorName = "Tariq Khan",
                    contactNumber = "+92 301 2233445",
                    cnicOrId = "35201-1234567-1",
                    studentId = "STD-2024-001",
                    studentName = "Muhammad Ali Khan",
                    relationship = "Father",
                    purpose = "Delivering winter clothes and laptop",
                    entryTime = "2024-10-26 14:15",
                    exitTime = null,
                    status = "CHECKED_IN",
                    recordedBy = "Guard - Ghulam Nabi"
                )
            )
            dao.insertVisitor(
                VisitorEntity(
                    id = "VIS-1002",
                    visitorName = "Zubair Hashmi",
                    contactNumber = "+92 322 7766554",
                    cnicOrId = "37405-9988771-3",
                    studentId = "STD-2024-002",
                    studentName = "Hamza Tariq",
                    relationship = "Uncle",
                    purpose = "Meeting student",
                    entryTime = "2024-10-25 16:00",
                    exitTime = "2024-10-25 17:30",
                    status = "CHECKED_OUT",
                    recordedBy = "Guard - Ghulam Nabi"
                )
            )

            // 11. Mess Menus (Weekly)
            val weekDays = listOf(
                Triple("MONDAY", "Halwa Puri, Channay, Chai", "Daal Mash, Seasonal Salad, Roti"),
                Triple("TUESDAY", "Fried Eggs, Butter Toast, Tea/Coffee", "Chicken Qorma, Plain Rice, Raita"),
                Triple("WEDNESDAY", "Aloo Paratha, Dahi Yogurt, Chai", "Kari Pakora, Zeera Rice, Salad"),
                Triple("THURSDAY", "Omelette, Bread, Fresh Milk", "Mix Vegetables Sabzi, Roti, Kheer"),
                Triple("FRIDAY", "Puri Chana, Sweet Lassi, Chai", "Special Chicken Biryani, Raita, Shami Kabab"),
                Triple("SATURDAY", "French Toast, Boiled Eggs, Tea", "Daal Chawal, Shami Kabab, Pickle"),
                Triple("SUNDAY", "Special Nihari, Naan, Kashmiri Chai", "Mutton Pulao, Mint Raita, Custard")
            )
            weekDays.forEach { (day, b, l) ->
                dao.insertMessMenu(
                    MessMenuEntity(
                        id = "MENU-$day",
                        dayOfWeek = day,
                        breakfast = b,
                        lunch = l,
                        dinner = if (day == "FRIDAY") "Chicken Karahi, Fresh Roti, Firni" else "Chicken Handi, Naan, Green Tea",
                        specialNotes = if (day == "FRIDAY" || day == "SUNDAY") "Special Feast Menu" else "Standard Balanced Diet"
                    )
                )
            }

            // Mess Expenses
            dao.insertMessExpense(
                MessExpenseEntity(
                    id = "EXP-101",
                    expenseDate = "2024-10-24",
                    itemDescription = "Fresh Milk & Dairy Supplies (80 Liters)",
                    category = "DAIRY",
                    quantity = "80 Liters",
                    amount = 16800.0,
                    recordedBy = "Mess Manager - Akram"
                )
            )
            dao.insertMessExpense(
                MessExpenseEntity(
                    id = "EXP-102",
                    expenseDate = "2024-10-25",
                    itemDescription = "Chicken Broiler & Eggs Wholesale Supply",
                    category = "MEAT",
                    quantity = "65 Kg Chicken + 10 Trays Eggs",
                    amount = 42500.0,
                    recordedBy = "Mess Manager - Akram"
                )
            )
            dao.insertMessExpense(
                MessExpenseEntity(
                    id = "EXP-103",
                    expenseDate = "2024-10-25",
                    itemDescription = "Rice Basmati, Daal Mash, Spices, Ghee",
                    category = "GROCERIES",
                    quantity = "150 Kg Groceries",
                    amount = 58000.0,
                    recordedBy = "Mess Manager - Akram"
                )
            )

            // Mess Feedback
            dao.insertMessFeedback(
                MessFeedbackEntity(
                    id = "MFB-101",
                    studentId = "STD-2024-001",
                    studentName = "Muhammad Ali Khan",
                    mealType = "DINNER",
                    rating = 5,
                    comments = "Friday Biryani was top notch! Excellent spices and tender meat.",
                    feedbackDate = "2024-10-25"
                )
            )
            dao.insertMessFeedback(
                MessFeedbackEntity(
                    id = "MFB-102",
                    studentId = "STD-2024-002",
                    studentName = "Hamza Tariq",
                    mealType = "BREAKFAST",
                    rating = 4,
                    comments = "Halwa Puri was delicious, please serve tea earlier at 7:30 AM.",
                    feedbackDate = "2024-10-26"
                )
            )

            // 12. Notices
            dao.insertNotice(
                NoticeEntity(
                    id = "NOT-101",
                    title = "Hostel Fee Clearance Notice for October 2024",
                    body = "All residents are reminded that the monthly fee due date is the 10th of each month. Please submit dues before the 15th to avoid late fee surcharge (Rs. 500).",
                    category = "FEE_REMINDER",
                    priority = "HIGH",
                    author = "Accounts Office",
                    targetAudience = "ALL_STUDENTS",
                    publishedDate = "2024-10-01"
                )
            )
            dao.insertNotice(
                NoticeEntity(
                    id = "NOT-102",
                    title = "High-Speed Wi-Fi Upgrade in Jinnah & Iqbal Halls",
                    body = "Fiber optic bandwidth is being doubled this weekend. There will be intermittent connectivity between 2 AM to 5 AM on Sunday.",
                    category = "MAINTENANCE",
                    priority = "NORMAL",
                    author = "Hostel IT Cell",
                    targetAudience = "ALL_STUDENTS",
                    publishedDate = "2024-10-24"
                )
            )
            dao.insertNotice(
                NoticeEntity(
                    id = "NOT-103",
                    title = "Night Curfew and Biometric In-Time Compliance",
                    body = "Hostel main gates close at 10:00 PM sharp. Students arriving later must have pre-approved emergency leave or guardian consent letter.",
                    category = "RULES",
                    priority = "URGENT",
                    author = "Chief Warden",
                    targetAudience = "ALL_STUDENTS",
                    publishedDate = "2024-10-20"
                )
            )

            // 13. Notifications
            dao.insertNotification(
                NotificationEntity(
                    id = "NTF-101",
                    recipientRole = "ALL",
                    title = "Welcome to HostelHub",
                    message = "Smart Living, Simple Management. Your portal is ready.",
                    category = "GENERAL"
                )
            )
            dao.insertNotification(
                NotificationEntity(
                    id = "NTF-102",
                    recipientRole = "STUDENT",
                    studentIdTarget = "STD-2024-001",
                    title = "Leave Request Approved",
                    message = "Your leave request for Lahore has been approved by Warden.",
                    category = "LEAVE"
                )
            )

            // 14. Audit Logs
            dao.insertAuditLog(
                AuditLogEntity(
                    id = "LOG-1001",
                    action = "INITIAL_SETUP",
                    entityType = "SYSTEM",
                    entityId = "SYS-001",
                    details = "HostelHub Smart Management System initialized with Jinnah, Iqbal, and Fatima Halls.",
                    performedBy = "System Administrator"
                )
            )
            dao.insertAuditLog(
                AuditLogEntity(
                    id = "LOG-1002",
                    action = "ALLOCATE_BED",
                    entityType = "ALLOCATION",
                    entityId = "ALC-1001",
                    details = "Bed JH-101-B1 allocated to student Muhammad Ali Khan (STD-2024-001)",
                    performedBy = "Maj. (R) Asim Javed"
                )
            )
            dao.insertAuditLog(
                AuditLogEntity(
                    id = "LOG-1003",
                    action = "RECORD_PAYMENT",
                    entityType = "PAYMENT",
                    entityId = "PAY-1001",
                    details = "Payment of Rs. 31,000 received for Invoice INV-1001 via Bank Transfer",
                    performedBy = "Sohail Qureshi"
                )
            )
        }
    }
}
