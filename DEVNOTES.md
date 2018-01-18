# Random Notes

- gfpeer needs CLUSTER:MANAGE and nothing else
- CLUSTER:MANAGE is not sufficient for access to Pulse

# Tests #

- locator with no credentials cannot join: PASS
- data node with wrong credentials cannot join: PASS
- gfpeer can't do anythin in Pulse: PASS
- can't remove gfpeer or gfadmin: PASS
- log in to Puls with bad gfadmin password fails: PASS

# Possible Future Enhancements #

- Add region level privileges
- The requirement to specify a member is undesirable but I can't see a
  way to remove it and still keep the "clientless" feature which I think
  is more important.
