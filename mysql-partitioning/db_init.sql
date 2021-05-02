DROP DATABASE IF EXISTS partitioning;
DROP USER IF EXISTS lab_user;

CREATE USER lab_user IDENTIFIED BY 'kL74pmb#h96Y%2^';

-- Create database and user
CREATE DATABASE `partitioning`;

USE partitioning;

GRANT ALL PRIVILEGES ON partitioning.* TO lab_user;

-- Create table without partitioning
CREATE TABLE IF NOT EXISTS `params_no_part`
(
    `id`    INTEGER     NOT NULL AUTO_INCREMENT,
    `code`  VARCHAR(64) NOT NULL,
    `value` VARCHAR(64) NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB;

-- Create table with partitioning
CREATE TABLE IF NOT EXISTS `params_partitions`
(
    `id`    INTEGER     NOT NULL AUTO_INCREMENT,
    `code`  VARCHAR(64) NOT NULL,
    `value` VARCHAR(64) NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
    PARTITION BY HASH (`id`)
        PARTITIONS 4;
