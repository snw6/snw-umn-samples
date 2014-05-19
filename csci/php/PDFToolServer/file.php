<?php


/**
 * File system interface
 *
 * @author Jacob Dison
 */

require_once 'iFile.php';

$basePath = "";

class file implements iFile {
       
    /**
     * Save a file
     * 
     * @global string $basePath basepath for working with files
     * @param type $fileName File name to save as
     * @param type $data Raw file data
     * @return boolean True if it saved
     */
    public function save($fileName, $data)
    {        
        global $basePath;
        
        //Does the file exsist?
        if(!file_exists("$basePath$fileName"))
        {            
            //open it
            if(file_put_contents("$basePath$fileName", $data, LOCK_EX)) 
            {
                return true;
            }            
        }
        return false;
    }
    
    /**
     * open a file
     * 
     * @global string $basePath basepath for working with files
     * @param type $path path to file to open
     * @return handle to the opened file
     */
    public function open($path)
    {
        global $basePath;
        
        return fopen("$basePath$path","r");
    }
    
    /**
     * Delete a file
     * 
     * @param type $fileName File to delete
     */
    public function delete($fileName)
    {
        global $basePath;
        
        unlink("$basePath$fileName");
    }
    
    /**
     * 
     * 
     * @global string $basePath basepath for working with files
     * @param type $fileName Name to save zip as
     * @param type $files Array of file names to zip
     * @return True if ok
     */
    public static function zip($fileName, $files, $baseName)
    {
        global $basePath;
        
        $zip = new ZipArchive();

        //Does the file already exsist
        if(!file_exists("$basePath$fileName"))
        { 
            //No? Good. Create it then.
            if($zip->open("$basePath$fileName", ZipArchive::CREATE)) 
            {
                //Add files to zip
                foreach ($files as $i => $file) 
                {
                    $zip->addFile("$basePath$file", "$baseName-$i.pdf");
                }
                $zip->close();
                return true;
            }
        } else {
            return true;
        }
        return false;

    }
    
    /**
     * Checks to see if a file exists 
     * 
     * @param type $fileName Name of the file to be checked
     * @return boolean True if the file exsists 
     */
    public function doesExist($fileName)
    {
        global $basePath;
        if(file_exists("$basePath$fileName"))
        { 
            return true;
        }
        
        return false;        
    }
    
    
}
