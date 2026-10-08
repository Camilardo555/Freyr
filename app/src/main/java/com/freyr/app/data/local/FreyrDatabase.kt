package com.freyr.app.data.local

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.freyr.app.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        User::class,
        FamilyGroup::class,
        Debt::class,
        Payment::class,
        Invitation::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FreyrDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun familyGroupDao(): FamilyGroupDao
    abstract fun debtDao(): DebtDao
    abstract fun paymentDao(): PaymentDao
    abstract fun invitationDao(): InvitationDao

    companion object {
        @Volatile
        private var INSTANCE: FreyrDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): FreyrDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FreyrDatabase::class.java,
                    "freyr_database"
                )
                    .addCallback(FreyrDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class FreyrDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }

            suspend fun populateInitialData(db: FreyrDatabase) {
                val userDao = db.userDao()
                val groupDao = db.familyGroupDao()
                val debtDao = db.debtDao()
                val paymentDao = db.paymentDao()
                val invitationDao = db.invitationDao()

                // Initial Seed Users
                val juan = User(
                    id = "user-juan",
                    fullName = "Juan Pérez",
                    username = "juan",
                    email = "juan@example.com",
                    password = "password123",
                    avatarColor = "#69042A",
                    createdAt = "2026-01-15T10:00:00Z"
                )
                val maria = User(
                    id = "user-maria",
                    fullName = "María González",
                    username = "maria",
                    email = "maria@example.com",
                    password = "password123",
                    avatarColor = "#9C27B0",
                    createdAt = "2026-01-16T11:00:00Z"
                )
                val carlos = User(
                    id = "user-carlos",
                    fullName = "Carlos Rodríguez",
                    username = "carlos",
                    email = "carlos@example.com",
                    password = "password123",
                    avatarColor = "#00897B",
                    createdAt = "2026-02-01T09:30:00Z"
                )
                userDao.insertUser(juan)
                userDao.insertUser(maria)
                userDao.insertUser(carlos)

                // Family Group
                val familyGroup = FamilyGroup(
                    id = "group-perez-gonzalez",
                    name = "Familia Pérez González",
                    creatorId = juan.id,
                    memberIds = listOf(juan.id, maria.id),
                    createdAt = "2026-01-20T14:00:00Z"
                )
                groupDao.insertGroup(familyGroup)

                // Debts (including prompt examples: Laptop & Internet Bill)
                val laptopDebt = Debt(
                    id = "debt-laptop",
                    name = "Laptop",
                    description = "Work and study laptop monthly finance",
                    amount = 3000000.0,
                    assignedUserId = juan.id,
                    familyGroupId = null, // Personal
                    status = "pending",
                    createdAt = "2026-02-10T12:00:00Z",
                    createdBy = juan.id
                )
                val internetDebt = Debt(
                    id = "debt-internet",
                    name = "Internet Bill",
                    description = "High-speed optical fiber internet monthly service",
                    amount = 120000.0,
                    assignedUserId = juan.id,
                    familyGroupId = familyGroup.id, // Family
                    status = "pending",
                    createdAt = "2026-02-15T08:30:00Z",
                    createdBy = juan.id
                )
                val groceriesDebt = Debt(
                    id = "debt-groceries",
                    name = "Bi-Weekly Groceries",
                    description = "Supermarket supplies and pantry items",
                    amount = 350000.0,
                    assignedUserId = null, // Entire family
                    familyGroupId = familyGroup.id,
                    status = "pending",
                    createdAt = "2026-02-18T16:00:00Z",
                    createdBy = maria.id
                )
                val gymDebt = Debt(
                    id = "debt-gym",
                    name = "Gym Annual Membership",
                    description = "Fitness center access paid in full",
                    amount = 360000.0,
                    assignedUserId = juan.id,
                    familyGroupId = null,
                    status = "paid",
                    createdAt = "2026-01-10T10:00:00Z",
                    createdBy = juan.id
                )
                debtDao.insertDebt(laptopDebt)
                debtDao.insertDebt(internetDebt)
                debtDao.insertDebt(groceriesDebt)
                debtDao.insertDebt(gymDebt)

                // Payments
                paymentDao.insertPayment(
                    Payment(
                        id = "pay-laptop-1",
                        debtId = laptopDebt.id,
                        userId = juan.id,
                        amount = 1000000.0,
                        date = "2026-02-15",
                        description = "Initial deposit & first installment",
                        createdAt = "2026-02-15T14:00:00Z"
                    )
                )
                paymentDao.insertPayment(
                    Payment(
                        id = "pay-internet-1",
                        debtId = internetDebt.id,
                        userId = juan.id,
                        amount = 60000.0,
                        date = "2026-02-20",
                        description = "First half payment",
                        createdAt = "2026-02-20T09:00:00Z"
                    )
                )
                paymentDao.insertPayment(
                    Payment(
                        id = "pay-groceries-1",
                        debtId = groceriesDebt.id,
                        userId = maria.id,
                        amount = 150000.0,
                        date = "2026-02-22",
                        description = "Fresh produce and pantry items",
                        createdAt = "2026-02-22T17:00:00Z"
                    )
                )
                paymentDao.insertPayment(
                    Payment(
                        id = "pay-gym-1",
                        debtId = gymDebt.id,
                        userId = juan.id,
                        amount = 360000.0,
                        date = "2026-01-12",
                        description = "Full upfront payment",
                        createdAt = "2026-01-12T11:00:00Z"
                    )
                )

                // Pending Invitation for Carlos
                invitationDao.insertInvitation(
                    Invitation(
                        id = "inv-carlos-1",
                        familyGroupId = familyGroup.id,
                        invitedUserId = carlos.id,
                        invitedByUserId = juan.id,
                        status = "pending",
                        createdAt = "2026-02-24T10:00:00Z"
                    )
                )
            }
        }
    }
}
