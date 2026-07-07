package com.testai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.testai.entity.TestDemo;

import java.util.List;

/**
 * 测试示例服务接口
 */
public interface TestDemoService extends IService<TestDemo> {

    /**
     * 根据名称查询测试数据
     *
     * @param name 名称
     * @return 测试数据列表
     */
    List<TestDemo> listByName(String name);
}
