!-- Copyright (c) 2015, Netherlands Forensic Institute --!
!-- All rights reserved. --!

Narrative: 
In order to identify new clients
As a server
I want to known when new clients are available


Scenario: Client registers with server

Given the acquisition server is running
Given the client is configured for case defaultcase
Given the client is configured for computer testingclient_40a628a27c904a9f6ca60b332d59aa63

When a new client with name testingclient is started
Then the 3 disks of testingclient are exported on a SMB share within 30 seconds

Given the acquisition server is running
!-- a second registration should not lead to more exported disks --!
When waiting for 30 seconds
Then the 3 disks of testingclient are exported on a SMB share within 0 seconds
