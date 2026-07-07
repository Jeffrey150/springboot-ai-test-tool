package com.testai.service;

import com.testai.entity.Document;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

/**
 * 文档服务接口
 * 提供文档的上传、查询、删除、内容提取等操作
 */
public interface DocumentService {

    /**
     * 上传单个文档
     * 
     * @param file 文件
     * @param documentType 文档类型
     * @return 上传的文档实体
     */
    Document uploadDocument(MultipartFile file, String documentType);

    /**
     * 批量上传文档（统一类型）
     * 
     * @param files 文件列表
     * @param documentType 文档类型
     * @return 上传的文档列表
     */
    List<Document> uploadDocuments(List<MultipartFile> files, String documentType);

    /**
     * 批量上传文档，每个文档可以指定不同的文档类型
     * 
     * @param files 文件列表
     * @param documentTypes 文档类型列表，与文件列表一一对应
     * @return 上传的文档列表
     */
    List<Document> uploadDocumentsWithTypes(List<MultipartFile> files, List<String> documentTypes);

    /**
     * 根据ID查询文档详情
     * 
     * @param id 文档ID
     * @return 文档实体
     */
    Document getById(Long id);

    /**
     * 删除文档
     * 
     * @param id 文档ID
     */
    void deleteById(Long id);

    /**
     * 查询文档列表
     * 
     * @param documentType 文档类型筛选（可选）
     * @param pageNum 页码（可选）
     * @param pageSize 每页数量（可选）
     * @return 文档列表
     */
    List<Document> listDocuments(String documentType, Integer pageNum, Integer pageSize);

    /**
     * 提取文档内容
     * 
     * @param id 文档ID
     * @return 文档内容文本
     */
    String extractContent(Long id);
}
