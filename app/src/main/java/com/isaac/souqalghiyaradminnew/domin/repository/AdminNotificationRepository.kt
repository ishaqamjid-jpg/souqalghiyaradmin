package com.isaac.souqalghiyaradminnew.domain.repository

import com.isaac.souqalghiyaradminnew.domain.model.admin_alarm
import kotlinx.coroutines.flow.Flow

interface AdminNotificationRepository {
    fun getAdminNotifications(): Flow<List<admin_alarm>>
    suspend fun markNotificationAsRead(alarmId: String): Result<Unit>
    suspend fun deleteNotification(alarmId: String): Result<Unit>
}
