package com.freyr.app.data.local

import androidx.room.*
import com.freyr.app.data.model.*
import kotlinx.coroutines.flow.Flow

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return value?.joinToString(",") ?: ""
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrEmpty()) return emptyList()
        return value.split(",").filter { it.isNotBlank() }
    }
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): User?

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:identifier) OR LOWER(username) = LOWER(:identifier) LIMIT 1")
    suspend fun getUserByIdentifier(identifier: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)
}

@Dao
interface FamilyGroupDao {
    @Query("SELECT * FROM family_groups")
    fun getAllGroups(): Flow<List<FamilyGroup>>

    @Query("SELECT * FROM family_groups WHERE id = :id LIMIT 1")
    suspend fun getGroupById(id: String): FamilyGroup?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: FamilyGroup)

    @Update
    suspend fun updateGroup(group: FamilyGroup)
}

@Dao
interface DebtDao {
    @Query("SELECT * FROM debts ORDER BY createdAt DESC")
    fun getAllDebts(): Flow<List<Debt>>

    @Query("SELECT * FROM debts WHERE id = :id LIMIT 1")
    suspend fun getDebtById(id: String): Debt?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: Debt)

    @Update
    suspend fun updateDebt(debt: Debt)

    @Delete
    suspend fun deleteDebt(debt: Debt)

    @Query("DELETE FROM debts WHERE id = :id")
    suspend fun deleteDebtById(id: String)
}

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY date DESC, createdAt DESC")
    fun getAllPayments(): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE debtId = :debtId ORDER BY date DESC")
    fun getPaymentsForDebt(debtId: String): Flow<List<Payment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment)

    @Delete
    suspend fun deletePayment(payment: Payment)

    @Query("DELETE FROM payments WHERE id = :id")
    suspend fun deletePaymentById(id: String)

    @Query("DELETE FROM payments WHERE debtId = :debtId")
    suspend fun deletePaymentsByDebtId(debtId: String)
}

@Dao
interface InvitationDao {
    @Query("SELECT * FROM invitations ORDER BY createdAt DESC")
    fun getAllInvitations(): Flow<List<Invitation>>

    @Query("SELECT * FROM invitations WHERE invitedUserId = :userId AND status = 'pending'")
    fun getPendingInvitationsForUser(userId: String): Flow<List<Invitation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvitation(invitation: Invitation)

    @Update
    suspend fun updateInvitation(invitation: Invitation)
}
