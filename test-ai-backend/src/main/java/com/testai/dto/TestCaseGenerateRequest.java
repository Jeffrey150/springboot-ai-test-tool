package com.testai.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 测试用例生成请求 DTO
 */
@Data
public class TestCaseGenerateRequest {

    /**
     * 上传的文件列表
     */
    private List<MultipartFile> files;
}
