DROP USER IF EXISTS db_user;
CREATE USER db_user IDENTIFIED BY '3<Nw=j$[uBse,3{g';

DROP DATABASE IF EXISTS `parameters`;
CREATE DATABASE `parameters`;
GRANT ALL PRIVILEGES ON `parameters`.* TO db_user;