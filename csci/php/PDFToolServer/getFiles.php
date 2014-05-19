<?php
/**
 * @author Roman Dovgopol
 * @version 0.1
 * @license cc-by-sa
 * @date Spring2014
 */
	$statusCode = 0;
	$errorCode = '';

	if (!isset($_POST['taskid'])) {
		$statusCode = 1 and $errorCode = "[error] getFiles: bad arguments passed\n";
	}
	
	$taskid = $_POST['taskid'];
	

    require_once 'database.php';
    $db = new database;
    
    
    $logs = fopen(LOGS,"a");
    if ($logs === false) {
        $statusCode = 1;
        $errorCode = "RuntimeException: Unable to open log file.";
    }

    if (!$statusCode) {
        fwrite($logs, "-- getFiles started, arguments:\n");
        $post_array = print_r($_POST, true);
        fwrite($logs, $post_array);

        
        
        $fileCount = $db ->getFileCount($new_taskid);
        $temp = $db->getPaths($new_taskid);
        $baseName = $temp[0];
        #fwrite($logs, "\n\n-- ... \n");

        fclose($logs);
    }

    $result = array('fileCount' => $fileCount , 'baseName' => $baseName);
    echo json_encode($result);
?>
