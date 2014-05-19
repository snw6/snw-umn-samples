<?php

require_once 'database.php';

class Task {

    private $id;
    private $type;
    private $baseName;
    private $userId;
    private $files;
    private $link;

    private function __construct($link, $id, $type, $baseName, $userId) {
        $this->link = $link;
        $this->id = $id;
        assert($type == 'merge' || $type == 'split');
        $this->type = $type;
        $this->baseName = $baseName;
        $this->userId = $userId;
        $this->files = array();
    }

    public static function createTask($link, $type, $baseName, $user = null) {

        assert($type == 'merge' || $type == 'split');

        $userId = $user ? $user->getId() : 0;
        $query = "
            INSERT INTO Tasks (type, userId, baseName) 
            VALUES('$type', '$userId', '$baseName')";
        $result = mysqli_query($link, $query);
        if(!$result) {
            throw new Exception(mysqli_error($link));
        }

        $id = mysqli_insert_id($link);
        assert($id > 0);

        return new Task($link, $id, $type, $baseName, $userId);
    }

    public static function getTask($link, $id) {

        $query = "SELECT type, userId, baseName FROM Tasks WHERE id = '$id'";
        $result = mysqli_query($link, $query);
        if(!$result) {
            throw new Exception(mysqli_error($link));
        }
        if(mysqli_num_rows($result) == 0) {
            throw new Exception("This taskId does not exist");
        }
        $row = mysqli_fetch_assoc($result);
        $type = $row['type'];
        $userId = $row['userId'];
        $baseName = $row['baseName'];

        $task = new Task($link, $id, $type, $baseName, $userId);
        
        // get the files for this task
        $query = "SELECT path FROM Files WHERE taskId = '$id'";
        $result = mysqli_query($link, $query);
        if(!$result) {
            throw new Exception(mysqli_error($link));
        }

        assert(mysqli_num_rows($result) > 0);
        
        while($row = mysqli_fetch_assoc($result)) {
            $task->addFile($row['path']);
        }

        return $task;
    }

    public function getId() {
        return $this->id;
    }

    public function getFiles() {
        return $this->files;
    }

    public function getType() {
        return $this->type;
    }

    public function getBaseName() {
        return $this->baseName;
    }

    public function save() {

        // TODO delete existing entries?

        $values = array();
        foreach($this->files as $id => $file) {
            $taskId = $this->id;
            $values[] = "($id, $taskId, '$file')";
        }

        $query = "INSERT INTO Files (id, taskId, path) VALUES " . implode(',', $values) . ";";
        $result = mysqli_query($this->link, $query);
        if(!$result) {
            throw new Exception(mysqli_error($this->link));
        }

    }

    public function addFile($file) {
        $this->files[] = $file; 
    }

    public function getFileCount() {
        return count($this->files);
    }
}

?>
