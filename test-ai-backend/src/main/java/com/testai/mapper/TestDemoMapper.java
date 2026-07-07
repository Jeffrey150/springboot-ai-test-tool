package com.testai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.testai.entity.TestDemo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 测试示例数据访问层
 */
@Mapper
public interface TestDemoMapper extends BaseMapper<TestDemo> {
}
