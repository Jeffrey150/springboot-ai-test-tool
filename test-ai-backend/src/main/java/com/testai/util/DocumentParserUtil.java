package com.testai.util;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文档解析工具类
 * 支持解析PDF、DOC、DOCX、MD等多种格式文档，提取文本内容和表格信息
 */
@Component
public class DocumentParserUtil {

    /** 文件上传路径 */
    @Value("${file.upload-path:/data/documents}")
    private String uploadPath;

    /**
     * 根据文件类型解析文档内容
     * 
     * @param filePath 文件路径
     * @param fileType 文件类型（PDF/DOC/DOCX/MD等）
     * @return 解析后的文本内容
     */
    public String parse(String filePath, String fileType) {
        Path path = Paths.get(filePath);

        try {
            String content;
            switch (fileType.toUpperCase()) {
                case "PDF":
                    content = parsePdfWithTable(path);
                    break;
                case "DOC":
                    content = parseDoc(path);
                    break;
                case "DOCX":
                    content = parseDocxWithTableAndImage(path);
                    break;
                case "MD":
                    content = parseMarkdownWithImage(path);
                    break;
                default:
                    content = parseDefault(path);
            }

            return content;
        } catch (IOException e) {
            throw new RuntimeException("文档解析失败: " + e.getMessage(), e);
        }
    }

    private String parsePdfWithTable(Path path) throws IOException {
        StringBuilder content = new StringBuilder();
        
        try (PDDocument document = Loader.loadPDF(path.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper() {
                private List<String> rowBuffer = new ArrayList<>();
                private float lastY = -1;
                private boolean inTable = false;
                
                @Override
                protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
                    if (textPositions.isEmpty()) return;
                    
                    float currentY = textPositions.get(0).getYDirAdj();
                    
                    if (lastY > 0 && Math.abs(currentY - lastY) > 5) {
                        if (rowBuffer.size() > 1) {
                            if (!inTable) {
                                inTable = true;
                            }
                            content.append("| ").append(String.join(" | ", rowBuffer)).append(" |\n");
                        } else if (!rowBuffer.isEmpty()) {
                            content.append(rowBuffer.get(0)).append("\n");
                            inTable = false;
                        }
                        rowBuffer.clear();
                    }
                    
                    float x = textPositions.get(0).getXDirAdj();
                    if (x > 50 && x < 550 && text.trim().length() > 0) {
                        rowBuffer.add(text.trim());
                    } else {
                        if (!rowBuffer.isEmpty()) {
                            content.append(String.join(" ", rowBuffer)).append(" ");
                            rowBuffer.clear();
                        }
                        content.append(text);
                    }
                    
                    lastY = currentY;
                }
            };
            
            content.append(stripper.getText(document));
            
            String result = content.toString();
            // 使用简单方式处理表格替换
            result = processTableMatches(result);
            
            return result;
        }
    }

    private String processTableMatches(String input) {
        Pattern pattern = Pattern.compile("\\| ([^|]+ \\|)+\\n");
        Matcher matcher = pattern.matcher(input);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String match = matcher.group();
            // 为表格行添加分隔符
            String separator = createTableSeparator(match);
            matcher.appendReplacement(result, matcher.quoteReplacement(match + separator));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }

    private String createTableSeparator(String tableRow) {
        int cellCount = (tableRow.split("\\|").length - 2);
        StringBuilder separator = new StringBuilder("|");
        for (int i = 0; i < cellCount; i++) {
            separator.append(" --- |");
        }
        return separator + "\n";
    }

    private String parseDoc(Path path) throws IOException {
        try (FileInputStream fis = new FileInputStream(path.toFile());
             HWPFDocument document = new HWPFDocument(fis)) {
            WordExtractor extractor = new WordExtractor(document);
            return extractor.getText();
        }
    }

    private String parseDocxWithTableAndImage(Path path) throws IOException {
        StringBuilder content = new StringBuilder();
        
        try (FileInputStream fis = new FileInputStream(path.toFile());
             XWPFDocument document = new XWPFDocument(fis)) {
            
            for (org.apache.poi.xwpf.usermodel.XWPFParagraph paragraph : document.getParagraphs()) {
                String text = paragraph.getText();
                if (text != null && !text.trim().isEmpty()) {
                    content.append(text).append("\n");
                }
            }
            
            for (org.apache.poi.xwpf.usermodel.XWPFTable table : document.getTables()) {
                content.append("\n");
                List<org.apache.poi.xwpf.usermodel.XWPFTableRow> rows = table.getRows();
                
                for (int i = 0; i < rows.size(); i++) {
                    org.apache.poi.xwpf.usermodel.XWPFTableRow row = rows.get(i);
                    List<org.apache.poi.xwpf.usermodel.XWPFTableCell> cells = row.getTableCells();
                    
                    StringBuilder rowContent = new StringBuilder("|");
                    for (org.apache.poi.xwpf.usermodel.XWPFTableCell cell : cells) {
                        String cellText = cell.getText().replace("\n", " ").trim();
                        rowContent.append(" ").append(cellText).append(" |");
                    }
                    content.append(rowContent).append("\n");
                    
                    if (i == 0 && rows.size() > 1) {
                        StringBuilder separator = new StringBuilder("|");
                        for (int j = 0; j < cells.size(); j++) {
                            separator.append(" --- |");
                        }
                        content.append(separator).append("\n");
                    }
                }
                content.append("\n");
            }
            
            int imageIndex = 1;
            List<org.apache.poi.xwpf.usermodel.XWPFPictureData> pictures = document.getAllPictures();
            for (org.apache.poi.xwpf.usermodel.XWPFPictureData picture : pictures) {
                String imageName = "image_" + imageIndex++ + "." + picture.suggestFileExtension();
                String imagePath = saveImage(picture.getData(), imageName);
                
                content.append("\n![图片").append(imageIndex - 1).append("](").append(imagePath).append(")\n");
            }
        }
        
        return content.toString();
    }

    private String parseMarkdownWithImage(Path path) throws IOException {
        // Java 8兼容方式读取文件
        List<String> lines = Files.readAllLines(path);
        String content = String.join("\n", lines);
        
        // 使用 Matcher 和 replace 方式处理
        StringBuffer result = new StringBuffer();
        Pattern pattern = Pattern.compile("!\\[([^\\]]*)\\]\\(([^)]+)\\)");
        Matcher matcher = pattern.matcher(content);
        
        while (matcher.find()) {
            String alt = matcher.group(1);
            String src = matcher.group(2);
            
            if (!src.startsWith("http") && !src.startsWith("data:")) {
                String imagePath = "/api/documents/images/" + Paths.get(src).getFileName();
                matcher.appendReplacement(result, "![" + alt + "](" + imagePath + ")");
            } else {
                matcher.appendReplacement(result, matcher.group());
            }
        }
        matcher.appendTail(result);
        
        return result.toString();
    }

    private String parseDefault(Path path) throws IOException {
        // 使用简单的文件读取方式
        List<String> lines = Files.readAllLines(path);
        return String.join("\n", lines);
    }

    private String saveImage(byte[] imageData, String imageName) throws IOException {
        Path imagePath = Paths.get(uploadPath, "images", imageName);
        Files.createDirectories(imagePath.getParent());
        Files.write(imagePath, imageData);
        return "/api/documents/images/" + imageName;
    }
}
