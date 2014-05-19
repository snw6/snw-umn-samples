<?php

/**
 * Database interface class
 *
 * @author Jacob Dison
 * 
 * No SQL injection shinagans please !
 */

require_once 'iDatabase.php';

$con;
$rs;  

//DB login stuff
$dbPath = "127.0.0.1";
$dbUserName = "root";
$dbPassword = "password";
$dbName = "csci5221";

class database implements iDatabase {

    private $con;

    public function __construct() {
        $this->con = NULL;
    }

    public function getLink() {
        if($this->con == NULL) {
            $this->open();
        }
        return $this->con;
    }
    
    /**
     * Open the database connection
     * 
     * @global type $con The connection to the database 
     * @global string $dbPath Path to database
     * @global string $dbUserName Database user name
     * @global string $dbPassword Database password
     * @global string $dbName Database name
     */
    private function open()
    {
        global $dbPath,$dbUserName,$dbPassword,$dbName;
        
        //Attempt database connection
        $this->con=mysqli_connect($dbPath,$dbUserName,$dbPassword,$dbName);
        
        // Check connection
        if (mysqli_connect_errno())
        {
            //no bueno :(
            echo "Failed to connect to MySQL: " . mysqli_connect_error();
        }        
    }
    
    /**
     * Close the database connection 
     * 
     * @global type $con Connection to database
     */
    private function close()
    {
        global $con;
        
        //Close connection
        mysqli_close($con);
    }

    /**
     * Return all of the file paths for a given task
     * 
     * @global type $con Connection to database
     * @param type $taskID TaskID to return paths for
     * @return type $rows Array of file names associated with the given task
     */
    public function getPaths($taskID) 
    {
        global $con;
        
        //Is database connection open?
        if ($con == NULL) {
            $this->open();
        }

        //Query 
        $rs = mysqli_query($con,"SELECT Path FROM PDF_table where TaskID = '$taskID'");
        
        $rows;
        
        //Parse
        while($row = mysqli_fetch_array($rs))
        {            
            $rows[] = $row['Path'];
        }
        
        return $rows;
    }
    
    /**
     * Get all of the tasks associated with a user
     * 
     * @global type $con Database connection 
     * @param type $userName Username to search on
     * @return type $rows Array of taskID's for the given user
     */
    public function getTasks($userName) 
    {
        global $con;
        
        //Is database connection open?
        if ($con == NULL) {
            $this->open();
        }

        //Query
        $rs = mysqli_query($con,"SELECT * FROM task_table WHERE UserName='$userName'");
        
        $rows;
        
        //Was anything returned?
        if (!$rs)
        { 
            return null;
        }
        
        //Parse
        while($row = mysqli_fetch_array($rs))
        {             
            $rows[] = "$row[TaskID] - $row[Type] - $row[Timestamp]";
        }
        
        return $rows;
    }

    /**
     * Create a new task for a user
     * 
     * @global type $con Connection to database
     * @param type $userName Username to create task for
     * @param type $type Task type (M = merge, S = Split)
     * @param type $webLink Allow weblink (Y/N)
     * @return Unique taskID
     */    
    public function newTask($userName, $type, $webLink)
    {
        global $con;
        
        //Is database connection open?
        if ($con == NULL) {
            $this->open();
        }

        //Get the time
        $time = date('Y-m-d H:i:s', time());
        
        //Query
        $sql = mysqli_query($con,"INSERT INTO task_table (UserName, Timestamp, WebLink, Type)
        VALUES ('$userName','$time','$webLink','$type');");   
               
        //Did we get anything back?
        if (!$sql)
        {            
            return false;
        }

        return mysqli_insert_id($con);        
    }
    
    /**
     * Add a file path to a taskId
     * 
     * @global type $con Conncetion to database
     * @param type $taskID TaskID to add paths to
     * @param type $paths A single or array of paths to associate eith the taskID
     * @return boolean true if things went well
     */
    public function addPDF($taskID, $paths)
    {
        global $con; 
        
        //Is database connection open?
        if ($con == NULL) {
            $this->open();
        }
       
        //Is it a single path or an array?
        if (is_array($paths))
        {
            //perform inserts
            foreach ($paths as $value)
            {
                $sql = mysqli_query($con,"INSERT INTO PDF_table (TaskID, Path)
                VALUES ('$taskID', '$value')");
        
                //How'd it go?
                if (!$sql)
                {
                    //Oh I see, well better luck next time
                    return false;
                }           
            }
        }
        else
        {
            //Insert
           $sql = mysqli_query($con,"INSERT INTO PDF_table (TaskID, Path)
           VALUES ('$taskID', '$paths')");
        
           //Any Problems?
           if (!$sql)
           {
                return false;
           }  
        }
        return true;
    }
    
    /**
     * 
     * @global type $con Database connection
     * @param type $taskID TaskID to return number of filed for
     * @return Number of files
     */
    public function getFileCount($taskID)
    {
        global $con; 
        
        //Is database connection open?
        if ($con == NULL) {
            $this->open();
        }
        
         $sql = mysqli_query($con,"SELECT COUNT(*) FROM PDF_table where TaskID = '$taskID';");   
               
        //Did we get anything back?
        if (!$sql)
        {            
            return false;
        }

        $row = mysqli_fetch_array($sql);
        return $row["COUNT(*)"];  
        
    }
    
    /**
     * All in one store of a new task and paths.
     * 
     * @param type $userName Username 
     * @param type $type  Task type (M = merge, S = Split)
     * @param type $webLink Allow weblink (Y/N)
     * @param type $paths A single or array of paths to associate eith the taskID
     */
    public function store($userName, $type, $webLink, $paths) 
    {                
        if($seq = $this->newTask($userName, $type, $webLink))
        {
            return $this->addPDF($seq, $paths);            
        }               
    }

}
