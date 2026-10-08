package com.freyr.app.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Dashboard : Screen("dashboard")
    object EditProfile : Screen("edit_profile")
    object AddDebt : Screen("add_debt?groupId={groupId}") {
        fun createRoute(groupId: String? = null) = if (groupId != null) "add_debt?groupId=$groupId" else "add_debt"
    }
    object PersonalFinances : Screen("personal_finances")
    object FamilyGroups : Screen("family_groups")
    object CreateFamilyGroup : Screen("create_family_group")
    object AcceptInvitations : Screen("accept_invitations")
    object FamilyGroupDetail : Screen("family_group_detail/{groupId}") {
        fun createRoute(groupId: String) = "family_group_detail/$groupId"
    }
    object DebtDetail : Screen("debt_detail/{debtId}") {
        fun createRoute(debtId: String) = "debt_detail/$debtId"
    }
    object AddPayment : Screen("add_payment/{debtId}") {
        fun createRoute(debtId: String) = "add_payment/$debtId"
    }
}
