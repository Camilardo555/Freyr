package com.freyr.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String,
    val fullName: String,
    val username: String,
    val email: String,
    val password: String = "password123",
    val avatarColor: String = "#69042A",
    val createdAt: String = ""
)

@Entity(tableName = "family_groups")
data class FamilyGroup(
    @PrimaryKey val id: String,
    val name: String,
    val creatorId: String,
    val memberIds: List<String>,
    val createdAt: String = ""
)

@Entity(tableName = "debts")
data class Debt(
    @PrimaryKey val id: String,
    val name: String,
    val description: String? = null,
    val amount: Double,
    val assignedUserId: String? = null, // null means whole family group or unassigned
    val familyGroupId: String? = null, // null means personal debt
    val status: String = "pending", // "pending" or "paid"
    val createdAt: String = "",
    val createdBy: String = ""
)

@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey val id: String,
    val debtId: String,
    val userId: String,
    val amount: Double,
    val date: String,
    val description: String? = null,
    val createdAt: String = ""
)

@Entity(tableName = "invitations")
data class Invitation(
    @PrimaryKey val id: String,
    val familyGroupId: String,
    val invitedUserId: String,
    val invitedByUserId: String,
    val status: String = "pending", // "pending", "accepted", "declined"
    val createdAt: String = ""
)

data class FinancialSummary(
    val pendingDebtsCount: Int = 0,
    val paidDebtsCount: Int = 0,
    val personalPaymentsTotal: Double = 0.0,
    val familyDebtsPendingTotal: Double = 0.0,
    val totalPendingAmount: Double = 0.0,
    val totalPaidAmount: Double = 0.0
)
