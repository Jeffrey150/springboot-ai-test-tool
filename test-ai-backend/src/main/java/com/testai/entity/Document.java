package com.testai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 文档实体类
 * 对应数据库表 document
 */
@Data
@TableName("document")
public class Document {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 文件名 */
    private String fileName;

    /** 文件存储路径 */
    private String filePath;

    /** 文件类型（扩展名） */
    private String fileType;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 文档类型（如 REQUIREMENT、DESIGN 等） */
    private String documentType;

    /** 状态（0-无效，1-有效） */
    private Integer status;

    /** 创建时间（自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间（自动填充） */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
