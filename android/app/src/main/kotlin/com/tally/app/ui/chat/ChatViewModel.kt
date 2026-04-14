package com.tally.app.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tally.app.data.model.ChatMessage
import com.tally.app.data.model.ChatSession
import com.tally.app.data.model.MessageRole
import com.tally.app.data.model.MessageStatus
import com.tally.app.data.remote.TallyApiClient
import com.tally.app.data.repository.ChatSessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val sessions: List<ChatSession> = emptyList(),
    val currentSessionId: String? = null,
    val showHistory: Boolean = false,
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChatSessionRepository(application)

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        // 从本地存储加载会话历史
        viewModelScope.launch {
            val savedSessions = repository.loadSessions()
            _uiState.update { it.copy(sessions = savedSessions) }
        }
    }

    fun onInputChange(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun prefillInput(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    /**
     * 创建新对话
     */
    fun createNewChat() {
        // 先保存当前对话（如果有消息）
        saveCurrentSession()
        // 清空当前对话
        _uiState.update { state ->
            state.copy(
                messages = emptyList(),
                inputText = "",
                currentSessionId = null,
                showHistory = false
            )
        }
    }

    /**
     * 切换对话历史显示
     */
    fun toggleHistory() {
        _uiState.update { it.copy(showHistory = !it.showHistory) }
    }

    /**
     * 加载指定会话
     */
    fun loadSession(sessionId: String) {
        val session = _uiState.value.sessions.find { it.id == sessionId }
        session?.let {
            _uiState.update { state ->
                state.copy(
                    messages = it.messages,
                    currentSessionId = it.id,
                    showHistory = false
                )
            }
        }
    }

    /**
     * 删除会话
     */
    fun deleteSession(sessionId: String) {
        _uiState.update { state ->
            val updatedSessions = state.sessions.filter { it.id != sessionId }
            val newCurrentId = if (state.currentSessionId == sessionId) null else state.currentSessionId
            state.copy(
                sessions = updatedSessions,
                currentSessionId = newCurrentId
            )
        }
        // 保存到本地
        viewModelScope.launch {
            repository.saveSessions(_uiState.value.sessions)
        }
    }

    /**
     * 保存当前会话到历史并持久化
     */
    private fun saveCurrentSession() {
        val currentState = _uiState.value
        if (currentState.messages.isNotEmpty()) {
            val title = generateSessionTitle(currentState.messages)
            val existingSession = currentState.sessions.find { it.id == currentState.currentSessionId }

            val updatedSessions = if (existingSession != null) {
                // 更新现有会话
                currentState.sessions.map { session ->
                    if (session.id == existingSession.id) {
                        session.copy(
                            messages = currentState.messages,
                            title = title,
                            updatedAt = System.currentTimeMillis()
                        )
                    } else session
                }
            } else {
                // 创建新会话
                val newSession = ChatSession(
                    title = title,
                    messages = currentState.messages
                )
                listOf(newSession) + currentState.sessions
            }

            _uiState.update { state ->
                val newSessionId = existingSession?.id ?: updatedSessions.first().id
                state.copy(
                    sessions = updatedSessions,
                    currentSessionId = newSessionId
                )
            }

            // 异步保存到本地存储
            viewModelScope.launch {
                repository.saveSessions(updatedSessions)
            }
        }
    }

    /**
     * 生成会话标题（取第一条用户消息的前 12 个字符）
     */
    private fun generateSessionTitle(messages: List<ChatMessage>): String {
        val firstUserMessage = messages.find { it.role == MessageRole.USER }
        return firstUserMessage?.content?.take(12)?.let {
            if (firstUserMessage.content.length > 12) "$it..." else it
        } ?: "新对话"
    }

    fun clearChat() {
        saveCurrentSession()
        _uiState.update { ChatUiState(sessions = it.sessions) }
    }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isEmpty() || _uiState.value.isLoading) return

        // 如果是第一条消息，先保存当前空会话（如果有的话），开始新会话
        if (_uiState.value.messages.isEmpty()) {
            saveCurrentSession()
        }

        // 追加用户消息
        val userMsg = ChatMessage(role = MessageRole.USER, content = text)
        _uiState.update { state ->
            state.copy(
                messages = state.messages + userMsg,
                inputText = "",
                isLoading = true,
            )
        }

        // 预先插入一条空的 AI 消息，用于流式填充
        val assistantMsgId = java.util.UUID.randomUUID().toString()
        val assistantMsg = ChatMessage(
            id = assistantMsgId,
            role = MessageRole.ASSISTANT,
            content = "",
            status = MessageStatus.STREAMING,
        )
        _uiState.update { it.copy(messages = it.messages + assistantMsg) }

        viewModelScope.launch {
            // 构建发送给后端的消息历史（只发 USER/ASSISTANT 的 DONE 消息 + 本次用户消息）
            val history = _uiState.value.messages
                .filter { it.id != assistantMsgId }

            TallyApiClient.streamChat(history)
                .catch { e ->
                    // 错误：将 AI 消息标记为 ERROR
                    updateAssistantMsg(assistantMsgId, "出错了：${e.message}", MessageStatus.ERROR)
                    _uiState.update { it.copy(isLoading = false) }
                }
                .collect { chunk ->
                    // 逐块拼接 AI 消息内容
                    _uiState.update { state ->
                        val updated = state.messages.map { msg ->
                            if (msg.id == assistantMsgId) {
                                msg.copy(content = msg.content + chunk)
                            } else msg
                        }
                        state.copy(messages = updated)
                    }
                }

            // 流结束：标记为 DONE
            updateAssistantMsg(
                assistantMsgId,
                _uiState.value.messages.find { it.id == assistantMsgId }?.content ?: "",
                MessageStatus.DONE,
            )
            _uiState.update { it.copy(isLoading = false) }

            // 对话完成后自动保存
            saveCurrentSession()
        }
    }

    private fun updateAssistantMsg(id: String, content: String, status: MessageStatus) {
        _uiState.update { state ->
            state.copy(messages = state.messages.map { msg ->
                if (msg.id == id) msg.copy(content = content, status = status) else msg
            })
        }
    }
}
