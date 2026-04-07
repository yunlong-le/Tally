import { tool } from 'ai';
import { z } from 'zod';
import { tavily } from '@tavily/core';

/**
 * 网络搜索工具（Tavily）
 * 用于查询实时信息，如展会日期、新闻、活动安排等
 */
export const webSearchTool = tool({
  description:
    '搜索互联网上的实时信息。适用于：活动/展会/比赛的时间与地点、近期新闻、最新价格、天气等需要最新数据的问题。',
  parameters: z.object({
    query: z.string().describe('搜索关键词，使用简洁的中文或英文短语'),
    maxResults: z
      .number()
      .int()
      .min(1)
      .max(5)
      .optional()
      .default(3)
      .describe('返回结果数量，默认 3'),
  }),
  execute: async ({ query, maxResults = 3 }) => {
    const apiKey = process.env.TAVILY_API_KEY;
    if (!apiKey || apiKey === 'your_tavily_api_key_here') {
      return { error: 'TAVILY_API_KEY 未配置，无法执行网络搜索' };
    }

    try {
      const client = tavily({ apiKey });
      const response = await client.search(query, {
        maxResults,
        searchDepth: 'basic',
      });

      return {
        results: response.results.map((r) => ({
          title: r.title,
          url: r.url,
          content: r.content,
          publishedDate: r.publishedDate ?? null,
        })),
        query,
      };
    } catch (error) {
      const message = error instanceof Error ? error.message : String(error);
      return { error: `搜索失败: ${message}`, query };
    }
  },
});

export const searchTools = {
  webSearch: webSearchTool,
};
