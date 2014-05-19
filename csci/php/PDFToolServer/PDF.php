<?php

/**
 * Class full of PDF command line tools
 *
 * @author Jacob Dison
 */

require_once 'iPDF.php';
$basePath = "";


class PDF implements iPDF {
    
    /**
     * Merge a set of PDF's together 
     *
     * @param type $sourcePDFs An array of PDF file names
     * @param type $outputName The file name to save the merged PDF as
     */
    public static function merge($sourcePDFs, $outputName) 
    {

        if(!is_array($sourcePDFs) or count($sourcePDFs) < 2) {
            return 1;
        }
        
        //String the file names together
        $files = "";
        foreach ($sourcePDFs as $value)
        {
            $files .= "$basePath$value ";
        }

        exec("java -jar pdfbox-app-1.8.4.jar PDFMerger $files $basePath$outputName", $output, $returnVar);
        return $returnVar;
    }

    /**
     * Returns 0 on success, not 0 on failure
     */
    public static function splitEvery($sourcePDF, $n, $startPage = -1, $endPage = -1) {
        $path = "$basePath$sourcePDF";

        $command = "java -jar pdfbox-app-1.8.4.jar PDFSplit -split $n";

        // optionally set the start and end pages
        if($startPage != -1) {
            $command .= " -startPage $startPage";
        }
        if($endPage != -1) {
            $command .= " -endPage $endPage";
        }
        $command .= " $path";

        exec($command, $output, $returnVar);
        return $returnVar;
    }

    /**
     * Returns 0 on success, not 0 on failure
     */
    public static function splitInto($sourcePDF, $n, $startPage = -1, $endPage = -1) {
        $path = "$basePath$sourcePDF";

        // get the number of pages in the PDF
        exec("pdfinfo $path", $output, $returnVar);
        if($returnVar != 0)
            return $returnVar;

        $pageCount = 0;
        foreach($output as $line) {
            if(preg_match("/Pages:\s*(\d+)/i", $line, $matches) === 1) {
                $pageCount = intval($matches[1]);
                break;
            }
        }

        if($pageCount == 0) {
            error_log("Invalid PDF?");
            return 1;
        }
        
        $pagesPerPdf = ceil($pageCount / $n);

        $command = "java -jar pdfbox-app-1.8.4.jar PDFSplit -split $pagesPerPdf";

        // optionally set the start and end pages
        if($startPage != -1) {
            $command .= " -startPage $startPage";
        }
        if($endPage != -1) {
            $command .= " -endPage $endPage";
        }
        $command .= " $path";

        exec($command, $output, $returnVar);
        return $returnVar;
    }
}
