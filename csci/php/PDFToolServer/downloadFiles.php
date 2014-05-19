<?php
/**
 * @author Roman Dovgopol
 * @version 0.1
 * @license cc-by-sa
 * @date Spring2014
 */

    require_once 'task.php';
    require_once 'file.php';
    require_once 'database.php';
    require_once 'file.php';
    $db = new database();
    $link = $db->getLink();
    $fi = new file;

    define("LOGS", getcwd() . "/logs/API.log");

    error_log($_SERVER['REQUEST_URI']);

    function failure($message) {
        error_log($message);
        header($message, true, 404);
        die();
    }

    if (!isset($_GET['taskId'])) {
        failure("No taskId");
    }
    $taskId = $_GET['taskId'];

    // get the task object
    try {
        $task = Task::getTask($link, $taskId);
    } catch(Exception $e) {
        failure($e->getMessage());
    }

    $taskType = $task->getType();
    $baseName = $task->getBaseName();

    $files = $task->getFiles();
    if($taskType == 'merge') {
        assert(count($files == 1));
        $path = $files[0];
        $name = "$baseName.pdf";
        $contentType = 'application/pdf';
    } else {
        assert(count($files > 1));

        // Create a ZIP containing all of the PDFs
        $path = "$taskId.zip";
        if(file::zip($path, $files, $baseName) == false) {
            failure("Failed to create ZIP file");
        }
        $name = "$baseName.zip";
        $contentType = 'application/zip';
    }
    $size = filesize($path);

    header($_SERVER["SERVER_PROTOCOL"] . " 200 OK");
    header("Cache-Control: public");
    header("Content-Type: $contentType");
    header("Content-Transfer-Encoding: Binary");
    header("Content-Length: $size");
    header("Content-Disposition: attachment; filename=$name");
    readfile($path);

?>
