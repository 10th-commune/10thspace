/*
 Navicat Premium Data Transfer

 Source Server         : 203
 Source Server Type    : MariaDB
 Source Server Version : 50568
 Source Host           : 192.168.88.203:3306
 Source Schema         : teamtalk

 Target Server Type    : MariaDB
 Target Server Version : 50568
 File Encoding         : 65001

 Date: 20/01/2021 10:01:34
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for IMAdmin
-- ----------------------------
DROP TABLE IF EXISTS `IMAdmin`;
CREATE TABLE `IMAdmin`  (
  `id` mediumint(6) UNSIGNED NOT NULL AUTO_INCREMENT,
  `uname` varchar(40) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户名',
  `pwd` char(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '密码',
  `status` tinyint(2) UNSIGNED NOT NULL DEFAULT 0 COMMENT '用户状态 0 :正常 1:删除 可扩展',
  `created` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '创建时间´',
  `updated` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '更新时间´',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for IMAudio
-- ----------------------------
DROP TABLE IF EXISTS `IMAudio`;
CREATE TABLE `IMAudio`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `fromId` varchar(130) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '发送者Id',
  `toId` varchar(130) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '接收者Id',
  `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '语音存储的地址',
  `size` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '文件大小',
  `duration` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '语音时长',
  `created` int(11) UNSIGNED NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_fromId_toId`(`fromId`, `toId`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for IMBlog
-- ----------------------------
DROP TABLE IF EXISTS `IMBlog`;
CREATE TABLE `IMBlog`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `groupId` varchar(66) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户的关系id',
  `userId` varchar(130) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '发送用户的id',
  `msgId` int(11) UNSIGNED NOT NULL COMMENT '消息ID',
  `content` varchar(4096) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '消息内容',
  `type` tinyint(3) UNSIGNED NOT NULL DEFAULT 2 COMMENT '群消息类型,101为群语音,2为文本',
  `status` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '消息状态',
  `updated` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '更新时间',
  `created` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_groupId_status_created`(`groupId`, `status`, `created`) USING BTREE,
  INDEX `idx_groupId_msgId_status_created`(`groupId`, `msgId`, `status`, `created`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for IMBlog_0
-- ----------------------------
DROP TABLE IF EXISTS `IMBlog_0`;
CREATE TABLE `IMBlog_0`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `relateId` int(11) UNSIGNED NOT NULL COMMENT '用户的关系id',
  `fromId` varchar(130) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '发送用户的id',
  `toId` varchar(130) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '接收用户的id',
  `msgId` int(11) UNSIGNED NOT NULL COMMENT '消息ID',
  `content` varchar(4096) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '消息内容',
  `type` tinyint(2) UNSIGNED NOT NULL DEFAULT 1 COMMENT '消息类型',
  `status` tinyint(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '0正常 1被删除',
  `updated` int(11) UNSIGNED NOT NULL COMMENT '更新时间',
  `created` int(11) UNSIGNED NOT NULL COMMENT '创建时间',
  `like_count` int(32) NULL DEFAULT 0,
  `comment_count` int(32) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_relateId_status_created`(`relateId`, `status`, `created`) USING BTREE,
  INDEX `idx_relateId_status_msgId_created`(`relateId`, `status`, `msgId`, `created`) USING BTREE,
  INDEX `idx_fromId_toId_created`(`fromId`, `toId`, `status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for IMDepart
-- ----------------------------
DROP TABLE IF EXISTS `IMDepart`;
CREATE TABLE `IMDepart`  (
  `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '部门id',
  `departName` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '部门名称',
  `priority` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '显示优先级',
  `parentId` int(11) UNSIGNED NOT NULL COMMENT '上级部门id',
  `status` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '状态',
  `created` int(11) UNSIGNED NOT NULL COMMENT '创建时间',
  `updated` int(11) UNSIGNED NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_departName`(`departName`) USING BTREE,
  INDEX `idx_priority_status`(`priority`, `status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for IMDiscovery
-- ----------------------------
DROP TABLE IF EXISTS `IMDiscovery`;
CREATE TABLE `IMDiscovery`  (
  `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'id',
  `itemName` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '名称',
  `itemUrl` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT 'URL',
  `itemPriority` int(11) UNSIGNED NOT NULL COMMENT '显示优先级',
  `status` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '状态',
  `created` int(11) UNSIGNED NOT NULL COMMENT '创建时间',
  `updated` int(11) UNSIGNED NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_itemName`(`itemName`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for IMFriendship
-- ----------------------------
DROP TABLE IF EXISTS `IMFriendship`;
CREATE TABLE `IMFriendship`  (
  `id` int(5) NOT NULL AUTO_INCREMENT COMMENT 'id自动',
  `myid` varchar(130) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `friendid` varchar(130) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '好友id',
  `req_msg` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '请求消息',
  `status` int(1) UNSIGNED ZEROFILL NULL DEFAULT 0 COMMENT '0是好友；1待添加；2拒绝添加；3被删除',
  `createtime` int(11) NULL DEFAULT NULL,
  `updatetime` int(11) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for IMGroup
-- ----------------------------
DROP TABLE IF EXISTS `IMGroup`;
CREATE TABLE `IMGroup`  (
  `id` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `name` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '群名称',
  `avatar` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '群头像',
  `creator` varchar(130) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '0' COMMENT '创建者用户id',
  `type` tinyint(3) UNSIGNED NOT NULL DEFAULT 1 COMMENT '群组类型，1-固定;2-临时群',
  `userCnt` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '成员人数',
  `status` tinyint(3) UNSIGNED NOT NULL DEFAULT 1 COMMENT '是否删除,0-正常，1-删除',
  `version` int(11) UNSIGNED NOT NULL DEFAULT 1 COMMENT '群版本号',
  `lastChated` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '最后聊天时间',
  `updated` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '更新时间',
  `created` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_name`(`name`(191)) USING BTREE,
  INDEX `idx_creator`(`creator`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = 'IM群信息' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for IMGroupMember
-- ----------------------------
DROP TABLE IF EXISTS `IMGroupMember`;
CREATE TABLE `IMGroupMember`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `groupId` varchar(66) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '群Id',
  `userId` varchar(130) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户id',
  `status` tinyint(4) UNSIGNED NOT NULL DEFAULT 1 COMMENT '是否退出群，0-正常，1-已退出',
  `created` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '创建时间',
  `updated` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_groupId_userId_status`(`groupId`, `userId`, `status`) USING BTREE,
  INDEX `idx_userId_status_updated`(`userId`, `status`, `updated`) USING BTREE,
  INDEX `idx_groupId_updated`(`groupId`, `updated`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 70 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '用户和群的关系表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for IMGroupMessage_0
-- ----------------------------
DROP TABLE IF EXISTS `IMGroupMessage_0`;
CREATE TABLE `IMGroupMessage_0`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `groupId` varchar(130) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '用户的关系id',
  `userId` varchar(130) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '发送用户的id',
  `msgId` int(11) UNSIGNED NOT NULL COMMENT '消息ID',
  `content` varchar(4096) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '消息内容',
  `type` tinyint(3) UNSIGNED NOT NULL DEFAULT 2 COMMENT '群消息类型,101为群语音,2为文本',
  `status` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '消息状态',
  `updated` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '更新时间',
  `created` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_groupId_status_created`(`groupId`, `status`, `created`) USING BTREE,
  INDEX `idx_groupId_msgId_status_created`(`groupId`, `msgId`, `status`, `created`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = 'IM群消息表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for IMGroupMessage_1
-- ----------------------------
DROP TABLE IF EXISTS `IMGroupMessage_1`;
CREATE TABLE `IMGroupMessage_1`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `groupId` varchar(130) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '用户的关系id',
  `userId` varchar(130) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '发送用户的id',
  `msgId` int(11) UNSIGNED NOT NULL COMMENT '消息ID',
  `content` varchar(4096) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '消息内容',
  `type` tinyint(3) UNSIGNED NOT NULL DEFAULT 2 COMMENT '群消息类型,101为群语音,2为文本',
  `status` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '消息状态',
  `updated` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '更新时间',
  `created` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_groupId_status_created`(`groupId`, `status`, `created`) USING BTREE,
  INDEX `idx_groupId_msgId_status_created`(`groupId`, `msgId`, `status`, `created`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = 'IM群消息表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for IMGroupMessage_2
-- ----------------------------
DROP TABLE IF EXISTS `IMGroupMessage_2`;
CREATE TABLE `IMGroupMessage_2`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `groupId` varchar(130) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '用户的关系id',
  `userId` varchar(130) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '发送用户的id',
  `msgId` int(11) UNSIGNED NOT NULL COMMENT '消息ID',
  `content` varchar(4096) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '消息内容',
  `type` tinyint(3) UNSIGNED NOT NULL DEFAULT 2 COMMENT '群消息类型,101为群语音,2为文本',
  `status` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '消息状态',
  `updated` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '更新时间',
  `created` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_groupId_status_created`(`groupId`, `status`, `created`) USING BTREE,
  INDEX `idx_groupId_msgId_status_created`(`groupId`, `msgId`, `status`, `created`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 56 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = 'IM群消息表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for IMGroupMessage_3
-- ----------------------------
DROP TABLE IF EXISTS `IMGroupMessage_3`;
CREATE TABLE `IMGroupMessage_3`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `groupId` varchar(130) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '用户的关系id',
  `userId` varchar(130) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '发送用户的id',
  `msgId` int(11) UNSIGNED NOT NULL COMMENT '消息ID',
  `content` varchar(4096) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '消息内容',
  `type` tinyint(3) UNSIGNED NOT NULL DEFAULT 2 COMMENT '群消息类型,101为群语音,2为文本',
  `status` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '消息状态',
  `updated` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '更新时间',
  `created` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_groupId_status_created`(`groupId`, `status`, `created`) USING BTREE,
  INDEX `idx_groupId_msgId_status_created`(`groupId`, `msgId`, `status`, `created`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = 'IM群消息表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for IMGroupMessage_4
-- ----------------------------
DROP TABLE IF EXISTS `IMGroupMessage_4`;
CREATE TABLE `IMGroupMessage_4`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `groupId` varchar(130) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '用户的关系id',
  `userId` varchar(130) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '发送用户的id',
  `msgId` int(11) UNSIGNED NOT NULL COMMENT '消息ID',
  `content` varchar(4096) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '消息内容',
  `type` tinyint(3) UNSIGNED NOT NULL DEFAULT 2 COMMENT '群消息类型,101为群语音,2为文本',
  `status` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '消息状态',
  `updated` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '更新时间',
  `created` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_groupId_status_created`(`groupId`, `status`, `created`) USING BTREE,
  INDEX `idx_groupId_msgId_status_created`(`groupId`, `msgId`, `status`, `created`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = 'IM群消息表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for IMGroupMessage_5
-- ----------------------------
DROP TABLE IF EXISTS `IMGroupMessage_5`;
CREATE TABLE `IMGroupMessage_5`  (
  `id` int(11) UNSIGNED ZEROFILL NOT NULL AUTO_INCREMENT,
  `groupId` varchar(130) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '用户的关系id',
  `userId` varchar(130) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '发送用户的id',
  `msgId` int(11) UNSIGNED NOT NULL COMMENT '消息ID',
  `content` varchar(4096) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '消息内容',
  `type` tinyint(3) UNSIGNED NOT NULL DEFAULT 2 COMMENT '群消息类型,101为群语音,2为文本',
  `status` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '消息状态',
  `updated` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '更新时间',
  `created` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_groupId_status_created`(`groupId`, `status`, `created`) USING BTREE,
  INDEX `idx_groupId_msgId_status_created`(`groupId`, `msgId`, `status`, `created`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = 'IM群消息表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for IMGroupMessage_6
-- ----------------------------
DROP TABLE IF EXISTS `IMGroupMessage_6`;
CREATE TABLE `IMGroupMessage_6`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `groupId` varchar(130) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '用户的关系id',
  `userId` varchar(130) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '发送用户的id',
  `msgId` int(11) UNSIGNED NOT NULL COMMENT '消息ID',
  `content` varchar(4096) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '消息内容',
  `type` tinyint(3) UNSIGNED NOT NULL DEFAULT 2 COMMENT '群消息类型,101为群语音,2为文本',
  `status` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '消息状态',
  `updated` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '更新时间',
  `created` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_groupId_status_created`(`groupId`, `status`, `created`) USING BTREE,
  INDEX `idx_groupId_msgId_status_created`(`groupId`, `msgId`, `status`, `created`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = 'IM群消息表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for IMGroupMessage_7
-- ----------------------------
DROP TABLE IF EXISTS `IMGroupMessage_7`;
CREATE TABLE `IMGroupMessage_7`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `groupId` varchar(130) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '用户的关系id',
  `userId` varchar(130) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '发送用户的id',
  `msgId` int(11) UNSIGNED NOT NULL COMMENT '消息ID',
  `content` varchar(4096) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '消息内容',
  `type` tinyint(3) UNSIGNED NOT NULL DEFAULT 2 COMMENT '群消息类型,101为群语音,2为文本',
  `status` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '消息状态',
  `updated` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '更新时间',
  `created` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_groupId_status_created`(`groupId`, `status`, `created`) USING BTREE,
  INDEX `idx_groupId_msgId_status_created`(`groupId`, `msgId`, `status`, `created`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = 'IM群消息表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for IMMessage_0
-- ----------------------------
DROP TABLE IF EXISTS `IMMessage_0`;
CREATE TABLE `IMMessage_0`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `relateId` int(11) UNSIGNED NOT NULL COMMENT '用户的关系id',
  `fromId` varchar(130) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '发送用户的id',
  `toId` varchar(130) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '接收用户的id',
  `msgId` int(11) UNSIGNED NOT NULL COMMENT '消息ID',
  `content` varchar(4096) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '消息内容',
  `type` tinyint(2) UNSIGNED NOT NULL DEFAULT 1 COMMENT '消息类型',
  `status` tinyint(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '0正常 1被删除',
  `updated` int(11) UNSIGNED NOT NULL COMMENT '更新时间',
  `created` int(11) UNSIGNED NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_relateId_status_created`(`relateId`, `status`, `created`) USING BTREE,
  INDEX `idx_relateId_status_msgId_created`(`relateId`, `status`, `msgId`, `created`) USING BTREE,
  INDEX `idx_fromId_toId_created`(`fromId`, `toId`, `status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for IMMessage_1
-- ----------------------------
DROP TABLE IF EXISTS `IMMessage_1`;
CREATE TABLE `IMMessage_1`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `relateId` int(11) UNSIGNED NOT NULL COMMENT '用户的关系id',
  `fromId` varchar(130) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '发送用户的id',
  `toId` varchar(130) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '接收用户的id',
  `msgId` int(11) UNSIGNED NOT NULL COMMENT '消息ID',
  `content` varchar(4096) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '消息内容',
  `type` tinyint(2) UNSIGNED NOT NULL DEFAULT 1 COMMENT '消息类型',
  `status` tinyint(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '0正常 1被删除',
  `updated` int(11) UNSIGNED NOT NULL COMMENT '更新时间',
  `created` int(11) UNSIGNED NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_relateId_status_created`(`relateId`, `status`, `created`) USING BTREE,
  INDEX `idx_relateId_status_msgId_created`(`relateId`, `status`, `msgId`, `created`) USING BTREE,
  INDEX `idx_fromId_toId_created`(`fromId`, `toId`, `status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for IMMessage_2
-- ----------------------------
DROP TABLE IF EXISTS `IMMessage_2`;
CREATE TABLE `IMMessage_2`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `relateId` int(11) UNSIGNED NOT NULL COMMENT '用户的关系id',
  `fromId` varchar(130) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '发送用户的id',
  `toId` varchar(130) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '接收用户的id',
  `msgId` int(11) UNSIGNED NOT NULL COMMENT '消息ID',
  `content` varchar(4096) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '消息内容',
  `type` tinyint(2) UNSIGNED NOT NULL DEFAULT 1 COMMENT '消息类型',
  `status` tinyint(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '0正常 1被删除',
  `updated` int(11) UNSIGNED NOT NULL COMMENT '更新时间',
  `created` int(11) UNSIGNED NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_relateId_status_created`(`relateId`, `status`, `created`) USING BTREE,
  INDEX `idx_relateId_status_msgId_created`(`relateId`, `status`, `msgId`, `created`) USING BTREE,
  INDEX `idx_fromId_toId_created`(`fromId`, `toId`, `status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for IMMessage_3
-- ----------------------------
DROP TABLE IF EXISTS `IMMessage_3`;
CREATE TABLE `IMMessage_3`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `relateId` int(11) UNSIGNED NOT NULL COMMENT '用户的关系id',
  `fromId` varchar(130) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '发送用户的id',
  `toId` varchar(130) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '接收用户的id',
  `msgId` int(11) UNSIGNED NOT NULL COMMENT '消息ID',
  `content` varchar(4096) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '消息内容',
  `type` tinyint(2) UNSIGNED NOT NULL DEFAULT 1 COMMENT '消息类型',
  `status` tinyint(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '0正常 1被删除',
  `updated` int(11) UNSIGNED NOT NULL COMMENT '更新时间',
  `created` int(11) UNSIGNED NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_relateId_status_created`(`relateId`, `status`, `created`) USING BTREE,
  INDEX `idx_relateId_status_msgId_created`(`relateId`, `status`, `msgId`, `created`) USING BTREE,
  INDEX `idx_fromId_toId_created`(`fromId`, `toId`, `status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for IMMessage_4
-- ----------------------------
DROP TABLE IF EXISTS `IMMessage_4`;
CREATE TABLE `IMMessage_4`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `relateId` int(11) UNSIGNED NOT NULL COMMENT '用户的关系id',
  `fromId` varchar(130) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '发送用户的id',
  `toId` varchar(130) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '接收用户的id',
  `msgId` int(11) UNSIGNED NOT NULL COMMENT '消息ID',
  `content` varchar(4096) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '消息内容',
  `type` tinyint(2) UNSIGNED NOT NULL DEFAULT 1 COMMENT '消息类型',
  `status` tinyint(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '0正常 1被删除',
  `updated` int(11) UNSIGNED NOT NULL COMMENT '更新时间',
  `created` int(11) UNSIGNED NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_relateId_status_created`(`relateId`, `status`, `created`) USING BTREE,
  INDEX `idx_relateId_status_msgId_created`(`relateId`, `status`, `msgId`, `created`) USING BTREE,
  INDEX `idx_fromId_toId_created`(`fromId`, `toId`, `status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for IMMessage_5
-- ----------------------------
DROP TABLE IF EXISTS `IMMessage_5`;
CREATE TABLE `IMMessage_5`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `relateId` int(11) UNSIGNED NOT NULL COMMENT '用户的关系id',
  `fromId` varchar(66) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '发送用户的id',
  `toId` varchar(66) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '接收用户的id',
  `msgId` int(11) UNSIGNED NOT NULL COMMENT '消息ID',
  `content` varchar(4096) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '消息内容',
  `type` tinyint(2) UNSIGNED NOT NULL DEFAULT 1 COMMENT '消息类型',
  `status` tinyint(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '0正常 1被删除',
  `updated` int(11) UNSIGNED NOT NULL COMMENT '更新时间',
  `created` int(11) UNSIGNED NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_relateId_status_created`(`relateId`, `status`, `created`) USING BTREE,
  INDEX `idx_relateId_status_msgId_created`(`relateId`, `status`, `msgId`, `created`) USING BTREE,
  INDEX `idx_fromId_toId_created`(`fromId`, `toId`, `status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for IMMessage_6
-- ----------------------------
DROP TABLE IF EXISTS `IMMessage_6`;
CREATE TABLE `IMMessage_6`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `relateId` int(11) UNSIGNED NOT NULL COMMENT '用户的关系id',
  `fromId` varchar(66) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '发送用户的id',
  `toId` varchar(66) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '接收用户的id',
  `msgId` int(11) UNSIGNED NOT NULL COMMENT '消息ID',
  `content` varchar(4096) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '消息内容',
  `type` tinyint(2) UNSIGNED NOT NULL DEFAULT 1 COMMENT '消息类型',
  `status` tinyint(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '0正常 1被删除',
  `updated` int(11) UNSIGNED NOT NULL COMMENT '更新时间',
  `created` int(11) UNSIGNED NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_relateId_status_created`(`relateId`, `status`, `created`) USING BTREE,
  INDEX `idx_relateId_status_msgId_created`(`relateId`, `status`, `msgId`, `created`) USING BTREE,
  INDEX `idx_fromId_toId_created`(`fromId`, `toId`, `status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 38 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for IMMessage_7
-- ----------------------------
DROP TABLE IF EXISTS `IMMessage_7`;
CREATE TABLE `IMMessage_7`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `relateId` int(11) UNSIGNED NOT NULL COMMENT '用户的关系id',
  `fromId` varchar(66) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '发送用户的id',
  `toId` varchar(66) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '接收用户的id',
  `msgId` int(11) UNSIGNED NOT NULL COMMENT '消息ID',
  `content` varchar(4096) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '消息内容',
  `type` tinyint(2) UNSIGNED NOT NULL DEFAULT 1 COMMENT '消息类型',
  `status` tinyint(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '0正常 1被删除',
  `updated` int(11) UNSIGNED NOT NULL COMMENT '更新时间',
  `created` int(11) UNSIGNED NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_relateId_status_created`(`relateId`, `status`, `created`) USING BTREE,
  INDEX `idx_relateId_status_msgId_created`(`relateId`, `status`, `msgId`, `created`) USING BTREE,
  INDEX `idx_fromId_toId_created`(`fromId`, `toId`, `status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for IMRecentSession
-- ----------------------------
DROP TABLE IF EXISTS `IMRecentSession`;
CREATE TABLE `IMRecentSession`  (
  `id` varchar(66) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `userId` varchar(130) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户id',
  `peerId` varchar(130) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '对方id',
  `type` tinyint(1) UNSIGNED NULL DEFAULT 0 COMMENT '类型，1-用户,2-群组',
  `status` tinyint(1) UNSIGNED NULL DEFAULT 0 COMMENT '用户:0-正常, 1-用户A删除,群组:0-正常, 1-被删除',
  `created` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '创建时间',
  `updated` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_userId_peerId_status_updated`(`userId`, `peerId`, `status`, `updated`) USING BTREE,
  INDEX `idx_userId_peerId_type`(`userId`, `peerId`, `type`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for IMRelationShip
-- ----------------------------
DROP TABLE IF EXISTS `IMRelationShip`;
CREATE TABLE `IMRelationShip`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `smallId` varchar(130) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户A的id',
  `bigId` varchar(130) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户B的id',
  `status` tinyint(1) UNSIGNED NULL DEFAULT 0 COMMENT '用户:0-正常, 1-用户A删除,群组:0-正常, 1-被删除',
  `created` bigint(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '创建时间',
  `updated` bigint(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_smallId_bigId_status_updated`(`smallId`, `bigId`, `status`, `updated`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for IMSysMsg_0
-- ----------------------------
DROP TABLE IF EXISTS `IMSysMsg_0`;
CREATE TABLE `IMSysMsg_0`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `relateId` int(11) UNSIGNED NOT NULL COMMENT '用户的关系id',
  `fromId` varchar(130) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '发送用户的id',
  `toId` varchar(130) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '接收用户的id',
  `msgId` int(11) UNSIGNED NOT NULL COMMENT '消息ID',
  `content` varchar(4096) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '消息内容',
  `type` tinyint(2) UNSIGNED NOT NULL DEFAULT 1 COMMENT '消息类型',
  `status` tinyint(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '0正常 1被删除',
  `updated` int(11) UNSIGNED NOT NULL COMMENT '更新时间',
  `created` int(11) UNSIGNED NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_relateId_status_created`(`relateId`, `status`, `created`) USING BTREE,
  INDEX `idx_relateId_status_msgId_created`(`relateId`, `status`, `msgId`, `created`) USING BTREE,
  INDEX `idx_fromId_toId_created`(`fromId`, `toId`, `status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for IMUser
-- ----------------------------
DROP TABLE IF EXISTS `IMUser`;
CREATE TABLE `IMUser`  (
  `id` varchar(135) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '用户id',
  `sex` tinyint(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '1男2女0未知',
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '用户名',
  `domain` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '拼音',
  `nick` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '花名,绰号等',
  `password` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '密码',
  `salt` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '混淆码',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '手机号码',
  `email` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT 'email',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '自定义用户头像',
  `departId` int(11) UNSIGNED NOT NULL COMMENT '所属部门Id',
  `status` tinyint(2) UNSIGNED NULL DEFAULT 0 COMMENT '1. 试用期 2. 正式 3. 离职 4.实习',
  `created` int(11) UNSIGNED NOT NULL COMMENT '创建时间',
  `updated` int(11) UNSIGNED NOT NULL COMMENT '更新时间',
  `push_shield_status` tinyint(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '0关闭勿扰 1开启勿扰',
  `sign_info` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '个性签名',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_domain`(`domain`) USING BTREE,
  INDEX `idx_name`(`name`) USING BTREE,
  INDEX `idx_phone`(`phone`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Compact;

-- ----------------------------
-- Triggers structure for table IMGroup
-- ----------------------------
DROP TRIGGER IF EXISTS `autouuid`;
delimiter ;;
CREATE TRIGGER `autouuid` BEFORE INSERT ON `IMGroup` FOR EACH ROW BEGIN
     SET new.id = UUID();
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table IMRecentSession
-- ----------------------------
DROP TRIGGER IF EXISTS `autonewid`;
delimiter ;;
CREATE TRIGGER `autonewid` BEFORE INSERT ON `IMRecentSession` FOR EACH ROW BEGIN
     SET new.id = UUID();
END
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;
