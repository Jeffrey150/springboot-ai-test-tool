-- 初始化数据库脚本
-- 数据库: testai

-- 创建测试示例表
CREATE TABLE IF NOT EXISTS `test_demo` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '名称',
  `value` varchar(200) DEFAULT NULL COMMENT '值',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试示例表';

-- 插入测试数据
INSERT INTO `test_demo` (`name`, `value`, `description`) VALUES
('测试数据1', 'value001', '这是第一条测试数据'),
('测试数据2', 'value002', '这是第二条测试数据'),
('示例A', 'demoA', '示例数据A的描述信息'),
('示例B', 'demoB', '示例数据B的描述信息'),
('功能验证', 'verify', '用于前后端联调验证的数据');
