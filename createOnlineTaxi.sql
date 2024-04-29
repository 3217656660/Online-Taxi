-- 创建数据库
CREATE DATABASE online_taxi;

-- 使用数据库
USE online_taxi;

-- 创建用户表
CREATE TABLE `user` (
                        `id` INT UNSIGNED PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '用户ID',
                        `username` VARCHAR(50) NOT NULL COMMENT '用户名',
                        `password` VARCHAR(200) NOT NULL COMMENT '密码（加密存储）',
                        `mobile` VARCHAR(20) NOT NULL COMMENT '手机号',
                        `email` VARCHAR(50) DEFAULT NULL COMMENT '邮箱',
                        `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        `version` INT UNSIGNED NOT NULL DEFAULT 1 COMMENT '乐观锁',
                        `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除（0-未删除；1-已删除；默认为0）',
                        INDEX idx_mobile(mobile),
                        INDEX idx_is_deleted(is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建司机表
CREATE TABLE `driver` (
                          `id` INT UNSIGNED PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '司机ID',
                          `username` VARCHAR(50) NOT NULL COMMENT '司机姓名',
                          `mobile` VARCHAR(20) NOT NULL COMMENT '司机手机号',
                          `password` VARCHAR(200) NOT NULL COMMENT '密码（加密存储）',
                          `email` VARCHAR(50) DEFAULT NULL COMMENT '邮箱',
                          `car_type` VARCHAR(50) NOT NULL COMMENT '车辆类型',
                          `car_number` VARCHAR(20) NOT NULL COMMENT '车牌号',
                          `latitude` DECIMAL(10, 8)  COMMENT '司机当前位置经度',
                          `longitude` DECIMAL(11, 8) COMMENT '司机当前位置纬度',
                          `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                          `version` INT UNSIGNED NOT NULL DEFAULT 1 COMMENT '乐观锁',
                          `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除（0-未删除；1-已删除；默认为0）',
                          INDEX idx_mobile(mobile),
                          INDEX idx_is_deleted(is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建订单表
CREATE TABLE `order` (
                         `id` INT UNSIGNED PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '订单ID',
                         `user_id` INT UNSIGNED NOT NULL COMMENT '用户ID',
                         `driver_id` INT UNSIGNED COMMENT '司机ID',
                         `start_address` VARCHAR(100) NOT NULL COMMENT '起点地址',
                         `start_latitude` DECIMAL(10, 8) NOT NULL COMMENT '起点地址经度',
                         `start_longitude` DECIMAL(11, 8) NOT NULL COMMENT '起点地址纬度',
                         `end_address` VARCHAR(100) NOT NULL COMMENT '终点地址',
                         `end_latitude` DECIMAL(10, 8) NOT NULL COMMENT '终点地址经度',
                         `end_longitude` DECIMAL(11, 8) NOT NULL COMMENT '终点地址纬度',
                         `status` TINYINT(1) NOT NULL COMMENT '订单状态（0-待接单；1-待出发；2-行驶中；3-待支付；4-已完成；5-已取消）',
                         `price` DECIMAL(10,2) DEFAULT NULL COMMENT '订单价格',
                         `distance` DECIMAL(10,2) NOT NULL COMMENT '行程距离',
                         `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
                         `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                         `version` INT UNSIGNED NOT NULL DEFAULT 1 COMMENT '乐观锁',
                         `user_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '用户是否删除（0-未删除；1-已删除；默认为0）',
                         `driver_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '司机是否删除（0-未删除；1-已删除；默认为0）',
                         INDEX idx_user_id(user_id),
                         INDEX idx_driver_id(driver_id),
                         INDEX idx_user_deleted(user_deleted),
                         INDEX idx_driver_deleted(driver_deleted),
                         CONSTRAINT fk_order_user_id FOREIGN KEY (user_id) REFERENCES user(id),
                         CONSTRAINT fk_order_driver_id FOREIGN KEY (driver_id) REFERENCES driver(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建支付表
CREATE TABLE `payment` (
                           `id` INT UNSIGNED PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '支付ID',
                           `order_id` INT UNSIGNED NOT NULL COMMENT '订单ID',
                           `user_id` INT UNSIGNED NOT NULL COMMENT '用户ID',
                           `payment_method` VARCHAR(50) NOT NULL COMMENT '支付方式',
                           `amount` DECIMAL(10,2) NOT NULL COMMENT '支付金额',
                           `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           `version` INT UNSIGNED NOT NULL DEFAULT 1 COMMENT '乐观锁',
                           `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除（0-未删除；1-已删除；默认为0）',
                           INDEX idx_order_id(order_id),
                           INDEX idx_user_id(user_id),
                           INDEX idx_is_deleted(is_deleted),
                           CONSTRAINT fk_payment_order_id FOREIGN KEY (order_id) REFERENCES `order`(id),
                           CONSTRAINT fk_payment_user_id FOREIGN KEY (user_id) REFERENCES user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建评价表
CREATE TABLE `review` (
                          `id` INT UNSIGNED PRIMARY KEY AUTO_INCREMENT NOT NULL COMMENT '评价ID',
                          `order_id` INT UNSIGNED NOT NULL COMMENT '订单ID',
                          `user_id` INT UNSIGNED NOT NULL COMMENT '用户ID',
                          `rating` INT UNSIGNED NOT NULL COMMENT '评分（1-5分）',
                          `comment` TEXT COMMENT '评论内容',
                          `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                          `version` INT UNSIGNED NOT NULL DEFAULT 1 COMMENT '乐观锁',
                          `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除（0-未删除；1-已删除；默认为0）',
                          INDEX idx_order_id(order_id),
                          INDEX idx_user_id(user_id),
                          INDEX idx_is_deleted(is_deleted),
                          CONSTRAINT fk_review_order_id FOREIGN KEY (order_id) REFERENCES `order`(id),
                          CONSTRAINT fk_review_user_id FOREIGN KEY (user_id) REFERENCES user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;