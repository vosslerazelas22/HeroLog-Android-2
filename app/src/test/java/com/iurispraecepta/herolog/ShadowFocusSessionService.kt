package com.iurispraecepta.herolog

import android.app.Service
import android.content.Intent
import com.iurispraecepta.herolog.service.FocusSessionService
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements

@Implements(FocusSessionService::class)
class ShadowFocusSessionService {
    @Implementation
    protected fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return Service.START_NOT_STICKY
    }
}
