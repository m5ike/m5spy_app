package com.myspy.android.data.local.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * SMS Entity for Room Database
 * @author Michael KOJDL
 * @version 1.0.0
 */
@Entity(tableName = "sms_messages")
data class SmsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // INCOMING, OUTGOING, DRAFT
    val address: String,
    val body: String,
    val timestamp: Long,
    val threadId: Int?,
    val synced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
