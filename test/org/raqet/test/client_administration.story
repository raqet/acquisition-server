!-- Copyright (c) 2015, Netherlands Forensic Institute --!
!-- All rights reserved. --!

Narrative: 
In order to inspect the status of the system
As a analyst
I want to check the list of current active clients

Scenario: Check the current registered clients.

Given the acquisition server is running
Given the client is configured for case case1
Given the client is configured for computer computer1

When requesting the list of investigationcases
Then the list of investigationcases contains 1 items
And the list of investigationcases contains defaultcase

Given the acquisition server is running
When adding a investigationcase case1
Then no error must be given

When requesting the list of investigationcases
Then the list of investigationcases contains 2 items
And the list of investigationcases contains case1

When requesting the list of computers for investigationcase case1
Then the list of computers is empty

When adding a computer computer1 with clientid testingclient to case1
Then no error must be given

When requesting the list of computers for investigationcase case1
Then the list of computers contains 1 items
And the list of computers contains computer1

Given the acquisition server is running
When adding a investigationcase case2
And requesting the list of computers for investigationcase case2
Then the list of computers is empty

Given the acquisition server is running
When adding a investigationcase case3
And adding a investigationcase case3
Then the error Failed must be given

When a new client with name testingclient is started
Then the 3 disks of testingclient are exported on a SMB share within 30 seconds

When requesting the list of active clients as text
Then the list of active clients in html contains testingclient

When requesting the list of active clients as json
Then the list of active clients in json contains testingclient

