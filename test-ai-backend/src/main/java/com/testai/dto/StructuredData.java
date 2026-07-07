package com.testai.dto;

import lombok.Data;

import java.util.List;

/**
 * 结构化数据 DTO（AI 分析结果）
 */
@Data
public class StructuredData {

    /**
     * 功能点列表
     */
    private List<String> features;

    /**
     * 字段列表
     */
    private List<FieldInfo> fields;

    /**
     * 业务规则列表
     */
    private List<String> businessRules;

    /**
     * 字段信息
     */
    @Data
    public static class FieldInfo {
        /**
         * 字段名称
         */
        private String name;

        /**
         * 字段类型
         */
        private String type;

        /**
         * 字段描述
         */
        private String description;

        /**
         * 是否必填
         */
        private Boolean required;

        /**
         * 默认值
         */
        private String defaultValue;
    }
}
