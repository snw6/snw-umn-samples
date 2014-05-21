#!/usr/bin/python

##########################################
# Modified version of class file
# Copyright claimed on modifications only
##########################################

from veil import extractARPDstMac
import socket, struct, sys, time, random

# Local imports 
from veil import *  # for the constants.
from threading import Thread

#######################################
#    Server Port THREAD FUNCTION
#######################################
def serverThread(server_socket):
    while (True):
        print '-----------------------------------------------------'
        client_socket, address = server_socket.accept()
        print "\n", myprintid, "Received an incoming connection from ", address
        packet = receivePacket(client_socket)
        print 'Received packet: ', packet.encode("hex")
        if len(packet) < HEADER_LEN + 8:
            print 'Malformed packet! Packet Length:', len(packet), 'Expected:', HEADER_LEN + 8
            client_socket.close()
            continue
        printPacket(packet, L)
        processPacket(packet)
        client_socket.close()

#updated: define a thread to let the switch down according to input parameter

def downThread():
    global Up
    time.sleep(int(sys.argv[4]))
    Up = False

def queryThread():
    while (1):
        time.sleep(30)
        for i in range(0,L):
            flag[i-1] = 0

###############################################
#    Server THREAD FUNCTION ENDS HERE
###############################################

# Adds an entry to rdvStore, and also ensures that there are no duplicates
def addIfNODuplicateRDVENTRY(dist, newentry):
    global rdvStore
    for x in rdvStore[dist]:
        if x[0] == newentry[0] and x[1] == newentry[1]:
            return
    rdvStore[dist].append(newentry)

# Finds a logically closest gateway for a given svid    
def findAGW(rdvStore, k, svid):
    gw = []

    # if no entry return blank string
    if k not in rdvStore:
        return []

    # for every entry in rdvStore, add Gateway to gateway set {gw}
    for t in rdvStore[k]:
        r = delta(t[0], svid)
        # stores the tuple (distance, gateway)
        gw.append((r, t[0]))

    if len(gw) == 0:
        return []

    # sort based on logical distance
    # this will sort by distance first. if two gateways are at the same
    # distance it will then sort by gateway
    gw.sort()

    # choose up to 3 gateways, shortest logical distance first
    # make sure not to choose the same gateway twice
    gws = []
    i = 0
    while len(gws) < 3 and i < len(gw):
        if gw[i][1] not in gws:
            gws.append(gw[i][1])
        i += 1
            

    #for i in range(min(len(gw), 3)): 
        #gws.append(gw[i][1])

    return gws

#######################################
#    PROCESSPACKET FUNCTION
#######################################
def processPacket(packet):
    global myvid
    dst = getDest(packet, L)
    packettype = getOperation(packet)  # ie. RDV_REPLY / RDV_QUERY / RDV_PUBLISH / DATA?


    # forward the packet if I am not the destination
    if dst != myvid:
        if packettype == RDV_DATA:            
           multipathroutepacket(packet) 
        else:
            routepacket(packet)
        return
               

    # I am the destination of the packet, so process it.    
    print myprintid, 'Processing packet'
    printPacket(packet, L)

    # extract source vid from packet
    svid = bin2str((struct.unpack("!I", packet[8:12]))[0], L)

    # extract payload from packet
    payload = bin2str((struct.unpack("!I", packet[16:20]))[0], L)

    # RDV_PUBLISH packet   
    if packettype == RDV_PUBLISH:
        #payload in RDV_PUBLISH is a vid that is the gateway at dist
        dist = delta(myvid, payload)  # calculate logical distance

        # check if published information already in rdvStore
        if dist not in rdvStore:
            rdvStore[dist] = []

        # prepare rdvStore entry    
        newentry = [svid, payload]  #

        # add new entry if not present in rdvStore
        addIfNODuplicateRDVENTRY(dist, newentry)
        return

    elif packettype == RDV_QUERY:
        k = int(payload, 2)

        # search in rdvStore for the logically closest gateways to reach kth distance away neighbor
        gws = findAGW(rdvStore, k, svid)

        # no gateways found
        if len(gws) == 0:
            print myprintid, 'No gateways found for the rdv_query packet to reach bucket: ', k, ' for node: ', svid
            return
        
        # convert gateways to integers
        gws = [int(gw, 2) for gw in gws]

        # gateway found, form reply packet and sent to svid
        # create a RDV_REPLY packet and send it
        replypacket = createRDV_REPLY(gws, k, myvid, svid)
        routepacket(replypacket)
        return

    elif packettype == RDV_REPLY:
        # Fill my routing table using this new information

        # determine how many gateways were sent in this reply
        ngws = (len(packet) - 20) / 4
        print "ngws=",ngws
        gws = []
        for i in range(ngws):
            p = 20 + (i * 4)
            [gw] = struct.unpack("!I", packet[p:p + 4])
            gws.append(gw)
        
        k = int(payload, 2)

        # add each gateway to the routing table
        for i in range(len(gws)):

            gw = gws[i]
            gw_str = bin2str(gw, L)

            # get nextHop using routingTable to reach Gateway [gw_str]    
            nexthop = getNextHop(gw_str)

            if nexthop == '':
                print 'ERROR: no nexthop found for the gateway:', gw_str
                print 'New routing information couldnt be added! '
                continue

            # convert nextHop from binary to decimal    
            nh = int(pid2vid[nexthop], 2)

            # prepare routingTable entry
            bucket_info = [nh, gw, getPrefix(myvid, k)]

            if k not in routingTable:
                routingTable[k] = []

            default = True
            add = True
            for b in routingTable[k]:
                if b[3] == True:
                    default = False;
                if bucket_info[:3] == b[:3]:
                    # this entry already exists in the routing table
                    add = False
                    break

            if add and len(routingTable[k]) < 3:
                bucket_info.append(default)
                # insert entry into routingTable
                routingTable[k].append(bucket_info)

    elif packettype == RDV_DATA:
        print "Data packet reached destination!"
    else:
        print myprintid, 'Unexpected Packet!!'

###############################################
#    SPROCESSPACKET FUNCTION ENDS HERE
###############################################
###############################################
#    getNextHop function starts here
###############################################

def getNextHop(destvid_str):
    nexthop = ''

    # if dest is neighbor return 
    if destvid_str in vid2pid:
        return vid2pid[destvid_str]

    # calculate logical distance
    dist = delta(myvid, destvid_str)

    # return node from routingTable with dist
    if dist in routingTable:
        entry = None
        # find the default entry
        for tableEntry in routingTable[dist]:
            if tableEntry[3] == True:
                entry = tableEntry
                break
        assert entry != None # there should always be a default
            
        nexthop = bin2str(entry[0], L)
        nexthop = vid2pid[nexthop]

    return nexthop

###############################################
#    getNextHop FUNCTION ENDS HERE
###############################################

###############################################
#    checkConnect FUNCTION BEGINS HERE
###############################################

def checkConnect(switch, address):
    #check to see if we are connected to the switch
    try:
        switch.connect((address[0], int(address[1])))
        return True
    except switch.error as err:
        return False

###############################################
#    checkConnect FUNCTION ENDS HERE
###############################################

###############################################
#    sendPacket function starts here
###############################################

def sendPacket(packet, nexthop):
    # connect to the nexthop
    try:

        packettype = getOperation(packet)
        toSwitch = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        address = nexthop.split(':')
        #check if we are connected before attempting to send
        connected = checkConnect(toSwitch, address)
        if connected == True:
            toSwitch.send(packet)  # send the packet
            toSwitch.close()  # close the connection
            if packettype == RDV_DATA:
                dataPacketSent()
            else:
                controlPacketSent()
            return True
        else:
            toSwitch.close()
            print myprintid, "Failed to connect to switch at IP: ",address[0], "Port: ",address[1]
            printPacket(packet,L)
    except:
        print myprintid, "Unexpected Exception: ", 'received while sending packet to Switch at IP: ', address[0], 'port: ', address[1]
        printPacket(packet, L)

    controlPacketLost()
    return False


###############################################
#    sendPacket FUNCTION ENDS HERE
###############################################

###############################################
#    routepacket function starts here
###############################################

def routepacket(packet):
    global myvid, routingTable, vid2pid, myprintid, L

    # get destination from packet
    dst = getDest(packet, L)

    # If destination is me
    if dst == myvid:
        #print 'I am the destination!'
        processPacket(packet)
        return

    #If destination is one of my physical neighbor
    if dst in vid2pid:
        sendPacket(packet, vid2pid[dst])
        return

    #Find the next hop
    nexthop = ''
    packettype = getOperation(packet)  # ie. RDV_REPLY / RDV_QUERY / RDV_PUBLISH / DATA?

    while nexthop == '':
        if dst in vid2pid:
            nexthop = vid2pid[dst]
            break

        # Calculate logical distance with destination    
        dist = delta(myvid, dst)

        if dist == 0:
            break

        if dist in routingTable:
            entry = None
            # find the default entry
            for tableEntry in routingTable[dist]:
                if tableEntry[3] == True:
                    entry = tableEntry
                    break
            assert entry != None # there should always be a default

            nexthop = bin2str(entry[0], L)
            nexthop = vid2pid[nexthop]
            break

        if (packettype != RDV_PUBLISH) and (packettype != RDV_QUERY):
            break

        print myprintid, 'No next hop for destination: ', dst, 'dist: ', dist
        # flip the dist bit to
        dst = flipBit(dst, dist)

    #print 'MyVID: ', myvid, 'DEST: ', dst
    if dst != getDest(packet, L):
        # update the destination on the packet
        packet = updateDestination(packet, dst)

    if dst == myvid:
        #print "I am the destination for this RDV Q/P message:"
        printPacket(packet, L)
        processPacket(packet)

    if nexthop == '':
        print myprintid, 'no route to destination', 'MyVID: ', myvid, 'DEST: ', dst
        printPacket(packet, L)
        return

    sendPacket(packet, nexthop)
    
# multiroute a packet
def multipathroutepacket(packet):
    global myvid, routingTable, vid2pid, myprintid, L, dataPacketsSent, dataPacketsLost
    global failureStartTime

    # get destination from packet
    dst = getDest(packet, L)
    
    # If destination is me
    if dst == myvid:
        #print 'I am the destination!'
        processPacket(packet)
        return

    #decrement ttl
    ttl = getTTL(packet, L)
    newttl = int(ttl) - 1    

    print "TTL:",newttl

    #die R.I.P. Truly. 
    if ttl == 0:
        dataPacketLost("TTL Expired")
        return

    packet = updateTTL(packet, newttl)

    
    #Find the next hop
    #  nexthop = ''   
    nexthops = []

    #If destination is one of my physical neighbor
    if dst in vid2pid:
        #sendPacket(packet, vid2pid[dst])
        nexthops.append(vid2pid[dst])
        print "destination is one of my physical neighboars", nexthops
        #return
    
    # get Forwarding directive
    fwd = getFwd(packet, L)

    print "fwd=",fwd
    updatefwd = False
    if fwd == dst:
        print 'fwd == dst'
        # calculate a new forwarding directive
        dist = delta(myvid, dst)
        print 'dist =',dist
        if dist in routingTable:
            # gateway of the first (default) bucket
            fwd = bin2str(routingTable[dist][0][1], L)
            # the gateway might be this node, in which case
            # it will be update in the next if statement
            updatefwd = True


    #at the gateway
    if fwd == myvid:
        print 'fwd == myvid'
        # set the new forwarding directive to be the destination
        fwd = dst
        updatefwd = True

    if updatefwd: 
        print "fwd updated to ",fwd
        if fwd in vid2pid:
            nexthops.append(vid2pid[fwd])
        packet = updateForwardingDirective(packet, fwd)

    print 'routingtable =',routingTable

    # Calculate logical distance with forwarding directive     
    dist = delta(myvid, fwd)

    if dist in routingTable: 
        for entry in routingTable[dist]:
            nexthop = bin2str(entry[0], L)
            nexthop = vid2pid[nexthop]
            nexthops.append(nexthop)

    if len(nexthops) == 0:
        print myprintid, 'no route to destination', 'MyVID: ', myvid, 'DEST: ', fwd
        printPacket(packet, L)
        dataPacketLost("No route to destination")
        return

    # remove duplicate nexthops
#nexthops = list(set(nexthops))     

    print "will try to forward this packet to these nexthops: ", nexthops
    for nexthop in nexthops:
        print "sending packet to nexthop {}".format(nexthop)
        if sendPacket(packet, nexthop):
            dataPacketSent()
            return
        if not failureStartTime:
            failureStartTime = time.time()
            print 'failure time - start:', failureStartTime
        print "packet NOT sent. Link to {} is DOWN".format(nexthop)
        nexthop = int(pid2vid[nexthop], 2)
        # remove this next hop
        print "old routing table: ",routingTable
        newRoutingTable = {}
        for k in routingTable:
            newList = []
            for entry in routingTable[k]:
                if entry[0] != nexthop:
                    newList.append(entry[:3] + [False])
            if len(newList):
                newList[0][3] = True
                newRoutingTable[k] = newList;
        routingTable = newRoutingTable
        print "new routing table: ",routingTable

    dataPacketLost("All next ops are down")
        

def dataPacketLost(reason = ''):
    global dataPacketsLost
    dataPacketsLost += 1
    print "Data packet lost - {} - total: {}".format(reason, dataPacketsLost)

def dataPacketSent(reason = ''):
    global dataPacketsSent
    dataPacketsSent += 1
    print "Data packet sent - {} - total: {}".format(reason, dataPacketsSent)

def controlPacketLost(reason = ''):
    global controlPacketsLost
    controlPacketsLost += 1
    print "control packet lost - {} - total: {}".format(reason, controlPacketsLost)

def controlPacketSent(reason = ''):
    global controlPacketsSent
    controlPacketsSent += 1
    print "control packet sent - {} - total: {}".format(reason, controlPacketsSent)

###############################################
#    routepacket FUNCTION ENDS HERE
###############################################
###############################################
#    Publish function starts here
###############################################

def publish(bucket, k):
    global myvid
    dst = getRendezvousID(k, myvid)
    packet = createRDV_PUBLISH(bucket, myvid, dst)
    print myprintid, 'Publishing my neighbor', bin2str(bucket[0], L), 'to rdv:', dst
    printPacket(packet, L)
    routepacket(packet)

###############################################
#    Publish FUNCTION ENDS HERE
###############################################

###############################################
#    Query function starts here
###############################################

def query(k):
    global myvid
    dst = getRendezvousID(k, myvid)
    packet = createRDV_QUERY(k, myvid, dst)
    print myprintid, 'Quering to reach Bucket:', k, 'to rdv:', dst
    printPacket(packet, L)
    routepacket(packet)

###############################################
#    Query FUNCTION ENDS HERE
###############################################


###############################################
#    RunARount function starts here
###############################################

def runARound(round):
    global routingTable
    global vid2pid, pid2vid, mypid, myvid, L
    global failureStartTime
    # start from round 2 since connectivity in round 1 is already learnt using the physical neighbors
    for i in range(2, round + 1):
        # see if routing entry for this round is already available in the routing table.
        if i in routingTable:
            if len(routingTable[i]) > 0:
                #publish the information if it is already there
                for t in routingTable[i]:
                    if t[1] == int(myvid, 2):
                        publish(t, i)
            if len(routingTable[i]) < 3:
                query(i)
        else:
            query(i)
    if failureStartTime:
        endTime = time.time()
        elapsed = endTime - failureStartTime
        print "failure end time {}, elapsed: {}".format(endTime, elapsed)
        failureStartTime = None

###############################################
#    RunARound FUNCTION ENDS HERE
###############################################

if len(sys.argv) != 5:
    print '-----------------------------------------------'
    print 'Wrong number of input parameters'
    print 'Usage: ', sys.argv[0], ' <TopologyFile>', '<vid_file>', '<my_ip:my_port>', '<failure_time>'
    print 'A node will have two identifiers'
    print 'i) pid: in this project pid is mapped to IP:Port of the host so if a veil_switch is running at flute.cs.umn.edu at port 5211 than pid of this switch is = flute.cs.umn.edu:5211'
    print 'ii) vid: It is the virtual id of the switch.'
    print 'TopologyFile: It contains the adjacency list using the pids. So each line contains more than one pid(s), it is interepreted as physical neighbors of the first pid.'
    print 'vid_file: It contains the pid to vid mapping, each line here contains a two tuples (space separated) first tuple is the pid and second tuple is the corresponding vid'
    print 'failure_time: enter 0 to disable and some other integer to set timer to fail the node'
    print '-----------------------------------------------\n\n\n'
    sys.exit(0)

sleeptime = random.random() * 5
print 'Sleeping :', sleeptime, ' seconds!'
time.sleep(sleeptime)

# Put arguments into variables
topofile = sys.argv[1]
vidfile = sys.argv[2]
myport = int((sys.argv[3].split(":"))[1])
mypid = sys.argv[3]
Up = True

dataPacketsSent = 0
dataPacketsLost = 0

controlPacketsSent = 0
controlPacketsLost = 0

failureStartTime = None

# Learn my neighbors by reading the input adjacency list file
myneighbors = []
myvid = ''
pid2vid = {}
vid2pid = {}
routingTable = {}
rdvStore = {}
myprintid = ''
L = 0
# Routing table is a dictionary, it contains the values at each distances from 1 to L
# So key in the routing table is the bucket distance, value is the 3 tuple: tuple 1 = nexthop (vid), tuple 2 = gateway (vid), tuple 3 = prefix (string)

# RDV STORE is a dictionary: it stores the list of edges with distances

# open the topology file in READ mode
fin = open(topofile, 'r')

# read the file line by line
line = fin.readline()  #reads and pushes the first line into "line"
line = line.strip()  #strips whitespaces from the beginning and end of the line (not the whitespaces in between)

while line != '':
    if line.find(mypid) == 0:  #satisfies if the first node in the line is mypid
        # this is the line which contains the neighbor list for my 
        myneighbors = (line.split(' '))[1:]  #populates every neighbor into the myneigbors array
        break  #exit while loop
    line = fin.readline()  # move to next line
    line = line.strip()
fin.close()  # close topology file

#Error checking
if ' ' in myneighbors:
    print 'Warning: My neighbor list contains empty pids!'
if len(myneighbors) < 1:
    print 'Warning: My neighbor list is empty, will quit now!'
    sys.exit(0)

#Print list of myneighbors    
print 'My neighbors: ', myneighbors

# Learn my and myneighbor's vids
fin = open(vidfile, 'r')  #open file in read mode
line = fin.readline()  #reads and pushes the first line into "line"
line = line.strip()  #strips whitespaces from the beginning and end of the line (not the whitespaces in between)

while line != '':
    tokens = line.split(' ')
    if tokens[0] in myneighbors:  # if pid present in myneighbor[] array
        pid2vid[tokens[0]] = tokens[1]  # eg. pid2vid["localhost:5001"] = "11"
        vid2pid[tokens[1]] = tokens[0]  # eg. pid2vid["11"] = "localhost:5001"
    elif tokens[0] == mypid:  # if pid == mypid
        myvid = tokens[1]  # store my vid

    line = fin.readline()  # move to next line
    line = line.strip()
fin.close()  # close vid file

# Learn L, it is the length of any vid
L = len(myvid)

myprintid = "VEIL_SWITCH: [" + mypid + '|' + myvid + ']'

# Now start my serversocket to listen to the incoming packets         
server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server_socket.bind(("", myport))
server_socket.listen(5)
print myprintid, ' is now listening at port: ', myport

# create a server thread to take care of incoming messages:
server = Thread(target=serverThread, args=([server_socket]))
server.setDaemon(True)
server.start()

# Simulate node failure time
if sys.argv[4] != '0':
    t = Thread(target=downThread)
    t.setDaemon(True)
    t.start()

round = 1

# Round 1: Put my physical neighbors' information in my routing table
for vid in vid2pid:  # iterate for every vid in vid2pid list
    dist = delta(vid, myvid)  #calculate logical distance XOR

    if dist not in routingTable:
        routingTable[dist] = []  # create dist level entry in routingTable

    bucket_len = len(routingTable[dist])  # no. of entries with dist in routingTable

    # int(X,2) converts binary X to decimal
    # getPrefix(myvid,dist), flips the dist^th bit and makes RHS of dist as * 
    #   eg. getPrefix('0101', 3) will return '00**'
    #   format[dist] = <Nexthop vid>, <gateway vid>, <prefix>, <default>
    bucket_info = [int(vid, 2), int(myvid, 2), getPrefix(myvid, dist)]

    if not isDuplicateBucket(routingTable[dist], bucket_info):
        if len(routingTable[dist]) == 0:
            # make the first gateway the default (TODO should be shortest distance?)
            bucket_info.append(True)
        else:
            bucket_info.append(False)
        routingTable[dist].append(bucket_info)  # add bucket_into to routingTable[dist]

while Up:
    print myprintid, 'Starting Round #', round
    runARound(round)
    round = round + 1
    if round > L:
        round = L
    print '\n\t----> Routing Table at :', myvid, '|', mypid, ' <----'
    for i in range(1, L + 1):
        if i in routingTable:
            for j in routingTable[i]:
                print 'Bucket #', i, 'Nexthop:', bin2str(j[0], L), 'Gateway:', bin2str(j[1], L), 'Prefix:', j[2], 'Default:', j[3]
        else:
            print 'Bucket #', i, '  --- E M P T Y --- '
    print 'RDV STORE: ', rdvStore
    print '\n --  --  --  --  -- --  --  --  --  -- --  --  --  --  -- \n'
    time.sleep(ROUND_TIME)

print "VEIL_SWITCH: ["+mypid+'|'+myvid+'] has been terminated!'
