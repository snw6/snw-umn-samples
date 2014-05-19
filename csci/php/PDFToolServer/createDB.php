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
        $con;
        $rs;
        $dbPath = "localhost";
        $dbUserName = "csci5221";
        $dbPassword = "qwerty1";
        $dbName = "csci5221";
        
        $con=mysqli_connect($dbPath,$dbUserName,$dbPassword,$dbName);
        // Check connection
        if (mysqli_connect_errno())
        {
            echo "Failed to connect to MySQL: " . mysqli_connect_error();
        }  
        else
        {
            echo "good stuff...";
        }
        
        
        $sql = mysqli_query($con,"CREATE  TABLE `csci5221`.`task_table` (
  `TaskID` INT NOT NULL AUTO_INCREMENT ,
  `UserName` VARCHAR(45) NOT NULL ,
  `Timestamp` TIMESTAMP NOT NULL ,
  `WebLink` CHAR NOT NULL ,
  `Type` VARCHAR(45) NOT NULL ,
  PRIMARY KEY (`TaskID`) ,
  UNIQUE INDEX `TaskID_UNIQUE` (`TaskID` ASC) );");
        
           if ($sql)
           {
                echo "task table created";
           }  
           else
           {
               echo "task table failed";
           }
           
           $sql = mysqli_query($con,"CREATE  TABLE `csci5221`.`PDF_table` (
  `PDFID` INT NOT NULL AUTO_INCREMENT ,
  `TaskID` INT NOT NULL ,
  `Path` VARCHAR(255) NOT NULL ,
  PRIMARY KEY (`PDFID`) ,
  UNIQUE INDEX `PDFID_UNIQUE` (`PDFID` ASC) ,
  INDEX `TaskID` (`TaskID` ASC) ,
  CONSTRAINT `TaskID`
    FOREIGN KEY (`TaskID` )
    REFERENCES `PDFapp`.`task_table` (`TaskID` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);
");
        
           if ($sql)
           {
                echo "PDF table created";
           }  
           else
           {
               echo "PDF table failed";
           }
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        mysqli_close($con);
        ?>
    </body>
</html>
