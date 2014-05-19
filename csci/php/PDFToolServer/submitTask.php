<?php
/**
 * @author Roman Dovgopol
 * @version 0.3
 * @license cc-by-sa
 * @date Spring2014
 */

    require_once 'task.php';
    require_once 'user.php';

    require_once 'database.php';
    require_once 'PDF.php';
    require_once 'file.php';
    $db = new database();
    $link = $db->getLink();
    $fi = new file;

    function success($taskId, $fileCount, $baseName) {
        $result = array(
            'statusCode' => 0,
            'taskId' => $taskId,
            'fileCount' => $fileCount, 
            'baseName' => $baseName,
        ); 
        die(json_encode($result));
    }

    function failure($error) {
        $result = array(
            "statusCode" => 1,
            "error" => $error,
        );
        die(json_encode($result));
    }


    define("LOGS", getcwd() . "/logs/API.log");

    if (!isset($_POST['user_agent'], $_POST['task_type'])) {
        failure("no userAgent or taskType");
    }

    $user_agent = $_POST['user_agent'];
    $taskType = $_POST['task_type'];

    $token = isset($_POST['token']) ? $_POST['token'] : null;

    if(isset($_POST['token'])) {
        try {
            $user = new user($link, $_POST['token']);
        } catch(Exception $e) {
            error_log($e->getMessage());
            $user = NULL;
        }
    }

    if($user_agent != 'android') {
        failure("invalid userAgent");
    }

    if($taskType == 'merge') {
        if(!isset($_FILES['files']) or count($_FILES['files']['name']) < 2) {
            failure("invalid files for merge");
        }
    } else if($taskType == 'split') {
        if(!isset($_FILES['files']) or count($_FILES['files']['name']) != 1) {
            failure("invalid file for split");
        }
    } else {
        failure("invalid taskType");
    }
    

    
    $logs = fopen(LOGS,"a");

    if ($logs === false) {
        failure("unable to open log file");
    }

    fwrite($logs, "-- submitTask started, arguments:\n");
    $post_array = print_r($_POST, true);          
    fwrite($logs, $post_array);

    // use the first (or only) file name as the base name
    $baseName = pathinfo($_FILES['files']['name'][0], PATHINFO_FILENAME);
    if($taskType == 'merge') {
        // add a suffix so the user may save the merged PDF in the same directory
        $baseName .= '-m';
    }
    $task = Task::createTask($link, $taskType, $baseName, $user);
    $taskId = $task->getId();

    if ($taskType == 'split')
    {
        $tmpName = $_FILES['files']['tmp_name'][0];
        $name = "$taskId.pdf";
        move_uploaded_file($tmpName, $name);

        // determine the split type
        if(isset($_POST['every'])) {
            $splitFunction = 'splitEvery';
            $n = $_POST['every'];
        } else if(isset($_POST['into'])) {
            $splitFunction = 'splitInto';
            $n = $_POST['into'];
        } else {
            failure("invalid split type");
        }

        $start = $end = -1;
        if(isset($_POST['start'])) {
            $start = $_POST['start'];
        }
        if(isset($_POST['end'])) {
            $end = $_POST['end'];
        }

        $r = PDF::$splitFunction($name, $n, $start, $end) != 0;
        // delete the original PDF
        unlink($name);
        if($r != 0) {
            failure("split error");
        }

        // add the splits PDFs to the database
        for($i = 0, $pdf = "$taskId-$i.pdf"; $fi->doesExist($pdf); $i++, $pdf = "$taskId-$i.pdf") {
            $task->addFile($pdf);
        }

        fwrite($logs, "\nSplit PDF. Task ID: $taskId");
    }
    else if($taskType == 'merge') 
    {
        $names = array();
        $pdf = "$taskId.pdf";

        foreach($_FILES['files']['tmp_name'] as $i => $tmpName) {
            $names[] = $tmpName;
        }

        if(PDF::merge($names, $pdf) != 0) {
            failure("merge error");
        }

        $task->addFile($pdf);

        fwrite($logs, "\nMerged PDFs. Task ID: $taskId");
    }
    
    try { 
        $task->save();
    } catch(Exception $e) {
        failure($e->getMessage());
    }

    $fileCount = $task->getFileCount();

    fclose($logs);
    success($taskId, $fileCount, $baseName);
?>
