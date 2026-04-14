package com.tally.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tally.app.data.model.ChatSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "chat_sessions")

/**
 * 对话会话仓库 - 负责持久化存储对话历史
 */
class ChatSessionRepository(private val context: Context) {

    private val gson = Gson()
    private val sessionsKey = stringPreferencesKey("chat_sessions")

    /**
     * 获取所有会话的 Flow
     */
    val sessionsFlow: Flow<List<ChatSession>> = context.dataStore.data
        .map { preferences ->
            val json = preferences[sessionsKey] ?: "[]"
            val type = object : TypeToken<List<ChatSession>>() {}.type
            try {
                gson.fromJson<List<ChatSession>>(json, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }

    /**
     * 保存所有会话
     */
    suspend fun saveSessions(sessions: List<ChatSession>) {
        context.dataStore.edit { preferences ->
            val json = gson.toJson(sessions)
            preferences[sessionsKey] = json
        }
    }

    /**
     * 加载所有会话（一次性）
     */
    suspend fun loadSessions(): List<ChatSession> {
        return context.dataStore.data.map { preferences ->
            val json = preferences[sessionsKey] ?: "[]"
            val type = object : TypeToken<List<ChatSession>>() {}.type
            try {
                gson.fromJson<List<ChatSession>>(json, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }.map { it }.first()
    }

    /**
     * 清空所有会话
     */
    suspend fun clearAllSessions() {
        context.dataStore.edit { preferences ->
            preferences.remove(sessionsKey)
        }
    }
}
