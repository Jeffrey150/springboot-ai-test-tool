package com.testai.service.impl;

import com.testai.entity.Document;
import com.testai.mapper.DocumentMapper;
import com.testai.service.DocumentService;
import com.testai.util.DocumentParserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

/**
 * 文档服务实现类
 * 实现文档的上传、查询、删除、内容提取等操作
 */
@Service
public class DocumentServiceImpl implements DocumentService {

    @Autowired
    private DocumentMapper documentMapper;

    @Autowired
    private DocumentParserUtil documentParserUtil;

    /** 文件上传路径 */
    @Value("${file.upload-path:/data/documents}")
    private String uploadPath;

    /**
     * 上传单个文档
     * 
     * @param file 文件
     * @param documentType 文档类型
     * @return 上传的文档实体
     */
    @Override
    public Document uploadDocument(MultipartFile file, String documentType) {
        String fileName = file.getOriginalFilename();
        String fileType = getFileExtension(fileName);

        String storedFileName = UUID.randomUUID().toString() + "." + fileType;
        Path storagePath = Paths.get(uploadPath, storedFileName);

        try {
            Path parentDir = storagePath.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }
            file.transferTo(storagePath.toFile());
        } catch (IOException e) {
            throw new RuntimeException("文件保存失败: " + e.getMessage(), e);
        }

        Document document = new Document();
        document.setFileName(fileName);
        document.setFilePath(storagePath.toString());
        document.setFileType(fileType);
        document.setFileSize(file.getSize());
        document.setDocumentType(documentType);
        document.setStatus(1);

        documentMapper.insert(document);
        
        // 立即查询验证是否写入成功
        Document checkDoc = documentMapper.selectById(document.getId());
        System.out.println("文档上传成功，ID: " + document.getId() + ", 文件名: " + document.getFileName());
        System.out.println("立即查询验证: " + (checkDoc != null ? "成功，文件名: " + checkDoc.getFileName() : "失败"));
        
        return document;
    }

    @Override
    public List<Document> uploadDocuments(List<MultipartFile> files, String documentType) {
        List<Document> documents = new java.util.ArrayList<>();
        for (MultipartFile file : files) {
            documents.add(uploadDocument(file, documentType));
        }
        return documents;
    }

    @Override
    public List<Document> uploadDocumentsWithTypes(List<MultipartFile> files, List<String> documentTypes) {
        List<Document> documents = new java.util.ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            String documentType = i < documentTypes.size() ? documentTypes.get(i) : "REQUIREMENT";
            documents.add(uploadDocument(files.get(i), documentType));
        }
        return documents;
    }

    @Override
    public Document getById(Long id) {
        return documentMapper.selectById(id);
    }

    @Override
    public void deleteById(Long id) {
        documentMapper.deleteById(id);
    }

    @Override
    public List<Document> listDocuments(String documentType, Integer pageNum, Integer pageSize) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Document> wrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        if (documentType != null && !documentType.isEmpty()) {
            wrapper.eq("document_type", documentType);
        }
        wrapper.orderByDesc("create_time");
        return documentMapper.selectList(wrapper);
    }

    @Override
    public String extractContent(Long id) {
        Document document = documentMapper.selectById(id);
        if (document == null) {
            throw new RuntimeException("文档不存在");
        }

        return documentParserUtil.parse(document.getFilePath(), document.getFileType());
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            throw new RuntimeException("无效的文件名");
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toUpperCase();
    }
}
