package com.testai.common;

import lombok.Data;
import java.util.List;

/**
 * 分页结果封装类
 * 
 * @param <T> 数据类型
 */
@Data
public class PageResult<T> {
    
    /** 数据列表 */
    private List<T> list;
    
    /** 总记录数 */
    private Long total;
    
    /** 当前页码 */
    private Integer pageNum;
    
    /** 每页数量 */
    private Integer pageSize;
    
    /**
     * 构造方法
     * 
     * @param list 数据列表
     * @param total 总记录数
     * @param pageNum 当前页码
     * @param pageSize 每页数量
     */
    public PageResult(List<T> list, Long total, Integer pageNum, Integer pageSize) {
        this.list = list;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }
    
    /**
     * 创建分页结果
     * 
     * @param list 数据列表
     * @param total 总记录数
     * @param pageNum 当前页码
     * @param pageSize 每页数量
     * @return 分页结果对象
     */
    public static <T> PageResult<T> of(List<T> list, Long total, Integer pageNum, Integer pageSize) {
        return new PageResult<>(list, total, pageNum, pageSize);
    }
}
