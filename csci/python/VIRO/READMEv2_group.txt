README v2_group.txt
-------------------------------------------------
CSCI 5221: Foundations of Advanced Networking
Spring 2014 - University of Minnesota
Project 2 - Multipath VIRO
By Jacob Dison, Roman Dovgopol, Vikram Reddy, Tyler Schloesser, and Seth West
--------------------------------------------------

Development and testing environment:

We developed this program using Python while running Ubuntu Linux on our own machines and CSELabs machines.

Instructions:

To expedite the process of running the VIRO simulations, we included a file called runme.sh (found in our main project repository folder, "/VIRO") which will allow the user to quickly run VIRO tests. If the user is unable to execute the file, do "chmod 755 runme.sh" to change the permissions to allow execution of the file.

Description of runme.sh:

The user is prompted to choose from a menu of 10 options for running VIRO.

Choose 0 to reset the program-- remove prior output and error text files and compiled Python binaries, recompile the binaries and set them with execute permissions
Choose 1 to run the fat-tree-k2 simulation with no workload
Choose 2 to run the isp-level3 simulation with no workload
Choose 3 to run the fat-tree-k2 simulation with workload1
Choose 4 to run the fat-tree-k2 simulation with workload2
Choose 5 to run the fat-tree-k2 simulation with workload3
Choose 6 to run the isp-level3 simulation with workload1
Choose 7 to run the isp-level3 simulation with workload2
Choose 8 to run the isp-level3 simulation with workload3
Choose 9 to exit the program without doing anything
Entering any other number or input character will cause the program to exit with an error message.

Any of the simulations using workloads will output the results to text files in the topologies_workloads/workloads_for_report directory. Additionally, any errors are logged and output to the errors.txt file in the main VIRO directory. In order to avoid any runtime socket-based errors, it is necessary to run the simulations as sudo, which runme.sh does (you will be prompted for a sudo password).

To stop all output, kill the veil_switch program as sudo (sudo pkill -f veil_switch). After veil_switch is finished, change the permissions on the viro output files to read them without being logged in as sudo (sudo chmod a+x topologies_workloads/workloads_for_report/*.txt).

Division of work:

Seth worked on Task 2 and Task 3.
Tyler worked on Task 1 and Task 2.
Jake worked on Task 2 and Task 3.
Vikram worked on the report.
Roman worked on the report.
