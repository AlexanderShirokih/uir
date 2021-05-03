DROP USER IF EXISTS repl_master;
CREATE USER repl_master IDENTIFIED BY 'repl_master_pass';
GRANT REPLICATION SLAVE ON *.* TO repl_master;