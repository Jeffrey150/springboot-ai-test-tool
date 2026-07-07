-- ===========================================
-- 测试用例生成平台 - 数据库初始化脚本
-- 请在 testai 数据库中执行此脚本
-- ===========================================

-- 1. 文档表 - 存储上传的需求文档和设计文档
CREATE TABLE IF NOT EXISTS document (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    file_name VARCHAR(255) NOT NULL COMMENT '文件名',
    file_path VARCHAR(500) NOT NULL COMMENT '文件存储路径',
    file_type VARCHAR(50) NOT NULL COMMENT '文件类型（PDF/DOC/DOCX）',
    file_size BIGINT COMMENT '文件大小（字节）',
    document_type VARCHAR(50) COMMENT '文档类型（REQUIREMENT-需求文档，DESIGN-设计文档）',
    content LONGTEXT COMMENT '文档内容（解析后）',
    status INT DEFAULT 0 COMMENT '状态（0-禁用，1-启用）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_document_type (document_type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档表';

-- 2. 模板表 - 存储测试用例生成模板
CREATE TABLE IF NOT EXISTS template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    template_name VARCHAR(100) NOT NULL COMMENT '模板名称',
    template_type VARCHAR(50) COMMENT '模板类型（功能测试/性能测试/安全测试）',
    content LONGTEXT NOT NULL COMMENT '模板内容（Markdown格式）',
    description VARCHAR(500) COMMENT '模板描述',
    status INT DEFAULT 1 COMMENT '状态（0-禁用，1-启用）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_template_name (template_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板表';

-- 3. 任务表 - 存储测试用例生成任务
CREATE TABLE IF NOT EXISTS task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    document_id BIGINT NOT NULL COMMENT '关联文档ID（单文档模式）',
    document_ids VARCHAR(1000) COMMENT '关联文档ID列表（多文档模式，逗号分隔）',
    document_type VARCHAR(50) COMMENT '文档类型',
    task_name VARCHAR(200) COMMENT '任务名称',
    task_no VARCHAR(100) COMMENT '任务号',
    task_status INT DEFAULT 0 COMMENT '任务状态（0-待处理，1-进行中，2-已完成，3-失败）',
    template_id BIGINT COMMENT '关联模板ID',
    config TEXT COMMENT 'AI配置JSON',
    start_time DATETIME COMMENT '任务开始时间',
    end_time DATETIME COMMENT '任务结束时间',
    duration INT COMMENT '耗时（秒）',
    result_count INT COMMENT '生成用例数',
    quality_score DECIMAL(5,2) COMMENT '质量评分',
    feedback TEXT COMMENT '用户反馈',
    ai_response LONGTEXT COMMENT 'AI原始响应内容',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_document_id (document_id),
    INDEX idx_task_status (task_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务表';

-- 4. 插入默认模板数据
INSERT IGNORE INTO template (template_name, template_type, content, description, status) VALUES 
('功能测试标准模板', '功能测试', '## 功能测试用例模板\n\n### 用例结构\n- 用例名称：清晰描述测试目标\n- 前置条件：执行测试前的必要条件\n- 操作步骤：详细的操作流程\n- 预期结果：期望的系统响应\n- 优先级：1-高，2-中，3-低\n- 用例类型：功能测试/边界测试/异常测试\n\n### 生成规则\n1. 每个功能点至少生成3个用例\n2. 操作步骤必须包含具体的输入值\n3. 预期结果必须可验证\n4. 边界值测试必须包含边界值本身', '标准的功能测试用例模板，适用于大多数功能测试场景', 1);
