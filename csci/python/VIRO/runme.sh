#!/bin/sh

#give user a choice of which tests to run

clear

CHOICE_0="0) Reset and recompile"
CHOICE_1="1) fat-tree-k2, no workload option"
CHOICE_2="2) isp-level3, no workload option"
CHOICE_3="3) fat-tree-k2 set 1"
CHOICE_4="4) fat-tree-k2 set 2"
CHOICE_5="5) fat-tree-k2 set 3"
CHOICE_6="6) isp-level3 set 1"
CHOICE_7="7) isp-level3 set 2"
CHOICE_8="8) isp-level3 set 3"
CHOICE_9="9) EXIT"

echo "Choose VIRO tests to run"
echo "------------------------"
echo $CHOICE_0
echo $CHOICE_1
echo $CHOICE_2
echo $CHOICE_3
echo $CHOICE_4
echo $CHOICE_5
echo $CHOICE_6
echo $CHOICE_7
echo $CHOICE_8
echo $CHOICE_9

read NUM_CHOICE

# run appropriate test based on user's choice

case $NUM_CHOICE in
    0) rm -f topologies_workloads/workloads_for_report/*.txt topologies_workloads/workloads_for_report/*.output error*.txt *.pyc *.*~ *~; python compile_all.py; chmod 755 *.pyc;;
    1) cd topologies_workloads/topologies; ./fat-tree-k2.no.workload.run.sh 2> ../../error$NUM_CHOICE.txt ;;
    2) cd topologies_workloads/topologies; ./isp-level3.no.workload.run.sh 2> ../../error$NUM_CHOICE.txt ;;
    3) cd topologies_workloads/workloads_for_report; sudo ./fat-tree-k2.set1.run.sh 2> ../../error$NUM_CHOICE.txt ;;
    4) cd topologies_workloads/workloads_for_report; sudo ./fat-tree-k2.set2.run.sh 2> ../../error$NUM_CHOICE.txt ;;
    5) cd topologies_workloads/workloads_for_report; sudo ./fat-tree-k2.set3.run.sh 2> ../../error$NUM_CHOICE.txt ;;
    6) cd topologies_workloads/workloads_for_report; sudo ./isp-level3.set1.run.sh 2> ../../error$NUM_CHOICE.txt ;;
    7) cd topologies_workloads/workloads_for_report; sudo ./isp-level3.set2.run.sh 2> ../../error$NUM_CHOICE.txt ;;
    8) cd topologies_workloads/workloads_for_report; sudo ./isp-level3.set3.run.sh 2> ../../error$NUM_CHOICE.txt ;;
    9) echo "Good-bye!" ; exit;;
    *) echo "Invalid choice!" ; exit;;
esac
cd ../..
