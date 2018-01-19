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
- if first access is from a client, ensure that cluster gets initialzed
  (what if client uses gfpeer ? should work but all client actions will
    not be authorized)

# Possible Future Enhancements #

- Add region level privileges
- The requirement to specify a member when running admin functions is
  undesirable but I can't see a way to remove it and still keep the "clientless"
  feature which I think is more important.
