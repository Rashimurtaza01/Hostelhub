package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole {
    ADMIN,
    WARDEN,
    ACCOUNTANT,
    STUDENT
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String, // e.g. "usr-admin-1", "usr-std-101"
    val email: String,
    val passwordHash: String,
    val fullName: String,
    val role: UserRole,
    val phone: String,
    val studentIdRef: String? = null, // Linked student ID if role == STUDENT
    val staffIdRef: String? = null,   // Linked staff ID if staff
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "student_profiles")
data class StudentProfileEntity(
    @PrimaryKey val id: String, // Unique Student ID e.g. "STD-2024-001"
    val fullName: String,
    val email: String,
    val phone: String,
    val gender: String, // "Male", "Female"
    val department: String, // "Computer Science", "Electrical Eng", etc.
    val degreeProgram: String, // "BS Software Eng", "BBA", etc.
    val semester: Int,
    val guardianName: String,
    val guardianPhone: String,
    val admissionDate: String, // "2024-01-15"
    val status: String = "ACTIVE", // "ACTIVE", "INACTIVE", "CHECKED_OUT"
    val emergencyContact: String,
    val currentRoomNumber: String? = null,
    val currentBedNumber: String? = null,
    val currentBuildingName: String? = null
)

@Entity(tableName = "staff_profiles")
data class StaffProfileEntity(
    @PrimaryKey val id: String, // e.g. "STF-101"
    val fullName: String,
    val email: String,
    val phone: String,
    val role: UserRole, // WARDEN, ACCOUNTANT, ADMIN
    val assignedBuildingId: String? = null,
    val status: String = "ACTIVE",
    val joiningDate: String
)

@Entity(tableName = "buildings")
data class BuildingEntity(
    @PrimaryKey val id: String, // e.g. "BLD-A"
    val name: String, // "Jinnah Hall"
    val code: String, // "JH-A"
    val address: String, // "North Campus, University Enclave"
    val floorsCount: Int,
    val status: String = "ACTIVE" // "ACTIVE", "MAINTENANCE"
)

@Entity(tableName = "rooms")
data class RoomEntity(
    @PrimaryKey val id: String, // e.g. "JH-101"
    val roomNumber: String, // "101"
    val buildingId: String,
    val floor: Int,
    val roomType: String, // "SINGLE", "DOUBLE", "TRIPLE", "QUAD"
    val capacity: Int,
    val monthlyRent: Double, // in PKR (e.g. 18000.0)
    val description: String,
    val maintenanceStatus: String = "OPERATIONAL" // "OPERATIONAL", "UNDER_MAINTENANCE"
)

@Entity(tableName = "beds")
data class BedEntity(
    @PrimaryKey val id: String, // e.g. "JH-101-B1"
    val bedNumber: String, // "Bed 1"
    val roomId: String,
    val isOccupied: Boolean = false,
    val currentStudentId: String? = null,
    val currentStudentName: String? = null
)

@Entity(tableName = "allocations")
data class AllocationEntity(
    @PrimaryKey val id: String, // e.g. "ALC-1001"
    val studentId: String,
    val studentName: String,
    val bedId: String,
    val bedNumber: String,
    val roomId: String,
    val roomNumber: String,
    val buildingId: String,
    val buildingName: String,
    val allocationDate: String, // "YYYY-MM-DD"
    val expectedCheckoutDate: String,
    val actualCheckoutDate: String? = null,
    val monthlyRent: Double,
    val status: String = "ACTIVE", // "ACTIVE", "TRANSFERRED", "CHECKED_OUT"
    val allocatedBy: String // Admin or Warden name
)
