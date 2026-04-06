package com.tally.app.data.remote

import com.tally.app.data.model.ChatMessage
import com.tally.app.data.model.MessageRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * 后端 HTTP 客户端
 * 负责将聊天消息发送到后端，并以 Flow<String> 形式返回流式文字。
 *
 * Vercel AI Data Stream 格式说明：
 *   f:{...}    → messageId（忽略）
 *   0:"text"   → 文字块（需拼接）
 *   9:{...}    → 工具调用（忽略）
 *   a:{...}    → 工具结果（忽略）
 *   e:{...}    → 单步结束（忽略）
 *   d:{...}    → 流结束（退出循环）
 */
object TallyApiClient {

    // adb reverse tcp:3000 tcp:3000 将手机 localhost:3000 隧道到 WSL 后端
    private const val BASE_URL = "http://localhost:3000"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS) // 流式响应需要更长的读超时
        .build()

    /**
     * 发送消息并返回流式文字 Flow。
     * Flow 每次 emit 一个增量字符串，调用方负责拼接显示。
     */
    fun streamChat(messages: List<ChatMessage>): Flow<String> = flow {
        val bodyJson = JSONObject().apply {
            put("messages", JSONArray().apply {
                messages.forEach { msg ->
                    put(JSONObject().apply {
                        put("role", if (msg.role == MessageRole.USER) "user" else "assistant")
                        put("content", msg.content)
                    })
                }
            })
        }

        val request = Request.Builder()
            .url("$BASE_URL/api/chat")
            .post(bodyJson.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("后端返回错误：${response.code}")
            }

            val source = response.body?.source()
                ?: throw Exception("响应体为空")

            try {
                while (!source.exhausted()) {
                    val line = source.readUtf8Line() ?: break

                    // 解析 Vercel AI Data Stream 的文字块：0:"text"
                    if (line.startsWith("0:")) {
                        val raw = line.removePrefix("0:")
                        if (raw.length >= 2 && raw.startsWith("\"") && raw.endsWith("\"")) {
                            try {
                                val text = JSONObject("{\"v\":$raw}").getString("v")
                                emit(text)
                            } catch (_: Exception) {
                                // JSON 解析失败，直接返回原始内容
                                emit(raw.trim('"'))
                            }
                        } else {
                            // 不是标准 JSON 字符串，直接返回
                            emit(raw)
                        }
                    }

                    // d: 表示流结束，退出循环
                    if (line.startsWith("d:")) {
                        return@use
                    }
                }
            } catch (e: IOException) {
                // 流结束或连接关闭，这是正常的
                // "unexpected end of stream" 也属于此类情况
                return@use
            }
        }
    }.flowOn(Dispatchers.IO)
}
