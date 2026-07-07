package com.testai.controller;

import com.testai.common.Result;
import com.testai.entity.TestDemo;
import com.testai.service.TestDemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 测试示例控制器
 */
@RestController
@RequestMapping("/test-demo")
public class TestDemoController {

    @Autowired
    private TestDemoService testDemoService;

    /**
     * 查询测试数据列表
     *
     * @param name 名称（可选，支持模糊查询）
     * @return 测试数据列表
     */
    @GetMapping("/list")
    public Result<List<TestDemo>> list(@RequestParam(required = false) String name) {
        List<TestDemo> list = testDemoService.listByName(name);
        return Result.success(list);
    }

    /**
     * 根据ID查询测试数据
     *
     * @param id 主键ID
     * @return 测试数据
     */
    @GetMapping("/{id}")
    public Result<TestDemo> getById(@PathVariable Long id) {
        TestDemo testDemo = testDemoService.getById(id);
        return Result.success(testDemo);
    }

    /**
     * 新增测试数据
     *
     * @param testDemo 测试数据
     * @return 结果
     */
    @PostMapping
    public Result<Void> save(@RequestBody TestDemo testDemo) {
        testDemoService.save(testDemo);
        return Result.success();
    }

    /**
     * 更新测试数据
     *
     * @param testDemo 测试数据
     * @return 结果
     */
    @PutMapping
    public Result<Void> update(@RequestBody TestDemo testDemo) {
        testDemoService.updateById(testDemo);
        return Result.success();
    }

    /**
     * 删除测试数据
     *
     * @param id 主键ID
     * @return 结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        testDemoService.removeById(id);
        return Result.success();
    }
}
