CREATE DATABASE IF NOT EXISTS giavico_formula;
CREATE DATABASE IF NOT EXISTS giavico_inventory;
CREATE DATABASE IF NOT EXISTS giavico_chat;

GRANT ALL PRIVILEGES ON giavico_formula.* TO 'giavico'@'%';
GRANT ALL PRIVILEGES ON giavico_inventory.* TO 'giavico'@'%';
GRANT ALL PRIVILEGES ON giavico_chat.* TO 'giavico'@'%';
FLUSH PRIVILEGES;
