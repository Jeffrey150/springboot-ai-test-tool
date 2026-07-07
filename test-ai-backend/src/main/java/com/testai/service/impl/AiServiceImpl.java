package com.testai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testai.common.BusinessException;
import com.testai.dto.AiConfig;
import com.testai.service.AiService;
import com.testai.util.SkillLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI服务实现类
 * 实现测试用例生成、质量评估、AI调用等功能，支持分批处理大文档
 */
@Slf4j
@Service
public class AiServiceImpl implements AiService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /** Skill文件路径 */
    @Value("${skill.testcase-gen.path:classpath:skills/testcase-gen.skill}")
    private String skillFilePath;

    /** 默认AI API密钥（后端配置，避免从前端泄露） */
    @Value("${ai.api-key:}")
    private String defaultApiKey;

    /** 连接超时时间（毫秒） */
    private static final int CONNECT_TIMEOUT = 30000;
    /** 读取超时时间（毫秒） */
    private static final int READ_TIMEOUT = 300000;
    /** 最大重试次数 */
    private static final int MAX_RETRY_COUNT = 3;

    /**
     * 生成测试用例
     * 
     * @param documentContent 文档内容
     * @param config AI配置
     * @param templateContent 模板内容
     * @return AI生成的测试用例内容
     */
    @Override
    public String generateTestCases(String documentContent, AiConfig config, String templateContent) {
        String prompt = buildPrompt(documentContent, templateContent);
        return callAiService(prompt, config);
    }

    @Override
    public String buildPrompt(String documentContent, String templateContent) {
        return buildPrompt(documentContent, templateContent, null);
    }
    
    @Override
    public String buildPrompt(String documentContent, String templateContent, String taskNo) {
        SkillLoader.SkillContent skillContent = SkillLoader.loadSkill(skillFilePath);
        
        String userPrompt = skillContent.getUserPrompt();
        userPrompt = userPrompt.replace("{TASK-ID}", taskNo != null ? taskNo : "");
        userPrompt = userPrompt.replace("{REQ_DOC}", documentContent);
        userPrompt = userPrompt.replace("{PROMPT_EXTENSION}", templateContent != null ? templateContent : "");
        
        return userPrompt;
    }

    @Override
    public String callAiService(String prompt, AiConfig config) {
        Exception lastException = null;
        
        for (int retry = 0; retry < MAX_RETRY_COUNT; retry++) {
            try {
                return doCallAiService(prompt, config);
            } catch (Exception e) {
                lastException = e;
                if (isRetryableException(e) && retry < MAX_RETRY_COUNT - 1) {
                    try {
                        Thread.sleep(1000L * (retry + 1));
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new BusinessException("重试被中断");
                    }
                }
            }
        }
        
        throw new BusinessException("AI服务调用失败: " + (lastException != null ? lastException.getMessage() : "未知错误"));
    }
    
    private boolean isRetryableException(Exception e) {
        String message = e.getMessage();
        return message != null && (message.contains("Read timed out") || 
                                   message.contains("Connection timed out") ||
                                   message.contains("SocketTimeoutException"));
    }

    private String resolveApiKey(AiConfig config) {
        if (config.getApiKey() != null && !config.getApiKey().isEmpty()) {
            return config.getApiKey();
        }
        if (defaultApiKey != null && !defaultApiKey.isEmpty()) {
            return defaultApiKey;
        }
        throw new BusinessException("AI API Key 未配置");
    }

    private String doCallAiService(String prompt, AiConfig config) {
        HttpURLConnection connection = null;
        OutputStreamWriter writer = null;
        BufferedReader reader = null;
        
        try {
            String apiUrl = config.getApiUrl();
            if (apiUrl == null || apiUrl.isEmpty()) {
                apiUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
            }

            URL url = new URL(apiUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + resolveApiKey(config));
            connection.setDoOutput(true);
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);

            SkillLoader.SkillContent skillContent = SkillLoader.loadSkill(skillFilePath);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", config.getModel());
            
            List<Map<String, String>> messages = new ArrayList<>();
            
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", skillContent.getSystemMessage());
            messages.add(systemMessage);
            
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            messages.add(userMessage);
            
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 4096);

            String requestJson = objectMapper.writeValueAsString(requestBody);
            
            writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
            writer.write(requestJson);
            writer.flush();

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                StringBuilder errorResponse = new StringBuilder();
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    errorResponse.append(errorLine);
                }
                errorReader.close();
                throw new BusinessException("AI服务请求失败，状态码: " + responseCode + ", 错误信息: " + errorResponse.toString());
            }

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            JsonNode root = objectMapper.readTree(response.toString());
            String content = root.path("choices").get(0).path("message").path("content").asText();
            
            if (content == null || content.isEmpty()) {
                throw new BusinessException("AI服务返回为空");
            }
            
            return content;
            
        } catch (Exception e) {
            throw new BusinessException("AI服务调用失败: " + e.getMessage());
        } finally {
            try {
                if (reader != null) reader.close();
            } catch (Exception e) {
                log.warn("关闭reader失败", e);
            }
            try {
                if (writer != null) writer.close();
            } catch (Exception e) {
                log.warn("关闭writer失败", e);
            }
            if (connection != null) connection.disconnect();
        }
    }

    @Override
    public String parseAiResponse(String aiResponse) {
        return aiResponse;
    }

    @Override
    public Double evaluateQuality(List<String> testCases) {
        return 80.0;
    }

    /**
     * 内容分段工具方法：将大文档按合理边界分割
     * 优先按文档边界分割（=== 文档 N ===），确保同一文档不会被分割到不同批次
     * @param content 原始内容
     * @param maxCharsPerChunk 每段最大字符数
     * @return 分段内容列表
     */
    private List<String> splitContentIntoChunks(String content, int maxCharsPerChunk) {
        List<String> chunks = new ArrayList<>();
        
        if (content == null || content.isEmpty()) {
            return chunks;
        }

        // 优先按文档边界分割（=== 文档 N ===）
        List<String> documents = splitByDocuments(content);
        
        for (String document : documents) {
            if (document.length() <= maxCharsPerChunk) {
                chunks.add(document);
            } else {
                // 单个文档过长，再按章节分割
                List<String> sections = splitBySections(document);
                for (String section : sections) {
                    if (section.length() <= maxCharsPerChunk) {
                        chunks.add(section);
                    } else {
                        // 章节过长，按段落进一步分割
                        chunks.addAll(splitByParagraphs(section, maxCharsPerChunk));
                    }
                }
            }
        }
        
        return chunks;
    }
    
    /**
     * 按文档边界分割（=== 文档 N ===）
     */
    private List<String> splitByDocuments(String content) {
        List<String> documents = new ArrayList<>();
        Pattern pattern = Pattern.compile("(===\\s*文档\\s+\\d+\\s*===\\s*[\\s\\S]*?)(?=\\s*===\\s*文档\\s+\\d+\\s*===|\\Z)");
        Matcher matcher = pattern.matcher(content);
        
        while (matcher.find()) {
            documents.add(matcher.group(1).trim());
        }
        
        // 如果没有找到文档边界，返回整个内容作为一段
        if (documents.isEmpty()) {
            documents.add(content);
        }
        
        return documents;
    }

    /**
     * 按Markdown章节分割
     */
    private List<String> splitBySections(String content) {
        List<String> sections = new ArrayList<>();
        Pattern pattern = Pattern.compile("(#{2,}\\s+.*?)(?=\\s*#{2,}\\s+|\\Z)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);
        
        while (matcher.find()) {
            sections.add(matcher.group(1).trim());
        }
        
        // 如果没有找到章节，返回整个内容作为一段
        if (sections.isEmpty()) {
            sections.add(content);
        }
        
        return sections;
    }

    /**
     * 按段落分割长内容
     */
    private List<String> splitByParagraphs(String content, int maxCharsPerChunk) {
        List<String> chunks = new ArrayList<>();
        StringBuilder currentChunk = new StringBuilder();
        
        String[] paragraphs = content.split("\n\n");
        
        for (String paragraph : paragraphs) {
            // 如果当前段落本身就超过限制，按句子分割
            if (paragraph.length() > maxCharsPerChunk) {
                // 先添加当前累积的内容
                if (currentChunk.length() > 0) {
                    chunks.add(currentChunk.toString().trim());
                    currentChunk = new StringBuilder();
                }
                
                // 按句子分割超长段落
                chunks.addAll(splitBySentences(paragraph, maxCharsPerChunk));
            } else if (currentChunk.length() + paragraph.length() + 2 <= maxCharsPerChunk) {
                // 可以加入当前块
                if (currentChunk.length() > 0) {
                    currentChunk.append("\n\n");
                }
                currentChunk.append(paragraph);
            } else {
                // 当前块已满，保存并开始新块
                chunks.add(currentChunk.toString().trim());
                currentChunk = new StringBuilder(paragraph);
            }
        }
        
        // 添加最后一个块
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }
        
        return chunks;
    }

    /**
     * 按句子分割超长内容
     */
    private List<String> splitBySentences(String content, int maxCharsPerChunk) {
        List<String> chunks = new ArrayList<>();
        StringBuilder currentChunk = new StringBuilder();
        
        // 按中文标点和英文标点分割句子
        String[] sentences = content.split("(?<=[。！？；])|(?<=[.!?;])");
        
        for (String sentence : sentences) {
            sentence = sentence.trim();
            if (sentence.isEmpty()) continue;
            
            if (currentChunk.length() + sentence.length() + 1 <= maxCharsPerChunk) {
                if (currentChunk.length() > 0) {
                    currentChunk.append("");
                }
                currentChunk.append(sentence);
            } else {
                if (currentChunk.length() > 0) {
                    chunks.add(currentChunk.toString());
                }
                // 如果单个句子也超过限制，强制分割
                if (sentence.length() > maxCharsPerChunk) {
                    int start = 0;
                    while (start < sentence.length()) {
                        int end = Math.min(start + maxCharsPerChunk, sentence.length());
                        chunks.add(sentence.substring(start, end));
                        start = end;
                    }
                } else {
                    currentChunk = new StringBuilder(sentence);
                }
            }
        }
        
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString());
        }
        
        return chunks;
    }

    /**
     * 分批次调用AI服务
     * @param documentContent 文档内容
     * @param templateContent 模板内容
     * @param config AI配置
     * @param maxTokensPerBatch 每批次最大token数（估算为字符数的2倍）
     * @return 合并后的AI响应
     */
    public String callAiServiceWithBatching(String documentContent, String templateContent, AiConfig config, int maxTokensPerBatch) {
        return callAiServiceWithBatching(documentContent, templateContent, null, config, maxTokensPerBatch);
    }
    
    @Override
    public String callAiServiceWithBatching(String documentContent, String templateContent, String taskNo, AiConfig config, int maxTokensPerBatch) {
        // 估算每批次最大字符数（1 token ≈ 2-3 中文字符）
        int maxCharsPerChunk = maxTokensPerBatch * 2;
        
        // 将文档内容分段
        List<String> contentChunks = splitContentIntoChunks(documentContent, maxCharsPerChunk);
        
        if (contentChunks.isEmpty()) {
            throw new BusinessException("文档内容为空");
        }
        
        // 如果只有一段，直接调用
        if (contentChunks.size() == 1) {
            String prompt = buildPrompt(contentChunks.get(0), templateContent, taskNo);
            return callAiService(prompt, config);
        }
        
        // 多段内容，分批处理并保持上下文
        StringBuilder finalResult = new StringBuilder();
        SkillLoader.SkillContent skillContent = SkillLoader.loadSkill(skillFilePath);
        String systemMessage = skillContent.getSystemMessage();
        
        // 已生成的模块列表，用于去重
        List<String> generatedModules = new ArrayList<>();
        
        // 当前处理的文档索引（用于判断是否跨文档）
        int currentDocIndex = 0;
        
        // 第一批：处理第一段内容
        String firstPrompt = buildBatchPrompt(contentChunks.get(0), templateContent, taskNo, 1, contentChunks.size(), false, generatedModules, false);
        String firstResponse = callAiServiceWithContext(firstPrompt, systemMessage, null, config);
        finalResult.append(firstResponse).append("\n\n");
        
        // 提取已生成的模块
        extractGeneratedModules(firstResponse, generatedModules);
        
        // 检查是否进入新文档
        currentDocIndex = updateDocumentIndex(contentChunks.get(0), currentDocIndex);
        
        // 后续批次：处理剩余内容，携带上下文
        String previousResponse = firstResponse;
        for (int i = 1; i < contentChunks.size(); i++) {
            String chunk = contentChunks.get(i);
            boolean isLast = (i == contentChunks.size() - 1);
            
            // 检查是否进入新文档
            int newDocIndex = updateDocumentIndex(chunk, currentDocIndex);
            boolean isNewDocument = (newDocIndex > currentDocIndex);
            currentDocIndex = newDocIndex;
            
            String prompt = buildBatchPrompt(chunk, templateContent, taskNo, i + 1, contentChunks.size(), isLast, generatedModules, isNewDocument);
            String response = callAiServiceWithContext(prompt, systemMessage, previousResponse, config);
            
            // 移除根节点（# 开头的行），避免重复
            response = removeRootNode(response);
            
            finalResult.append(response).append("\n\n");
            previousResponse = response;
            
            // 更新已生成的模块列表（跨文档时不严格去重，只记录）
            if (!isNewDocument) {
                extractGeneratedModules(response, generatedModules);
            }
        }
        
        return finalResult.toString().trim();
    }

    /**
     * 构建批次处理的提示词
     */
    private String buildBatchPrompt(String contentChunk, String templateContent, String taskNo, int batchNum, int totalBatches, boolean isLast, List<String> generatedModules, boolean isNewDocument) {
        SkillLoader.SkillContent skillContent = SkillLoader.loadSkill(skillFilePath);
        
        String userPrompt = skillContent.getUserPrompt();
        userPrompt = userPrompt.replace("{TASK-ID}", taskNo != null ? taskNo : "");
        userPrompt = userPrompt.replace("{REQ_DOC}", contentChunk);
        userPrompt = userPrompt.replace("{PROMPT_EXTENSION}", templateContent != null ? templateContent : "");
        
        StringBuilder batchInfo = new StringBuilder();
        batchInfo.append("\n\n--- 批次信息 ---\n");
        batchInfo.append("当前批次: ").append(batchNum).append("/").append(totalBatches).append("\n");
        
        if (batchNum == 1) {
            batchInfo.append("注意: 这是第一批内容，请生成完整的测试用例文档结构，包含 # 根节点。\n");
        } else if (isNewDocument) {
            // 跨文档时：提示AI这是新文档，需要补充测试用例，而不是跳过
            batchInfo.append("重要提示: 这是后续批次（第").append(batchNum).append("批），且是新的文档（设计文档/补充文档）。\n");
            batchInfo.append("请直接生成 ## 大模块内容，**不要重复生成 # 根节点**。\n");
            batchInfo.append("对于与之前模块名称相同或相关的内容，请进行补充和细化，不要简单跳过。\n");
            batchInfo.append("之前已生成的模块参考: ").append(generatedModules != null && !generatedModules.isEmpty() ? String.join(", ", generatedModules) : "无").append("\n");
        } else {
            // 同文档内：严格去重
            batchInfo.append("重要提示: 这是后续批次（第").append(batchNum).append("批），请直接生成 ## 大模块内容，**不要重复生成 # 根节点**，只需生成业务模块的测试用例。\n");
            
            // 添加已生成模块列表，避免重复
            if (generatedModules != null && !generatedModules.isEmpty()) {
                batchInfo.append("已生成的模块（请不要重复）: ").append(String.join(", ", generatedModules)).append("\n");
            }
        }
        
        if (!isLast) {
            batchInfo.append("后续批次将继续提供更多需求内容，请专注于当前批次涉及的模块。\n");
        } else {
            batchInfo.append("这是最后一批内容，请确保覆盖当前批次所有功能点。\n");
        }
        
        return userPrompt + batchInfo.toString();
    }
    
    /**
     * 更新文档索引（检测是否进入新文档）
     */
    private int updateDocumentIndex(String content, int currentIndex) {
        if (content == null) {
            return currentIndex;
        }
        // 检查是否包含新文档标记
        Pattern pattern = Pattern.compile("===\\s*文档\\s+(\\d+)\\s*===");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            int docNum = Integer.parseInt(matcher.group(1));
            return Math.max(currentIndex, docNum);
        }
        return currentIndex;
    }
    
    /**
     * 从AI响应中提取已生成的模块名称（## 开头的标题）
     */
    private void extractGeneratedModules(String response, List<String> generatedModules) {
        if (response == null || response.isEmpty() || generatedModules == null) {
            return;
        }
        
        Pattern pattern = Pattern.compile("^##\\s+(.+)$", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(response);
        
        while (matcher.find()) {
            String moduleName = matcher.group(1).trim();
            if (!generatedModules.contains(moduleName)) {
                generatedModules.add(moduleName);
            }
        }
    }
    
    /**
     * 移除响应中的根节点（# 开头的行）
     * 用于后续批次处理时确保不会有重复的根节点
     */
    private String removeRootNode(String response) {
        if (response == null || response.isEmpty()) {
            return response;
        }
        
        // 移除以 # 开头的行（根节点），保留 ## 及以下的内容
        return response.replaceAll("(?m)^#\\s+.+$\\n?", "").trim();
    }
    
    /**
     * 带上下文的AI调用（带重试）
     */
    private String callAiServiceWithContext(String prompt, String systemMessage, String context, AiConfig config) {
        Exception lastException = null;
        
        for (int retry = 0; retry < MAX_RETRY_COUNT; retry++) {
            try {
                return doCallAiServiceWithContext(prompt, systemMessage, context, config);
            } catch (Exception e) {
                lastException = e;
                if (isRetryableException(e) && retry < MAX_RETRY_COUNT - 1) {
                    try {
                        Thread.sleep(1000L * (retry + 1));
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new BusinessException("重试被中断");
                    }
                }
            }
        }
        
        throw new BusinessException("AI服务调用失败: " + (lastException != null ? lastException.getMessage() : "未知错误"));
    }
    
    /**
     * 带上下文的AI调用（实际执行）
     */
    private String doCallAiServiceWithContext(String prompt, String systemMessage, String context, AiConfig config) {
        HttpURLConnection connection = null;
        OutputStreamWriter writer = null;
        BufferedReader reader = null;
        
        try {
            String apiUrl = config.getApiUrl();
            if (apiUrl == null || apiUrl.isEmpty()) {
                apiUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
            }

            URL url = new URL(apiUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + resolveApiKey(config));
            connection.setDoOutput(true);
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", config.getModel());
            
            List<Map<String, String>> messages = new ArrayList<>();
            
            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemMessage);
            messages.add(systemMsg);
            
            // 添加上下文（如果有）
            if (context != null && !context.isEmpty()) {
                Map<String, String> contextMsg = new HashMap<>();
                contextMsg.put("role", "assistant");
                contextMsg.put("content", context);
                messages.add(contextMsg);
            }
            
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", prompt);
            messages.add(userMsg);
            
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 4096);

            String requestJson = objectMapper.writeValueAsString(requestBody);
            
            writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
            writer.write(requestJson);
            writer.flush();

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                StringBuilder errorResponse = new StringBuilder();
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    errorResponse.append(errorLine);
                }
                errorReader.close();
                throw new BusinessException("AI服务请求失败，状态码: " + responseCode + ", 错误信息: " + errorResponse.toString());
            }

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            JsonNode root = objectMapper.readTree(response.toString());
            String content = root.path("choices").get(0).path("message").path("content").asText();
            
            if (content == null || content.isEmpty()) {
                throw new BusinessException("AI服务返回为空");
            }
            
            return content;
            
        } catch (Exception e) {
            throw new BusinessException("AI服务调用失败: " + e.getMessage());
        } finally {
            try {
                if (reader != null) reader.close();
            } catch (Exception e) {
                log.warn("关闭reader失败", e);
            }
            try {
                if (writer != null) writer.close();
            } catch (Exception e) {
                log.warn("关闭writer失败", e);
            }
            if (connection != null) connection.disconnect();
        }
    }
}
