import { createOpenAI } from '@ai-sdk/openai';

/**
 * kimi-k2.5 是 thinking 模型，Vercel AI SDK 重构多步消息历史时不保留 reasoning_content，
 * 导致 Moonshot API 拒绝第二步请求（"reasoning_content is missing"）。
 *
 * 【实验性修复】使用占位符 "." 代替真实 reasoning_content。
 * 假设：多步工具调用中，模型下一步的决策由 tool_result 驱动，
 *       而非上一步的推理内容，因此占位符不影响输出质量。
 * 如果实验证明需要真实内容，再改回完整的流式拦截方案。
 */

interface MoonshotMessage {
  role: string;
  content?: unknown;
  tool_calls?: unknown[];
  reasoning_content?: string;
}

interface MoonshotRequestBody {
  messages?: MoonshotMessage[];
  [key: string]: unknown;
}

async function kimiCompatFetch(
  url: string | URL | Request,
  options?: RequestInit
): Promise<Response> {
  if (options?.body && typeof options.body === 'string') {
    let body: MoonshotRequestBody;
    try {
      body = JSON.parse(options.body) as MoonshotRequestBody;
    } catch {
      return fetch(url, options);
    }

    // assistant 消息含 tool_calls 但缺 reasoning_content 时注入占位符
    if (Array.isArray(body.messages)) {
      const patched = body.messages.map((msg) => {
        if (
          msg.role === 'assistant' &&
          Array.isArray(msg.tool_calls) &&
          msg.tool_calls.length > 0 &&
          msg.reasoning_content === undefined
        ) {
          return { ...msg, reasoning_content: '.' };
        }
        return msg;
      });

      return fetch(url, {
        ...options,
        body: JSON.stringify({ ...body, messages: patched }),
      });
    }
  }

  return fetch(url, options);
}

/**
 * 月之暗面 Kimi 客户端（OpenAI-compatible）
 */
export function createKimiClient() {
  const apiKey = process.env.KIMI_API_KEY;
  if (!apiKey) {
    throw new Error('KIMI_API_KEY 未配置，请检查 backend/.env 文件');
  }

  return createOpenAI({
    baseURL: process.env.KIMI_BASE_URL || 'https://api.moonshot.cn/v1',
    apiKey,
    fetch: kimiCompatFetch,
  });
}

export const KIMI_MODEL = process.env.KIMI_MODEL || 'kimi-k2.5';
