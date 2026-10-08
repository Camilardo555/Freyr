package com.freyr.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.freyr.app.data.local.FreyrDatabase
import com.freyr.app.data.model.*
import kotlinx.coroutines.flow.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class FreyrRepository(
    private val database: FreyrDatabase,
    private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("freyr_prefs", Context.MODE_PRIVATE)

    private val _currentUserId = MutableStateFlow(
        prefs.getString("current_user_id", "user-juan")
    )
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    val allUsers: Flow<List<User>> = database.userDao().getAllUsers()
    val allGroups: Flow<List<FamilyGroup>> = database.familyGroupDao().getAllGroups()
    val allDebts: Flow<List<Debt>> = database.debtDao().getAllDebts()
    val allPayments: Flow<List<Payment>> = database.paymentDao().getAllPayments()
    val allInvitations: Flow<List<Invitation>> = database.invitationDao().getAllInvitations()

    val currentUser: Flow<User?> = combine(allUsers, currentUserId) { users, id ->
        users.find { it.id == id }
    }

    fun setLoggedInUser(userId: String?) {
        _currentUserId.value = userId
        prefs.edit().apply {
            if (userId != null) {
                putString("current_user_id", userId)
            } else {
                remove("current_user_id")
            }
            apply()
        }
    }

    suspend fun login(identifier: String, password: String):Result<User> {
        val clean = identifier.trim().lowercase(Locale.ROOT)
        val user = database.userDao().getUserByIdentifier(clean)
            ?: return Result.failure(Exception("User not found with identifier: $identifier"))

        if (user.password.isNotEmpty() && user.password != password) {
            return Result.failure(Exception("Incorrect password"))
        }

        setLoggedInUser(user.id)
        return Result.success(user)
    }

    suspend fun register(
        fullName: String,
        username: String,
        email: String,
        password: String
    ): Result<User> {
        val trimmedUsername = username.trim().lowercase(Locale.ROOT)
        val trimmedEmail = email.trim().lowercase(Locale.ROOT)

        if (fullName.isBlank()) return Result.failure(Exception("Full name is required"))
        if (trimmedUsername.isBlank()) return Result.failure(Exception("Username is required"))
        if (trimmedEmail.isBlank()) return Result.failure(Exception("Email is required"))
        if (password.length < 6) return Result.failure(Exception("Password must be at least 6 characters"))

        val existingUser = database.userDao().getUserByIdentifier(trimmedUsername)
            ?: database.userDao().getUserByIdentifier(trimmedEmail)

        if (existingUser != null) {
            return Result.failure(Exception("Username or email already in use"))
        }

        val colors = listOf("#69042A", "#00897B", "#1E88E5", "#8E24AA", "#D81B60", "#3949AB")
        val randomColor = colors.random()

        val newUser = User(
            id = "user-${System.currentTimeMillis()}",
            fullName = fullName.trim(),
            username = trimmedUsername,
            email = trimmedEmail,
            password = password,
            avatarColor = randomColor,
            createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        )

        database.userDao().insertUser(newUser)
        setLoggedInUser(newUser.id)
        return Result.success(newUser)
    }

    suspend fun updateProfile(
        fullName: String,
        username: String,
        email: String,
        password: String?
    ): Result<Unit> {
        val current = currentUser.first() ?: return Result.failure(Exception("Not logged in"))
        val trimmedUsername = username.trim().lowercase(Locale.ROOT)
        val trimmedEmail = email.trim().lowercase(Locale.ROOT)

        if (fullName.isBlank()) return Result.failure(Exception("Full name cannot be empty"))
        if (trimmedUsername.isBlank()) return Result.failure(Exception("Username cannot be empty"))
        if (trimmedEmail.isBlank()) return Result.failure(Exception("Email cannot be empty"))

        val users = allUsers.first()
        val usernameTaken = users.any { it.id != current.id && it.username.lowercase(Locale.ROOT) == trimmedUsername }
        if (usernameTaken) return Result.failure(Exception("Username already in use"))

        val emailTaken = users.any { it.id != current.id && it.email.lowercase(Locale.ROOT) == trimmedEmail }
        if (emailTaken) return Result.failure(Exception("Email already in use"))

        val updated = current.copy(
            fullName = fullName.trim(),
            username = trimmedUsername,
            email = trimmedEmail,
            password = if (!password.isNullOrBlank()) password else current.password
        )
        database.userDao().updateUser(updated)
        return Result.success(Unit)
    }

    suspend fun addDebt(
        name: String,
        description: String?,
        amount: Double,
        assignedUserId: String?,
        familyGroupId: String?
    ): Result<Debt> {
        val user = currentUser.first() ?: return Result.failure(Exception("Not logged in"))

        if (name.isBlank()) return Result.failure(Exception("Debt name cannot be empty"))
        if (amount <= 0.0) return Result.failure(Exception("Amount must be a positive number"))

        val newDebt = Debt(
            id = "debt-${System.currentTimeMillis()}",
            name = name.trim(),
            description = description?.trim()?.ifBlank { null },
            amount = amount,
            assignedUserId = assignedUserId,
            familyGroupId = familyGroupId,
            status = "pending",
            createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date()),
            createdBy = user.id
        )

        database.debtDao().insertDebt(newDebt)
        return Result.success(newDebt)
    }

    suspend fun deleteDebt(debtId: String) {
        database.debtDao().deleteDebtById(debtId)
        database.paymentDao().deletePaymentsByDebtId(debtId)
    }

    suspend fun addPayment(
        debtId: String,
        amount: Double,
        date: String,
        description: String?
    ): Result<Payment> {
        val user = currentUser.first() ?: return Result.failure(Exception("Not logged in"))
        if (amount <= 0.0) return Result.failure(Exception("Payment amount must be greater than 0"))

        val debt = database.debtDao().getDebtById(debtId)
            ?: return Result.failure(Exception("Debt not found"))

        val payments = database.paymentDao().getPaymentsForDebt(debtId).first()
        val currentPaid = payments.sumOf { it.amount }
        val newTotalPaid = currentPaid + amount

        val newPayment = Payment(
            id = "pay-${System.currentTimeMillis()}",
            debtId = debtId,
            userId = user.id,
            amount = amount,
            date = date,
            description = description?.trim()?.ifBlank { null },
            createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        )
        database.paymentDao().insertPayment(newPayment)

        if (newTotalPaid >= debt.amount) {
            database.debtDao().updateDebt(debt.copy(status = "paid"))
        }

        return Result.success(newPayment)
    }

    suspend fun deletePayment(paymentId: String, debtId: String) {
        database.paymentDao().deletePaymentById(paymentId)
        val debt = database.debtDao().getDebtById(debtId) ?: return
        val remainingPayments = database.paymentDao().getPaymentsForDebt(debtId).first()
        val totalPaid = remainingPayments.sumOf { it.amount }
        if (totalPaid < debt.amount) {
            database.debtDao().updateDebt(debt.copy(status = "pending"))
        }
    }

    suspend fun createFamilyGroup(name: String): Result<FamilyGroup> {
        val user = currentUser.first() ?: return Result.failure(Exception("Not logged in"))
        if (name.isBlank()) return Result.failure(Exception("Group name cannot be empty"))

        val newGroup = FamilyGroup(
            id = "group-${System.currentTimeMillis()}",
            name = name.trim(),
            creatorId = user.id,
            memberIds = listOf(user.id),
            createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        )
        database.familyGroupDao().insertGroup(newGroup)
        return Result.success(newGroup)
    }

    suspend fun inviteUser(groupId: String, identifier: String): Result<Unit> {
        val user = currentUser.first() ?: return Result.failure(Exception("Not logged in"))
        val group = database.familyGroupDao().getGroupById(groupId)
            ?: return Result.failure(Exception("Family group not found"))

        val targetUser = database.userDao().getUserByIdentifier(identifier.trim().lowercase(Locale.ROOT))
            ?: return Result.failure(Exception("No user found with username or email: $identifier"))

        if (group.memberIds.contains(targetUser.id)) {
            return Result.failure(Exception("${targetUser.fullName} is already a member of this group"))
        }

        val allInvites = allInvitations.first()
        val alreadyInvited = allInvites.any {
            it.familyGroupId == groupId && it.invitedUserId == targetUser.id && it.status == "pending"
        }
        if (alreadyInvited) {
            return Result.failure(Exception("An invitation is already pending for ${targetUser.fullName}"))
        }

        val invitation = Invitation(
            id = "inv-${System.currentTimeMillis()}",
            familyGroupId = groupId,
            invitedUserId = targetUser.id,
            invitedByUserId = user.id,
            status = "pending",
            createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        )
        database.invitationDao().insertInvitation(invitation)
        return Result.success(Unit)
    }

    suspend fun respondToInvitation(invitationId: String, accept: Boolean) {
        val invites = allInvitations.first()
        val invite = invites.find { it.id == invitationId } ?: return

        if (accept) {
            val group = database.familyGroupDao().getGroupById(invite.familyGroupId)
            if (group != null && !group.memberIds.contains(invite.invitedUserId)) {
                val updatedMembers = group.memberIds + invite.invitedUserId
                database.familyGroupDao().updateGroup(group.copy(memberIds = updatedMembers))
            }
        }

        database.invitationDao().updateInvitation(
            invite.copy(status = if (accept) "accepted" else "declined")
        )
    }

    companion object {
        fun formatCurrency(amount: Double): String {
            val format = NumberFormat.getCurrencyInstance(Locale.US)
            format.maximumFractionDigits = if (amount % 1.0 == 0.0) 0 else 2
            format.minimumFractionDigits = if (amount % 1.0 == 0.0) 0 else 2
            return format.format(amount)
        }
    }
}
