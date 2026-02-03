package com.skyeai.jarvis.knowledge.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class KnowledgeBaseService {

    @Value("${document.chunk-size:1000}")
    private int chunkSize;

    @Value("${document.overlap:100}")
    private int overlap;

    @Value("#{T(java.util.Arrays).asList('pdf', 'docx', 'txt', 'md')}")
    private List<String> supportedFormats;

    private Tika tika;

    @PostConstruct
    public void init() {
        this.tika = new Tika();
        System.out.println("KnowledgeBaseService initialized successfully");
    }

    /**
     * 创建知识库
     * @param name 知识库名称
     * @param description 知识库描述
     * @return 知识库ID
     */
    public String createKnowledgeBase(String name, String description) {
        // 实际应用中应该持久化到数据库
        String kbId = "kb_" + System.currentTimeMillis();
        System.out.println("Created knowledge base: " + kbId + " - " + name);
        return kbId;
    }

    /**
     * 上传文档到知识库
     * @param kbId 知识库ID
     * @param file 文档文件
     * @return 文档ID
     */
    public String uploadDocument(String kbId, MultipartFile file) {
        try {
            // 检查文件格式
            String fileName = file.getOriginalFilename();
            String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
            
            if (!supportedFormats.contains(fileExtension)) {
                throw new IllegalArgumentException("Unsupported file format: " + fileExtension);
            }

            // 解析文档内容
            String content = parseDocument(file);
            
            // 分块处理
            List<String> chunks = chunkDocument(content);
            
            // 生成文档ID
            String docId = "doc_" + System.currentTimeMillis();
            
            System.out.println("Uploaded document: " + docId + " to knowledge base: " + kbId);
            System.out.println("Document chunks: " + chunks.size());
            
            // 实际应用中应该将文档和分块存储到数据库和向量数据库
            
            return docId;
        } catch (Exception e) {
            System.err.println("Failed to upload document: " + e.getMessage());
            throw new RuntimeException("Failed to upload document", e);
        }
    }

    /**
     * 解析文档
     * @param file 文档文件
     * @return 文档内容
     */
    private String parseDocument(MultipartFile file) throws IOException, org.apache.tika.exception.TikaException {
        String fileName = file.getOriginalFilename();
        String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        
        try (InputStream inputStream = file.getInputStream()) {
            switch (fileExtension) {
                case "pdf":
                    return parsePdf(inputStream);
                case "docx":
                    return parseDocx(inputStream);
                case "txt":
                case "md":
                    return new String(inputStream.readAllBytes());
                default:
                    // 使用Tika解析其他格式
                    return tika.parseToString(inputStream);
            }
        }
    }

    /**
     * 解析PDF文档
     * @param inputStream 输入流
     * @return 文档内容
     */
    private String parsePdf(InputStream inputStream) throws IOException {
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    /**
     * 解析DOCX文档
     * @param inputStream 输入流
     * @return 文档内容
     */
    private String parseDocx(InputStream inputStream) throws IOException {
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            StringBuilder content = new StringBuilder();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                content.append(paragraph.getText()).append("\n");
            }
            return content.toString();
        }
    }

    /**
     * 分块处理文档
     * @param content 文档内容
     * @return 文档分块
     */
    private List<String> chunkDocument(String content) {
        List<String> chunks = new ArrayList<>();
        int length = content.length();
        int start = 0;
        
        while (start < length) {
            int end = Math.min(start + chunkSize, length);
            String chunk = content.substring(start, end);
            chunks.add(chunk);
            start += chunkSize - overlap;
        }
        
        return chunks;
    }

    /**
     * 删除知识库
     * @param kbId 知识库ID
     * @return 是否成功
     */
    public boolean deleteKnowledgeBase(String kbId) {
        // 实际应用中应该从数据库中删除
        System.out.println("Deleted knowledge base: " + kbId);
        return true;
    }

    /**
     * 获取知识库列表
     * @return 知识库列表
     */
    public List<KnowledgeBaseInfo> getKnowledgeBases() {
        // 实际应用中应该从数据库中查询
        List<KnowledgeBaseInfo> kbList = new ArrayList<>();
        KnowledgeBaseInfo kb1 = new KnowledgeBaseInfo();
        kb1.setId("kb_123");
        kb1.setName("技术文档");
        kb1.setDescription("技术相关文档集合");
        kbList.add(kb1);
        return kbList;
    }

    /**
     * 知识库信息类
     */
    public static class KnowledgeBaseInfo {
        private String id;
        private String name;
        private String description;

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
