package com.freyr.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.freyr.app.data.model.*
import com.freyr.app.data.repository.FreyrRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FreyrViewModel(
    val repository: FreyrRepository
) : ViewModel() {

    val currentUser = repository.currentUser.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    val allUsers = repository.allUsers.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val allGroups = repository.allGroups.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val allDebts = repository.allDebts.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val allPayments = repository.allPayments.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val allInvitations = repository.allInvitations.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val financialSummary: StateFlow<FinancialSummary> = combine(
        currentUser,
        allDebts,
        allPayments,
        allGroups
    ) { user, debts, payments, groups ->
        if (user == null) {
            return@combine FinancialSummary()
        }

        val userGroupIds = groups.filter { it.memberIds.contains(user.id) }.map { it.id }

        // All debts relevant to user
        val relevantDebts = debts.filter { debt ->
            if (debt.familyGroupId == null) {
                debt.assignedUserId == user.id || debt.createdBy == user.id
            } else {
                userGroupIds.contains(debt.familyGroupId)
            }
        }

        var pendingCount = 0
        var paidCount = 0
        var totalPending = 0.0
        var totalPaid = 0.0

        relevantDebts.forEach { debt ->
            val debtPayments = payments.filter { it.debtId == debt.id }
            val paid = debtPayments.sumOf { it.amount }
            val remaining = (debt.amount - paid).coerceAtLeast(0.0)

            if (remaining == 0.0 || debt.status == "paid") {
                paidCount++
                totalPaid += debt.amount
            } else {
                pendingCount++
                totalPending += remaining
                totalPaid += paid
            }
        }

        val personalPaymentsTotal = payments
            .filter { it.userId == user.id }
            .sumOf { it.amount }

        val familyDebtsPending = debts
            .filter { it.familyGroupId != null && userGroupIds.contains(it.familyGroupId) }
            .sumOf { debt ->
                val paid = payments.filter { it.debtId == debt.id }.sumOf { it.amount }
                (debt.amount - paid).coerceAtLeast(0.0)
            }

        FinancialSummary(
            pendingDebtsCount = pendingCount,
            paidDebtsCount = paidCount,
            personalPaymentsTotal = personalPaymentsTotal,
            familyDebtsPendingTotal = familyDebtsPending,
            totalPendingAmount = totalPending,
            totalPaidAmount = totalPaid
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        FinancialSummary()
    )

    fun getPaidForDebt(debtId: String): Double {
        return allPayments.value.filter { it.debtId == debtId }.sumOf { it.amount }
    }

    fun getRemainingForDebt(debtId: String): Double {
        val debt = allDebts.value.find { it.id == debtId } ?: return 0.0
        val paid = getPaidForDebt(debtId)
        return (debt.amount - paid).coerceAtLeast(0.0)
    }

    fun getUserName(userId: String?): String {
        if (userId == null) return "Entire Family"
        return allUsers.value.find { it.id == userId }?.fullName ?: "Unknown User"
    }

    fun logout() {
        repository.setLoggedInUser(null)
    }

    fun switchUser(userId: String) {
        repository.setLoggedInUser(userId)
    }

    suspend fun login(id: String, pass: String) = repository.login(id, pass)
    suspend fun register(fn: String, un: String, em: String, pw: String) = repository.register(fn, un, em, pw)
    suspend fun updateProfile(fn: String, un: String, em: String, pw: String?) = repository.updateProfile(fn, un, em, pw)

    suspend fun addDebt(name: String, desc: String?, amt: Double, assigned: String?, group: String?) =
        repository.addDebt(name, desc, amt, assigned, group)

    suspend fun deleteDebt(debtId: String) = repository.deleteDebt(debtId)

    suspend fun addPayment(debtId: String, amt: Double, date: String, desc: String?) =
        repository.addPayment(debtId, amt, date, desc)

    suspend fun deletePayment(paymentId: String, debtId: String) =
        repository.deletePayment(paymentId, debtId)

    suspend fun createFamilyGroup(name: String) = repository.createFamilyGroup(name)
    suspend fun inviteUser(groupId: String, identifier: String) = repository.inviteUser(groupId, identifier)
    suspend fun respondToInvitation(inviteId: String, accept: Boolean) = repository.respondToInvitation(inviteId, accept)
}

class FreyrViewModelFactory(
    private val repository: FreyrRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FreyrViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FreyrViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
