<?php

require_once 'task.php';

class User {

    private $id;
    private $link;

    public function __construct($link, $token) {

        error_log($token);

        // use the token to access basic user information
        $url = "https://www.googleapis.com/oauth2/v1/userinfo?access_token=" . urlencode($token);
        $json = file_get_contents($url);

        if($json === false) {
            throw new Exception("Unable to retrieve user info");
        }

        $userInfo = json_decode($json);

        if(!isset($userInfo->id)) {
            throw new Exception("Invalid token");
        }

        $googleId = $userInfo->id;
        error_log("googleId: $googleId");

        $query = "SELECT id FROM Users WHERE googleId = '$googleId'";
        error_log($query);
        $result = mysqli_query($link, $query);
        if(!$result) {
            throw new Exception(mysqli_error($link));
        }

        assert(mysqli_num_rows($result) <= 1);

        if(mysqli_num_rows($result) == 1) {
            $row = mysqli_fetch_assoc($result);
            $id = $row['id'];
        } else {
            // create a new user
            $query = "INSERT INTO Users (googleId) VALUES('$googleId')";
            $result = mysqli_query($link, $query);
            if(!$result) {
                throw new Exception("Unable to save user");
            }
            $id = mysqli_insert_id($link);
        }

        $this->id = $id;
        $this->link = $link;
    }

    public function getId() {
        return $this->id;
    }

    public function getTasks() {
        $link = $this->link;
        $query = "SELECT id FROM Tasks WHERE userId = '{$this->id}'";
        error_log($query);
        $result = mysqli_query($link, $query);

        if(!$result) {
            throw new Exception(mysqli_error($link));
        }

        $tasks = array();
        while($row = mysqli_fetch_assoc($result)) {
            $taskId = $row['id'];
            $tasks[] = Task::getTask($link, $taskId);
        }
        return $tasks;
    }

}
