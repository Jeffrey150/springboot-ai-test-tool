package com.testai.service;

import com.testai.entity.Template;
import java.util.List;

/**
 * 模板服务接口
 * 提供模板的创建、查询、更新、删除等操作
 */
public interface TemplateService {

    /**
     * 创建模板
     * 
     * @param template 模板实体
     * @return 创建的模板实体
     */
    Template createTemplate(Template template);

    /**
     * 根据ID查询模板详情
     * 
     * @param id 模板ID
     * @return 模板实体
     */
    Template getById(Long id);

    /**
     * 查询模板列表
     * 
     * @param templateType 模板类型筛选（可选）
     * @return 模板列表
     */
    List<Template> listTemplates(String templateType);

    /**
     * 更新模板
     * 
     * @param template 模板实体
     * @return 更新后的模板实体
     */
    Template updateTemplate(Template template);

    /**
     * 删除模板
     * 
     * @param id 模板ID
     */
    void deleteById(Long id);

    /**
     * 根据ID获取模板内容
     * 
     * @param id 模板ID
     * @return 模板内容
     */
    String getContentById(Long id);
}
