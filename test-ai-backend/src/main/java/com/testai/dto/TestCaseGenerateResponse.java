package com.testai.dto;

import lombok.Data;

/**
 * 测试用例生成响应 DTO
 */
@Data
public class TestCaseGenerateResponse {

    /**
     * 生成的 Markdown 格式测试用例内容
     */
    private String markdownContent;
}
