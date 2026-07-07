package com.testai.service.impl;

import com.testai.entity.Template;
import com.testai.mapper.TemplateMapper;
import com.testai.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 模板服务实现类
 * 实现模板的创建、查询、更新、删除等操作
 */
@Service
public class TemplateServiceImpl implements TemplateService {

    @Autowired
    private TemplateMapper templateMapper;

    /**
     * 创建模板
     * 
     * @param template 模板实体
     * @return 创建的模板实体
     */
    @Override
    public Template createTemplate(Template template) {
        template.setStatus(1);
        templateMapper.insert(template);
        return template;
    }

    @Override
    public Template getById(Long id) {
        return templateMapper.selectById(id);
    }

    @Override
    public List<Template> listTemplates(String templateType) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Template> wrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        if (templateType != null && !templateType.isEmpty()) {
            wrapper.eq("template_type", templateType);
        }
        wrapper.orderByDesc("create_time");
        return templateMapper.selectList(wrapper);
    }

    @Override
    public Template updateTemplate(Template template) {
        templateMapper.updateById(template);
        return template;
    }

    @Override
    public void deleteById(Long id) {
        templateMapper.deleteById(id);
    }

    @Override
    public String getContentById(Long id) {
        Template template = templateMapper.selectById(id);
        return template != null ? template.getContent() : null;
    }
}
