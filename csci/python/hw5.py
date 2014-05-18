## HW5.py
## Written for Computer Science 1901: Structure of Computer Programming I
## This program is an exercise in recursive computing and list processing/comparison.
## My commenting style here is quite over-the-top, as this was my first CSCI class
## and I progressed and improved my commenting style as time went on.

## For educational reference only! See DISCLAIMER on the home page of my repo.

## Copyright 2010 Seth West
## Licensed under the Apache License, Version 2.0 (the "License");    
## you may not use this file except in compliance with the License.
## You may obtain a copy of the License at:
## 
## http://www.apache.org/licenses/LICENSE-2.0
##
## Unless required by applicable law or agreed to in writing, software
## distributed under the License in distributed on an "AS IS" BASIS,
## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
## implied.
## See the License for the specific language governing permissions and
## limitations under the License.

###############################################
# Python section
#################################################

####################
# Problem 2 Solution
#find_e
#Parameters: n (integer)
#Returns: base+total3 (float, approximation of e based on limit)
def find_e(n):
  if n==0:
    return 1
  else:
    base=1
    total=1
    total2=1
    total3=0
    power = n
    while power>0:#as long as the power is greater than 0, we'll loop through...
      exp=power#exp is initialized to the value of power (how we'll set up the factorial loop)
      while exp>0:#while the value of exp is greater than 0 (since we know 1/0! is 1 anyways...)...
        total*=exp#take the current value for total and multiply it by the value of exp for the new total...
        exp-=1#lower exp by 1 and loop again until exp reaches 0 here...
      total2=(1.0/total)#when the "exp" loop is finished, the total2 value will be set at 1.0 over the value of total (needed to turn the fraction into a float decimal)
      total3+=total2#total3 is added to total2 to give the new value for total3
      total=1#total is reset to 1, and...
      total2=1#total2 is also reset to 1...
      power-=1#as is power lowered by 1 to travel the outer loop again (provided the power is not 0)
    return base+total3#once the "power" loop is finished, we are done calculating, so simply return the value of base(1)+plus whatever the value for total3 is--the sum of those two numbers is our approximation of e to the value of n we fed into the function

# Test cases
print find_e(0)
# should return 1
print find_e(2)
# should return 2.5
print find_e(10)
# should return (about) 2.7182818011463845
#print find_e(256)
# should return (about) 2.7182818284590455
print find_e(170)

#########################
# Problem 3 Solution
def compareLists(listA, listB):
  if len(listA)!=len(listB):#since the length of both lists passed in must be equal to properly run this function...
    return "ERROR!!! ERROR!!! ERROR!!! EXAMINE!!"#...if the lengths of both lists are not equal, return this STAR TREK-inspired error message telling the user to examine the lists put in (error message is a quote from the STAR TREK episode, "The Changeling", uttered by the alien robot, Nomad, who had destroyed all life it considered "imperfect," after Captain Kirk pointed out--with what Mr. Spock called "impeccable logic"--that it made three different errors during its time on the USS Enterprise, which implies...IMPERFECTION)
  else:#otherwise, solider onward...
    aLen=len(listA)#initialize aLen to the length of listA (this will be the same as the length of listB, because if it were not, the program would kick you out)
    index=aNum=bNum=tNum=0#initialize index, aNum, bNum, and tNum to 0 (index is a placeholder to traverse only the length of the lists being passed in which will check elements in lists at spots indicated by "index"; aNum, bNum, and tNum will hold counts of how many times either: 1) the element in listA is greater than the element in listB, 2) vice versa, or 3) neither--as in they're both equal)
    while index<aLen:#while the index is less than aLen (while we are looking at items within the range of listA)...
      if listA[index]>listB[index]:#if the "index-th" item in listA is greater than the "index-th" item in listB...
        aNum+=1#add 1 to the value of aNum
      elif listB[index]>listA[index]:#otherwise if the "index-th" item in listB is greater than the "index-th" item in listB...
        bNum+=1#add 1 to the value of bNum
      else:#otherwise..(since neither of those are true, by default, they must be equal to each other)
        tNum+=1#add 1 to the value of tNum
      index+=1#then add one to index: if the new index is still less than aLen, run the loop again...
    if aNum>bNum:#otherwise, when the loop finally exits, check to see if aNum is greater than bNum, if it is...
      finalLet="A"#assign "A" to the variable finalLet (this variable stores the instance that occured the most--if A had the most occurences of higher numbers, if B did, or if they both did)
    elif bNum>aNum:#otherwise if bNum is greater than aNum...
      finalLet="B"#assign "B" to finalLet
    elif aNum==bNum:#otherwise if aNum and bNum are actually equal to each other...(then they TIED!)
      finalLet="T"#...assign "T" to finalLet
    else:#otherwise, by default, they must have tied more
      finalLet="T"#so assign "T" to finalLet
    aList=["A", aNum]#make a new list called aList with "A" and aNum as its elements (stats for occurences of A being greater)
    bList=["B", bNum]#make a new list called blist with "B" and bNum as its elements (stats for occurences of B being greater)
    tList=["T", tNum]#make a new list called tList with "T" and tNum as its elements (stats for occurences of A and B being equal)
    finalList=[aList, bList, tList]#make yet another list called finalList with aList, bList, and tList as its elements (list of all final stats)
    absolute=[finalLet, finalList]#make one last list called absolute with finalLet and finalList as its elements (this list is the final result of all our computing
    return absolute#to finish it all off, return absolute and we are done

#Test cases
print compareLists([104, 203, 197, 178, 107], [187, 177, 209, 178, 114])
# should return 'B' ((A, 1), (B, 3), (T,1))
print compareLists([104, 203, 197, 178, 107], [104, 187, 209, 178, 114])
# should return  'B' ((A, 1), (B, 2), (T,2))
print compareLists([194, 203, 297, 178], [277, 209, 178, 114])
# should return 'T' ((A, 2), (B, 2), (T, 0))
print compareLists([203, 197, 178, 178, 114], [187, 177, 209, 178, 114])
# should return 'A' ((A, 2), (B, 1), (T,2))
