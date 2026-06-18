CREATE DATABASE IF NOT EXISTS giavico_formula;
CREATE DATABASE IF NOT EXISTS giavico_inventory;
CREATE DATABASE IF NOT EXISTS giavico_chat;
<<<<<<< HEAD
=======
CREATE DATABASE IF NOT EXISTS giavico_rnd_documents;
>>>>>>> 3cc04d2 (Migrate project to mircoservices)

GRANT ALL PRIVILEGES ON giavico_formula.* TO 'giavico'@'%';
GRANT ALL PRIVILEGES ON giavico_inventory.* TO 'giavico'@'%';
GRANT ALL PRIVILEGES ON giavico_chat.* TO 'giavico'@'%';
<<<<<<< HEAD
=======
GRANT ALL PRIVILEGES ON giavico_rnd_documents.* TO 'giavico'@'%';
>>>>>>> 3cc04d2 (Migrate project to mircoservices)
FLUSH PRIVILEGES;
