package com.testai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 任务文件信息 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskFileDTO {

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件内容字节数组
     */
    private byte[] content;
}
