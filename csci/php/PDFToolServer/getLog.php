<?php
/**
 * @author Roman Dovgopol
 * @version 0.1
 * @license cc-by-sa
 * @date Spring2014
 */

	define("LOGS", getcwd() . "/logs/API.log");
    /*
    ini_set('display_errors', 'On');
    error_reporting(E_ALL);
    */
    $statusCode = 0;
    $errorCode = '';

	if (!isset($_POST['token'])) {
		echo "[error] getLog: bad arguments passed\n";
		die;
	}
	
	$token = $_POST['token'];
	
	require_once 'database.php';
    $db = new database;
    
    
    $logs = fopen(LOGS,"a");
    if ($logs === false) {
        $statusCode = 1;
        $errorCode = "RuntimeException: Unable to open log file.";
    }

    if (!$statusCode) {
        fwrite($logs, "-- getLog started, arguments:\n");
        $post_array = print_r($_POST, true);
        fwrite($logs, $post_array);

        $taskLog = "";
        $rs = $db->getTasks($token); 
        
        foreach ($temp as $value) 
        {         
            $taskLog .= "Task: $value \n";
        }

        fclose($logs);
    }

    $result = array('logs' => $taskLog); 
    echo json_encode($result);
?>
