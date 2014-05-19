<!DOCTYPE html>
<!--
To change this license header, choose License Headers in Project Properties.
To change this template file, choose Tools | Templates
and open the template in the editor.
-->
<html>
    <head>
        <meta charset="UTF-8">
        <title></title>
    </head>
    <body>
        <?php
            require_once 'database.php';
            require_once 'file.php';
            
            $db = new database;
            $fi = new file;
            
            $temp = $db->getPaths($_GET["taskID"]);
                        
            if ($temp != null)
            {
               $fi -> zip($_GET["taskID"].".zip", $temp);  
               header("Location: ".$_GET["taskID"].".zip");
            }
            else
            {
                echo "<h3>Sorry, that task could not be found</h3>";
            }
        ?>
    </body>
</html>
