!-- Copyright (c) 2015, Netherlands Forensic Institute --!
!-- All rights reserved. --!

Narrative: 
In order to read the remote disks
As a analyst
I want to read data from the virual SMB file


Scenario: Client registers with server

Given the acquisition server is running
Given the client is configured for case defaultcase
Given the client is configured for computer testingclient_e31c59cc9c15c72e3f37fb1e31630440

When a new client with name testingclient is started
Then the 3 disks of testingclient are exported on a SMB share within 30 seconds

Given the acquisition server is running
When parallel reading in 20 threads 19 bytes at offset 101 of disk 0 on client testingclient
Then the read bytes should contain "afbf629c1b776877dec"

When reading 20 bytes at offset 100 of disk 0 on the evidence storage of testingclient
Then the read bytes should contain "5afbf629c1b776877dec"

When reading 15 bytes at offset 65536 of disk 0 on the evidence storage of testingclient
Then the read bytes should be zeros

Given a readable evidence storagebitmap of disk 0 of testingclient
When checking availability of bytes at offset 100 in the evidence storagebitmap
Then the availability status should be true
When checking availability of bytes at offset 65536 in the evidence storagebitmap
Then the availability status should be false

When reading 15 bytes at offset 65536 of disk 0 on client testingclient
Then the read bytes should contain "BLOCK  00000080"

When reading 15 bytes at offset 65536 of disk 0 on the evidence storage of testingclient
Then the read bytes should contain "BLOCK  00000080"

Given a readable evidence storagebitmap of disk 0 of testingclient
When checking availability of bytes at offset 65536 in the evidence storagebitmap
Then the availability status should be true

When parallel reading in 20 threads 19 bytes at offset 101 of disk 0 on client testingclient
Then the read bytes should contain "afbf629c1b776877dec"

When parallel reading with skips of 65536 in 20 threads 19 bytes at offset 101 of disk 3 on client testingclient
Given a readable evidence storagebitmap of disk 3 of testingclient
When checking availability of bytes from offset 101 until 1245285 in the evidence storagebitmap
Then the availability status should be true
