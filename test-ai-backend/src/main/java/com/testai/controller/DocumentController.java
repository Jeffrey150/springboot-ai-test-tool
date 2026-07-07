package com.testai.controller;

import com.testai.common.Result;
import com.testai.entity.Document;
import com.testai.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * 文档管理控制器
 * 负责文档的上传、查询、删除、内容预览等操作
 */
@RestController
@RequestMapping("/documents")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    /** 文件上传路径 */
    @Value("${file.upload-path:/data/documents}")
    private String uploadPath;

    /**
     * 批量上传文档（统一类型）
     * 
     * @param files 文件列表
     * @param documentType 文档类型，默认为 REQUIREMENT
     * @return 上传的文档列表
     */
    @PostMapping("/upload")
    public Result<List<Document>> upload(
            @RequestParam("file") List<MultipartFile> files,
            @RequestParam(value = "documentType", defaultValue = "REQUIREMENT") String documentType) {
        return Result.success(documentService.uploadDocuments(files, documentType));
    }

    /**
     * 批量上传文档，支持为每个文档指定不同类型
     * 
     * @param files 文件列表
     * @param documentTypes 文档类型列表，与文件列表一一对应
     * @return 上传的文档列表
     */
    @PostMapping("/upload-with-types")
    public Result<List<Document>> uploadWithTypes(
            @RequestParam("file") List<MultipartFile> files,
            @RequestParam("documentTypes") List<String> documentTypes) {
        return Result.success(documentService.uploadDocumentsWithTypes(files, documentTypes));
    }

    /**
     * 根据ID查询文档详情
     * 
     * @param id 文档ID
     * @return 文档实体
     */
    @GetMapping("/{id}")
    public Result<Document> getById(@PathVariable Long id) {
        return Result.success(documentService.getById(id));
    }

    /**
     * 删除文档
     * 
     * @param id 文档ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        documentService.deleteById(id);
        return Result.success();
    }

    /**
     * 查询文档列表
     * 
     * @param documentType 文档类型筛选（可选）
     * @param pageNum 页码（可选）
     * @param pageSize 每页数量（可选）
     * @return 文档列表
     */
    @GetMapping("/list")
    public Result<List<Document>> list(
            @RequestParam(required = false) String documentType,
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize) {
        return Result.success(documentService.listDocuments(documentType, pageNum, pageSize));
    }

    /**
     * 获取文档内容
     * 
     * @param id 文档ID
     * @return 文档内容文本
     */
    @GetMapping("/{id}/content")
    public Result<String> getContent(@PathVariable Long id) {
        return Result.success(documentService.extractContent(id));
    }

    /**
     * 获取图片资源
     * 
     * @param fileName 图片文件名
     * @return 图片资源
     */
    @GetMapping("/images/{fileName}")
    public ResponseEntity<Resource> getImage(@PathVariable String fileName) throws IOException {
        Path imagePath = Paths.get(uploadPath, "images", fileName);
        
        if (!Files.exists(imagePath)) {
            return ResponseEntity.notFound().build();
        }
        
        Resource resource = new FileSystemResource(imagePath);
        String contentType = Files.probeContentType(imagePath);
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType != null ? contentType : "application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .body(resource);
    }

    /**
     * 获取文档预览（HTML格式）
     * 
     * @param id 文档ID
     * @return HTML格式的文档预览内容
     */
    @GetMapping("/{id}/preview")
    public Result<String> getPreview(@PathVariable Long id) {
        String content = documentService.extractContent(id);
        String htmlContent = convertToHtml(content);
        return Result.success(htmlContent);
    }

    /**
     * 将文档内容转换为HTML格式
     * 
     * @param content 文档内容
     * @return HTML格式内容
     */
    private String convertToHtml(String content) {
        if (content == null) return "";
        
        StringBuilder html = new StringBuilder("<div class=\"document-content\">");
        
        String[] lines = content.split("\n");
        boolean inTable = false;
        
        for (String line : lines) {
            if (line.startsWith("|")) {
                if (!inTable) {
                    html.append("<table><tbody>");
                    inTable = true;
                }
                
                if (line.matches("\\|\\s*[-]+\\s*\\|.*")) {
                    continue;
                }
                
                html.append("<tr>");
                String[] cells = line.split("\\|");
                for (int i = 1; i < cells.length - 1; i++) {
                    html.append("<td>").append(escapeHtml(cells[i].trim())).append("</td>");
                }
                html.append("</tr>");
            } else if (inTable) {
                html.append("</tbody></table>");
                inTable = false;
                html.append("<p>").append(escapeHtml(line)).append("</p>");
            } else if (line.startsWith("![")) {
                String imageTag = line.replace("![", "<img alt=\"").replace("](", "\" src=\"").replace(")", "\" />");
                html.append(imageTag);
            } else {
                html.append("<p>").append(escapeHtml(line)).append("</p>");
            }
        }
        
        if (inTable) {
            html.append("</tbody></table>");
        }
        
        html.append("</div>");
        return html.toString();
    }

    /**
     * HTML转义
     * 
     * @param text 原始文本
     * @return 转义后的文本
     */
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;");
    }
}
