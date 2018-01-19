# Random Notes

- gfpeer needs CLUSTER:MANAGE and nothing else
- CLUSTER:MANAGE is not sufficient for access to Pulse

# Tests #

- locator with no credentials cannot join: PASS
- data node with wrong credentials cannot join: PASS
- gfpeer can't do anything in Pulse: PASS
- can't remove gfpeer or gfadmin: PASS
- log in to Pulse with bad gfadmin password fails: PASS
- user with MONITOR privileges can use most of Pulse but not Data Browser: PASS
- create a new cluster, log in to gfsh with gfadmin and create monitor
  user, stop cluster, start cluster and gfsh connect using monitor user.
  Ensure cluster is initialized using "list functions", "list regions" : PASS
- MONITOR cannot create regions: PASS
- MONITOR cannot query \_gemusers or execute security admin. Function: PASS
- MONITOR cannot read or write data in regions: PASS
- PEER cannot create regions: PASS
- PEER cannot query \_gemusers or execute security admin. Function: PASS
- PEER cannot read or write data in regions: PASS
- READER cannot create regions: PASS
- READER cannot query \_gemusers or execute security admin. Function: PASS
- READER can read and query data in normal regions regions: PASS
- READER cannot write data: PASS
- WRITER cannot create regions: PASS
- WRITER cannot query \_gemusers or execute security admin. Function: PASS
- WRITER can read and query data in normal regions regions: PASS
- WRITER can write data:  PASS
- ADMIN can create regions: PASS
- ADMIN cannot query \_gemusers or execute security admin. Function: PASS
- ADMIN can read and query data in normal regions regions: PASS
- ADMIN can write data:  PASS
- SECADMIN can create regions: PASS
- SECADMIN cannot query \_gemusers or execute security admin. Function: PASS
- SECADMIN can read and query data in normal regions regions:PASS
- SECADMIN can write data:  PASS
- The following test test the initialization login ...
  - if the first thing to access the cluster is a client using gfadmin,
    the client can use the cluster. PASS
  - if the first thing to access the cluster is a client using gfpeer,
    the client cannot access the cluster and a "Not Authorized" message is
    given.  PASS
  - if a client authenticating as a WRITER is the first thing to access the
    cluster, the client is able to use the cluster.


# Possible Future Enhancements #

- Automate the tests
- Add region level privileges
- The requirement to specify a member when running admin functions is
  undesirable but I can't see a way to remove it and still keep the "clientless"
  feature which I think is more important.
