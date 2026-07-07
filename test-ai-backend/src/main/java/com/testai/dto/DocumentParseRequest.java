package com.testai.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文档解析请求 DTO
 */
@Data
public class DocumentParseRequest {

    /**
     * 上传的文件列表
     */
    private List<MultipartFile> files;
}
