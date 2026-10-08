package com.freyr.app.data.local;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;
import com.freyr.app.data.model.*;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
    entities = {User.class, FamilyGroup.class, Debt.class, Payment.class, Invitation.class},
    version = 1,
    exportSchema = false
)
@TypeConverters({Converters.class})
public abstract class FreyrDatabase extends RoomDatabase {

    public abstract UserDao userDao();
    public abstract FamilyGroupDao familyGroupDao();
    public abstract DebtDao debtDao();
    public abstract PaymentDao paymentDao();
    public abstract InvitationDao invitationDao();

    private static volatile FreyrDatabase INSTANCE;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(4);

    public static FreyrDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (FreyrDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        FreyrDatabase.class,
                        "freyr_database"
                    )
                    .addCallback(sRoomDatabaseCallback)
                    .build();
                }
            }
        }
        return INSTANCE;
    }

    private static final RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            databaseWriteExecutor.execute(() -> {
                if (INSTANCE == null) return;
                UserDao userDao = INSTANCE.userDao();
                FamilyGroupDao groupDao = INSTANCE.familyGroupDao();
                DebtDao debtDao = INSTANCE.debtDao();
                PaymentDao paymentDao = INSTANCE.paymentDao();
                InvitationDao invitationDao = INSTANCE.invitationDao();

                // 1. Initial Users
                User juan = new User("user-juan", "Juan Pérez", "juan", "juan@example.com", "password123", "#69042A", "2026-01-15T10:00:00Z");
                User maria = new User("user-maria", "María González", "maria", "maria@example.com", "password123", "#9C27B0", "2026-01-16T11:00:00Z");
                User carlos = new User("user-carlos", "Carlos Rodríguez", "carlos", "carlos@example.com", "password123", "#00897B", "2026-02-01T09:30:00Z");
                userDao.insertUser(juan);
                userDao.insertUser(maria);
                userDao.insertUser(carlos);

                // 2. Family Group
                FamilyGroup familyGroup = new FamilyGroup(
                    "group-perez-gonzalez",
                    "Familia Pérez González",
                    juan.getId(),
                    Arrays.asList(juan.getId(), maria.getId()),
                    "2026-01-20T14:00:00Z"
                );
                groupDao.insertGroup(familyGroup);

                // 3. Debts (Examples from brief: Laptop: 3M, Internet: 120k)
                Debt laptopDebt = new Debt(
                    "debt-laptop",
                    "Laptop",
                    "Work and study laptop monthly finance",
                    3000000.0,
                    juan.getId(),
                    null, // Personal
                    "pending",
                    "2026-02-10T12:00:00Z",
                    juan.getId()
                );
                Debt internetDebt = new Debt(
                    "debt-internet",
                    "Internet Bill",
                    "High-speed fiber optic monthly service",
                    120000.0,
                    juan.getId(),
                    familyGroup.getId(), // Family debt assigned to Juan
                    "pending",
                    "2026-02-15T08:30:00Z",
                    juan.getId()
                );
                Debt groceriesDebt = new Debt(
                    "debt-groceries",
                    "Bi-Weekly Groceries",
                    "Pantry supplies and groceries",
                    350000.0,
                    null, // Entire family
                    familyGroup.getId(),
                    "pending",
                    "2026-02-18T16:00:00Z",
                    maria.getId()
                );
                Debt gymDebt = new Debt(
                    "debt-gym",
                    "Gym Annual Membership",
                    "Fitness center annual access paid off",
                    360000.0,
                    juan.getId(),
                    null,
                    "paid",
                    "2026-01-10T10:00:00Z",
                    juan.getId()
                );
                debtDao.insertDebt(laptopDebt);
                debtDao.insertDebt(internetDebt);
                debtDao.insertDebt(groceriesDebt);
                debtDao.insertDebt(gymDebt);

                // 4. Payments
                paymentDao.insertPayment(new Payment(
                    "pay-laptop-1", laptopDebt.getId(), juan.getId(), 1000000.0, "2026-02-15", "Initial deposit & 1st installment", "2026-02-15T14:00:00Z"
                ));
                paymentDao.insertPayment(new Payment(
                    "pay-internet-1", internetDebt.getId(), juan.getId(), 60000.0, "2026-02-20", "First half installment", "2026-02-20T09:00:00Z"
                ));
                paymentDao.insertPayment(new Payment(
                    "pay-groceries-1", groceriesDebt.getId(), maria.getId(), 150000.0, "2026-02-22", "Supermarket purchase", "2026-02-22T17:00:00Z"
                ));
                paymentDao.insertPayment(new Payment(
                    "pay-gym-1", gymDebt.getId(), juan.getId(), 360000.0, "2026-01-12", "Full payment", "2026-01-12T11:00:00Z"
                ));

                // 5. Invitation for Carlos
                invitationDao.insertInvitation(new Invitation(
                    "inv-carlos-1", familyGroup.getId(), carlos.getId(), juan.getId(), "pending", "2026-02-24T10:00:00Z"
                ));
            });
        }
    };
}
