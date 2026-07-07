package com.testai.controller;

import com.testai.common.Result;
import com.testai.entity.Template;
import com.testai.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 模板管理控制器
 * 负责模板的创建、查询、更新、删除等操作
 */
@RestController
@RequestMapping("/templates")
public class TemplateController {

    @Autowired
    private TemplateService templateService;

    /**
     * 创建模板
     * 
     * @param template 模板实体
     * @return 创建的模板实体
     */
    @PostMapping("/create")
    public Result<Template> create(@RequestBody Template template) {
        return Result.success(templateService.createTemplate(template));
    }

    /**
     * 根据ID查询模板详情
     * 
     * @param id 模板ID
     * @return 模板实体
     */
    @GetMapping("/{id}")
    public Result<Template> getById(@PathVariable Long id) {
        return Result.success(templateService.getById(id));
    }

    /**
     * 查询模板列表
     * 
     * @param templateType 模板类型筛选（可选）
     * @return 模板列表
     */
    @GetMapping("/list")
    public Result<List<Template>> list(
            @RequestParam(required = false) String templateType) {
        return Result.success(templateService.listTemplates(templateType));
    }

    /**
     * 更新模板
     * 
     * @param id 模板ID
     * @param template 模板实体
     * @return 更新后的模板实体
     */
    @PutMapping("/{id}")
    public Result<Template> update(@PathVariable Long id, @RequestBody Template template) {
        template.setId(id);
        return Result.success(templateService.updateTemplate(template));
    }

    /**
     * 删除模板
     * 
     * @param id 模板ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        templateService.deleteById(id);
        return Result.success();
    }
}
