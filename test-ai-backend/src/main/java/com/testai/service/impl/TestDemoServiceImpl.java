package com.testai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.testai.entity.TestDemo;
import com.testai.mapper.TestDemoMapper;
import com.testai.service.TestDemoService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 测试示例服务实现
 */
@Service
public class TestDemoServiceImpl extends ServiceImpl<TestDemoMapper, TestDemo> implements TestDemoService {

    @Override
    public List<TestDemo> listByName(String name) {
        LambdaQueryWrapper<TestDemo> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.trim().isEmpty()) {
            wrapper.like(TestDemo::getName, name);
        }
        wrapper.orderByDesc(TestDemo::getCreateTime);
        return this.list(wrapper);
    }
}
