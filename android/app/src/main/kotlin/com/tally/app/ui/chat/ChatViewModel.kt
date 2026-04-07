package com.tally.app.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tally.app.data.model.ChatMessage
import com.tally.app.data.model.MessageRole
import com.tally.app.data.model.MessageStatus
import com.tally.app.data.remote.TallyApiClient
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
)

class ChatViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun onInputChange(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun prefillInput(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isEmpty() || _uiState.value.isLoading) return

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
