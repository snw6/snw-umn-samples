<?php


/**
 *
 * @author Jacob Dison
 */
interface iFile {
    public function save($fileName, $data);
    public function open($path);
    public function delete($fileName);
    public static function zip($fileName, $files, $baseName);
}
