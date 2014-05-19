<!DOCTYPE html>
<!--
To change this license header, choose License Headers in Project Properties.
To change this template file, choose Tools | Templates
and open the template in the editor.
-->
<html>
    <head>
        <meta charset="UTF-8">
        <title>PDFapp</title>
    </head>
    <body>
        
        <h1>Welcome to PDFapp</h1>
        <?php
            require_once 'database.php';            
            
            $db = new database;   
            $activeFiles = false;
            
            $temp = $db->getPaths($_GET["taskID"]);
                        
            if ($temp != null)
            {
                echo "<ul>";
                foreach ($temp as $value) 
                {
                    //Does the file exsist
                    if(file_exists("$value"))
                    { 
                        echo "<li><a href=$value>$value</a></li>";
                        $activeFiles = true;
                    }
                }
                
                if ($activeFiles)
                {
                    echo "</ul><br /><br /><a href=zipServe?taskID=".$_GET["taskID"].">Download ZIP</a>";
                }
                else
                {
                    echo "<li>We're sorry, these files are no longer available.</li></ul>";
                }                                    
                
            }
            else
            {
                echo "<h3>Sorry, that task could not be found</h3>";
            }
            
        ?>
    </body>
</html>
