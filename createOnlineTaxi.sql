-- 创建数据库
CREATE DATABASE online_taxi;

-- 使用数据库
USE online_taxi;

-- 创建用户表
CREATE TABLE user (
                      id INT PRIMARY KEY NOT NULL COMMENT '用户ID',
                      username VARCHAR(50) NOT NULL COMMENT '用户名',
                      password VARCHAR(50) NOT NULL COMMENT '密码（加密存储）',
                      mobile VARCHAR(20) NOT NULL COMMENT '手机号',
                      email VARCHAR(50) DEFAULT NULL COMMENT '邮箱',
                      create_time DATETIME NOT NULL COMMENT '创建时间',
                      update_time DATETIME NOT NULL COMMENT '更新时间',
                      is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除（0-未删除；1-已删除；默认为0）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建司机表
CREATE TABLE driver (
                        id INT PRIMARY KEY NOT NULL COMMENT '司机ID',
                        name VARCHAR(50) NOT NULL COMMENT '司机姓名',
                        mobile VARCHAR(20) NOT NULL COMMENT '司机手机号',
                        create_time DATETIME NOT NULL COMMENT '创建时间',
                        update_time DATETIME NOT NULL COMMENT '更新时间',
                        is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除（0-未删除；1-已删除；默认为0）',
                        car_type VARCHAR(50) NOT NULL COMMENT '车辆类型',
                        car_number VARCHAR(20) NOT NULL COMMENT '车牌号'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建订单表
CREATE TABLE `order` (
                         id INT PRIMARY KEY NOT NULL COMMENT '订单ID',
                         user_id INT NOT NULL COMMENT '用户ID（外键关联）',
                         driver_id INT NOT NULL COMMENT '司机ID（外键关联）',
                         start_address VARCHAR(100) NOT NULL COMMENT '起点地址',
                         end_address VARCHAR(100) NOT NULL COMMENT '终点地址',
                         `status` TINYINT(1) NOT NULL COMMENT '订单状态（0-待接单；1-待出发；2-行驶中；3-待支付；4-已完成；5-已取消）',
                         price DECIMAL(10,2) DEFAULT NULL COMMENT '订单价格',
                         create_time DATETIME NOT NULL COMMENT '创建时间',
                         update_time DATETIME NOT NULL COMMENT '更新时间',
                         is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除（0-未删除；1-已删除；默认为0）',
                         distance DECIMAL NOT NULL COMMENT '行程距离',
                         end_time DATETIME DEFAULT NULL COMMENT '结束时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建支付表
CREATE TABLE payment (
                         id INT PRIMARY KEY NOT NULL COMMENT '支付ID',
                         order_id INT NOT NULL COMMENT '订单ID（外键关联）',
                         payment_method VARCHAR(50) NOT NULL COMMENT '支付方式',
                         amount DECIMAL(10,2) NOT NULL COMMENT '支付金额',
                         create_time DATETIME NOT NULL COMMENT '创建时间',
                         update_time DATETIME NOT NULL COMMENT '更新时间',
                         is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除（0-未删除；1-已删除；默认为0）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建评价表
CREATE TABLE review (
                        id INT PRIMARY KEY NOT NULL COMMENT '评价ID',
                        order_id INT NOT NULL COMMENT '订单ID（外键关联）',
                        rating INT NOT NULL COMMENT '评分（1-5分）',
                        comment TEXT COMMENT '评论内容',
                        create_time DATETIME NOT NULL COMMENT '创建时间',
                        update_time DATETIME NOT NULL COMMENT '更新时间',
                        is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除（0-未删除；1-已删除；默认为0）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建客服表
CREATE TABLE customer_service (
                                  id INT PRIMARY KEY NOT NULL COMMENT '客服ID',
                                  name VARCHAR(50) NOT NULL COMMENT '客服姓名',
                                  mobile VARCHAR(20) NOT NULL COMMENT '客服手机号',
                                  create_time DATETIME NOT NULL COMMENT '创建时间',
                                  update_time DATETIME NOT NULL COMMENT '更新时间',
                                  is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除（0-未删除；1-已删除；默认为0）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;