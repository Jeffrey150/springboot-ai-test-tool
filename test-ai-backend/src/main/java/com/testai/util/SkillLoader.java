package com.testai.util;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Skill文件加载器
 * 用于加载AI提示词模板文件（.skill文件）
 */
@Component
public class SkillLoader {

    /** 系统消息标识前缀 */
    private static final String SYSTEM_MSG_PREFIX = "===SYSTEM_MSG===";
    /** 用户提示词标识前缀 */
    private static final String USER_PROMPT_PREFIX = "===USER_PROMPT===";

    private final ResourceLoader resourceLoader;

    /**
     * 构造函数
     * 
     * @param resourceLoader 资源加载器
     */
    public SkillLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * 加载Skill文件内容
     * 
     * @param skillPath 文件路径
     * @return Skill内容对象
     */
    public static SkillContent loadSkill(String skillPath) {
        SkillContent content = new SkillContent();
        
        try {
            Resource resource = new org.springframework.core.io.DefaultResourceLoader().getResource(skillPath);
            
            if (!resource.exists()) {
                throw new RuntimeException("Skill文件不存在: " + skillPath);
            }
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                
                StringBuilder currentSection = new StringBuilder();
                String currentSectionType = null;
                
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith(SYSTEM_MSG_PREFIX)) {
                        if (currentSectionType != null) {
                            saveSection(content, currentSectionType, currentSection.toString());
                        }
                        currentSectionType = "system";
                        currentSection = new StringBuilder();
                    } else if (line.startsWith(USER_PROMPT_PREFIX)) {
                        if (currentSectionType != null) {
                            saveSection(content, currentSectionType, currentSection.toString());
                        }
                        currentSectionType = "user";
                        currentSection = new StringBuilder();
                    } else {
                        currentSection.append(line).append("\n");
                    }
                }
                
                if (currentSectionType != null) {
                    saveSection(content, currentSectionType, currentSection.toString());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("加载Skill文件失败: " + e.getMessage(), e);
        }
        
        return content;
    }
    
    private static void saveSection(SkillContent content, String sectionType, String sectionContent) {
        if ("system".equals(sectionType)) {
            content.setSystemMessage(sectionContent.trim());
        } else if ("user".equals(sectionType)) {
            content.setUserPrompt(sectionContent.trim());
        }
    }

    public static class SkillContent {
        private String systemMessage;
        private String userPrompt;
        
        public String getSystemMessage() {
            return systemMessage;
        }
        
        public void setSystemMessage(String systemMessage) {
            this.systemMessage = systemMessage;
        }
        
        public String getUserPrompt() {
            return userPrompt;
        }
        
        public void setUserPrompt(String userPrompt) {
            this.userPrompt = userPrompt;
        }
    }
}
