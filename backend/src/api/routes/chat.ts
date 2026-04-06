import { Router, Request, Response } from 'express';
import { z } from 'zod';
import { streamChat } from '../../agents/orchestrator';
import { CoreMessage } from 'ai';

const router = Router();

// 请求体校验 schema
const messageSchema = z.object({
  role: z.enum(['user', 'assistant', 'system']),
  content: z.string().min(1),
});

const chatRequestSchema = z.object({
  messages: z.array(messageSchema).min(1),
  conversationId: z.string().optional(),
});

/**
 * POST /api/chat
 * 流式对话接口（SSE / Data Stream）
 * 请求体：{ messages: [{role, content}], conversationId? }
 * 响应：Vercel AI SDK Data Stream 格式（text/event-stream）
 */
router.post('/chat', async (req: Request, res: Response) => {
  const parsed = chatRequestSchema.safeParse(req.body);
  if (!parsed.success) {
    res.status(400).json({
      success: false,
      error: {
        code: 'VALIDATION_ERROR',
        message: '请求格式错误',
        details: parsed.error.flatten(),
      },
    });
    return;
  }

  const { messages } = parsed.data;

  try {
    const result = streamChat(messages as CoreMessage[]);
    // pipeDataStreamToResponse 将 AI SDK 数据流直接写入 Express response
    result.pipeDataStreamToResponse(res);
  } catch (error: unknown) {
    const message = error instanceof Error ? error.message : '未知错误';
    res.status(500).json({
      success: false,
      error: {
        code: 'CHAT_ERROR',
        message,
      },
    });
  }
});

export default router;
