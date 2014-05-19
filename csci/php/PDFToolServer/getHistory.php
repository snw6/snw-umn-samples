<?php

require_once 'user.php';
require_once 'database.php';

$db = new database();
$link = $db->getLink();

$user = new User($link, $_POST['token']);
$tasks = $user->getTasks();

$result = array();
foreach($tasks as $task) {
    $result[] = array(
        "baseName" => $task->getBaseName(),
        "taskId" => $task->getId(),
        "type" => $task->getType(),
    );
}

die(json_encode($result));


?>
