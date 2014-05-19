<?php


/**
 *
 * @author Jacob Dison
 */
interface iDatabase {
    public function getTasks($userName);
    public function getPaths($taskID);
    public function store($userName, $type, $webLink, $paths);
    public function newTask($userName, $type, $webLink);
    public function addPDF($taskID, $paths);
}
