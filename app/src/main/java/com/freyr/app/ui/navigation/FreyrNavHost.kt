package com.freyr.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.freyr.app.ui.screens.auth.LoginScreen
import com.freyr.app.ui.screens.auth.RegisterScreen
import com.freyr.app.ui.screens.dashboard.DashboardScreen
import com.freyr.app.ui.screens.debt.AddDebtScreen
import com.freyr.app.ui.screens.debt.AddPaymentScreen
import com.freyr.app.ui.screens.debt.DebtDetailScreen
import com.freyr.app.ui.screens.debt.PersonalFinancesScreen
import com.freyr.app.ui.screens.group.AcceptInvitationsScreen
import com.freyr.app.ui.screens.group.CreateFamilyGroupScreen
import com.freyr.app.ui.screens.group.FamilyGroupDetailScreen
import com.freyr.app.ui.screens.group.FamilyGroupsScreen
import com.freyr.app.ui.screens.profile.EditProfileScreen
import com.freyr.app.ui.viewmodel.FreyrViewModel

@Composable
fun FreyrNavHost(
    navController: NavHostController,
    viewModel: FreyrViewModel
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val startDestination = if (currentUser != null) Screen.Dashboard.route else Screen.Login.route

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = viewModel,
                onRegisterSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToEditProfile = { navController.navigate(Screen.EditProfile.route) },
                onNavigateToAddDebt = { navController.navigate(Screen.AddDebt.createRoute()) },
                onNavigateToCreateFamilyGroup = { navController.navigate(Screen.CreateFamilyGroup.route) },
                onNavigateToAcceptInvitations = { navController.navigate(Screen.AcceptInvitations.route) },
                onNavigateToPersonalFinances = { navController.navigate(Screen.PersonalFinances.route) },
                onNavigateToFamilyGroups = { navController.navigate(Screen.FamilyGroups.route) },
                onNavigateToDebtDetail = { debtId -> navController.navigate(Screen.DebtDetail.createRoute(debtId)) },
                onNavigateToAddPayment = { debtId -> navController.navigate(Screen.AddPayment.createRoute(debtId)) }
            )
        }

        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.AddDebt.route,
            arguments = listOf(
                navArgument("groupId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")
            AddDebtScreen(
                viewModel = viewModel,
                initialGroupId = groupId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.PersonalFinances.route) {
            PersonalFinancesScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddDebt = { navController.navigate(Screen.AddDebt.createRoute()) },
                onNavigateToDebtDetail = { debtId -> navController.navigate(Screen.DebtDetail.createRoute(debtId)) },
                onNavigateToAddPayment = { debtId -> navController.navigate(Screen.AddPayment.createRoute(debtId)) }
            )
        }

        composable(Screen.FamilyGroups.route) {
            FamilyGroupsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCreateGroup = { navController.navigate(Screen.CreateFamilyGroup.route) },
                onNavigateToGroupDetail = { groupId -> navController.navigate(Screen.FamilyGroupDetail.createRoute(groupId)) }
            )
        }

        composable(Screen.CreateFamilyGroup.route) {
            CreateFamilyGroupScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.FamilyGroupDetail.route,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            FamilyGroupDetailScreen(
                groupId = groupId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddDebt = { gId -> navController.navigate(Screen.AddDebt.createRoute(gId)) },
                onNavigateToDebtDetail = { debtId -> navController.navigate(Screen.DebtDetail.createRoute(debtId)) },
                onNavigateToAddPayment = { debtId -> navController.navigate(Screen.AddPayment.createRoute(debtId)) }
            )
        }

        composable(Screen.AcceptInvitations.route) {
            AcceptInvitationsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.DebtDetail.route,
            arguments = listOf(navArgument("debtId") { type = NavType.StringType })
        ) { backStackEntry ->
            val debtId = backStackEntry.arguments?.getString("debtId") ?: ""
            DebtDetailScreen(
                debtId = debtId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddPayment = { dId -> navController.navigate(Screen.AddPayment.createRoute(dId)) }
            )
        }

        composable(
            route = Screen.AddPayment.route,
            arguments = listOf(navArgument("debtId") { type = NavType.StringType })
        ) { backStackEntry ->
            val debtId = backStackEntry.arguments?.getString("debtId") ?: ""
            AddPaymentScreen(
                debtId = debtId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
