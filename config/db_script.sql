CREATE DATABASE  IF NOT EXISTS `idservice`;
USE `idservice`;

DROP TABLE IF EXISTS `bulkJob`;
CREATE TABLE `bulkJob` (
    `id`                                           INT AUTO_INCREMENT PRIMARY KEY,
    `name`                                         VARCHAR(255)                                     NULL,
    `status`                                       VARCHAR(1)                                       NULL,
    `request`                                      LONGTEXT                                         NULL,
    `created_at`                                   DATETIME                                         NULL,
    `modified_at`                                  DATETIME                                         NULL,
    `log`                                          VARCHAR(1000)                                    NULL
) CHARSET = `utf8`;

DROP TABLE IF EXISTS `namespace`;
CREATE TABLE `namespace` (
    `namespace`                                    INT                            DEFAULT 0         NOT NULL PRIMARY KEY,
    `organizationName`                             VARCHAR(255)                                     NULL,
    `organizationAndContactDetails`                VARCHAR(2000)                                    NULL,
    `dateIssued`                                   DATETIME                                         NULL,
    `email`                                        VARCHAR(255)                                     NULL,
    `notes`                                        VARCHAR(2000)                                    NULL,
    `idPregenerate`                                VARCHAR(1)                                       NULL
) CHARSET = `utf8`;

DROP TABLE IF EXISTS `partitions`;
CREATE TABLE `partitions` (
    `namespace`                                    INT                            DEFAULT 0         NOT NULL,
    `partitionId`                                  VARCHAR(2)                     DEFAULT ''        NOT NULL,
    `sequence`                                     BIGINT                                           NULL,
    PRIMARY KEY (`namespace`, `partitionId`)
) CHARSET = `utf8`;

DROP TABLE IF EXISTS `permissionsNamespace`;
CREATE TABLE `permissionsNamespace` (
    `namespace`                                    INT                            DEFAULT 0         NOT NULL,
    `username`                                     VARCHAR(255)                   DEFAULT ''        NOT NULL,
    `role`                                         VARCHAR(255)                                     NULL,
    PRIMARY KEY (`namespace`, `username`)
) CHARSET = `utf8`;

DROP TABLE IF EXISTS `permissionsScheme`;
CREATE TABLE `permissionsScheme` (
    `scheme`                                       VARCHAR(160)                   DEFAULT ''        NOT NULL,
    `username`                                     VARCHAR(160)                   DEFAULT ''        NOT NULL,
    `role`                                         VARCHAR(255)                                     NULL,
    PRIMARY KEY (`scheme`, `username`)
) CHARSET = `utf8`;

DROP TABLE IF EXISTS `schemeId`;
CREATE TABLE `schemeId` (
    `scheme`                                       VARCHAR(18)                    DEFAULT ''        NOT NULL,
    `schemeId`                                     VARCHAR(18) COLLATE `utf8_bin` DEFAULT ''        NOT NULL,
    `sequence`                                     BIGINT                                           NULL,
    `checkDigit`                                   TINYINT                                          NULL,
    `systemId`                                     VARCHAR(255)                                     NOT NULL,
    `status`                                       VARCHAR(20)                                      NULL,
    `author`                                       VARCHAR(255)                                     NULL,
    `software`                                     VARCHAR(255)                                     NULL,
    `expirationDate`                               DATE                                             NULL,
    `comment`                                      VARCHAR(255)                                     NULL,
    `jobId`                                        INT                                              NULL,
    `created_at`                                   DATETIME                                         NULL,
    `modified_at`                                  DATETIME                                         NULL,
    PRIMARY KEY (`scheme`, `schemeId`),
    CONSTRAINT `sysId`
        UNIQUE (`systemId`, `scheme`)
) CHARSET = `utf8`;

CREATE INDEX `jobId` ON `schemeId` (`jobId`);
CREATE INDEX `stat` ON `schemeId` (`status`, `scheme`);

DROP TABLE IF EXISTS `schemeId_log`;
CREATE TABLE `schemeId_log` (
    `scheme`                                       VARCHAR(18)                    DEFAULT ''        NOT NULL,
    `schemeId`                                     VARCHAR(18) COLLATE `utf8_bin` DEFAULT ''        NOT NULL,
    `sequence`                                     BIGINT                                           NULL,
    `checkDigit`                                   TINYINT                                          NULL,
    `systemId`                                     VARCHAR(255)                                     NOT NULL,
    `status`                                       VARCHAR(20)                                      NULL,
    `author`                                       VARCHAR(255)                                     NULL,
    `software`                                     VARCHAR(255)                                     NULL,
    `expirationDate`                               DATE                                             NULL,
    `comment`                                      VARCHAR(255)                                     NULL,
    `jobId`                                        INT                                              NULL,
    `created_at`                                   DATETIME                                         NULL,
    `modified_at`                                  DATETIME                                         NULL
) CHARSET = `utf8`;

CREATE INDEX `job_schemeId_log` ON `schemeId_log` (`jobId`);

DROP TABLE IF EXISTS `schemeIdBase`;
CREATE TABLE `schemeIdBase` (
    `scheme`                                       VARCHAR(18)                    DEFAULT ''        NOT NULL PRIMARY KEY,
    `idBase`                                       VARCHAR(18)                                      NULL
) CHARSET = `utf8`;

DROP TABLE IF EXISTS `sctId`;
CREATE TABLE `sctId` (
    `sctid`                                        VARCHAR(18) DEFAULT ''                           NOT NULL PRIMARY KEY,
    `sequence`                                     BIGINT                                           NULL,
    `namespace`                                    INT                                              NULL,
    `partitionId`                                  VARCHAR(2)                                       NULL,
    `checkDigit`                                   TINYINT                                          NULL,
    `systemId`                                     VARCHAR(255)                                     NOT NULL,
    `status`                                       VARCHAR(20)                                      NULL,
    `author`                                       VARCHAR(255)                                     NULL,
    `software`                                     VARCHAR(255)                                     NULL,
    `expirationDate`                               DATE                                             NULL,
    `comment`                                      VARCHAR(255)                                     NULL,
    `jobId`                                        INT                                              NULL,
    `created_at`                                   DATETIME                                         NULL,
    `modified_at`                                  DATETIME                                         NULL,
    CONSTRAINT `sysid` UNIQUE (`systemId`)
) CHARSET = `utf8`;

CREATE INDEX `jobId` ON `sctId` (`jobId`);
CREATE INDEX `nam_par_st` ON `sctId` (`namespace`, `partitionId`, `status`);
CREATE INDEX `stat` ON `sctId` (`status`);

DROP TABLE IF EXISTS `sctId_log`;
CREATE TABLE `sctId_log` (
    `sctid`                                        VARCHAR(18)                                      NOT NULL DEFAULT '',
    `sequence`                                     BIGINT                                           NULL,
    `namespace`                                    INT                                              NULL,
    `partitionId`                                  VARCHAR(2)                                       NULL,
    `checkDigit`                                   TINYINT                                          NULL,
    `systemId`                                     VARCHAR(255)                                     NOT NULL,
    `status`                                       VARCHAR(20)                                      NULL,
    `author`                                       VARCHAR(255)                                     NULL,
    `software`                                     VARCHAR(255)                                     NULL,
    `expirationDate`                               DATE                                             NULL,
    `comment`                                      VARCHAR(255)                                     NULL,
    `jobId`                                        INT                                              NULL,
    `created_at`                                   DATETIME                                         NULL,
    `modified_at`                                  DATETIME                                         NULL
) CHARSET = `utf8`;

CREATE INDEX `job_sctId_log` ON `sctId_log` (`jobId`);

DROP TABLE IF EXISTS `pwi_import`;
CREATE TABLE `pwi_import` (
    `sctid`                                        VARCHAR(18)                    DEFAULT NULL,
    `current_status`                               VARCHAR(20)                    DEFAULT NULL,
    `new_status`                                   VARCHAR(20)                    DEFAULT NULL,
    KEY `idx_id` (`sctid`)
) DEFAULT CHARSET = `latin1`;

DROP TABLE IF EXISTS `pwi_deleted_ids`;
CREATE TABLE `pwi_deleted_ids` (
    `sctid`                                        VARCHAR(18)                    DEFAULT NULL,
    KEY `idx_del_id` (`sctid`)
) DEFAULT CHARSET = `latin1`;
