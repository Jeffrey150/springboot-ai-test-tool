package com.testai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.testai.entity.Document;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文档数据访问层
 */
@Mapper
public interface DocumentMapper extends BaseMapper<Document> {
}
