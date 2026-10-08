package com.freyr.app

import android.app.Application
import com.freyr.app.data.local.FreyrDatabase
import com.freyr.app.data.repository.FreyrRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class FreyrApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { FreyrDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { FreyrRepository(database, this) }
}
