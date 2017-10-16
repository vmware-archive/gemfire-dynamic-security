Holds users, passwords in the _gemusers_ region in and privileges in the _gemroles_
region.  Keep security information in gemfire solves the problem of distribution
to new members.

User maintenance is performed via simple "puts" to the _gemusers_ and _gemroles_
regions and can be done with gfsh.

The implementation has a built in user called "gfadmin" who will have all
privileges. gfadmin cannot be delete and it's privileges can't be changed.  The
password for gfadmin is given at statup time and can only be changes with a
restart (can be rolling).

Similarly, the cluster members authenticate each other using a fixed user, "gfpeer" which
has only the minimum permissions required to join the cluster.  The "gfpeer"
password is set with the "security-peer-password" setting.


To use gemfire-dynamic-security set the following properties:

```
security-manager=io.pivotal.pde.gemfire.dynamic.security.DynamicSecurityManager
security-peer-password=passw0rd
security-admin-password=opensesame
security-disk-store-dir=/data/security
user=gfpeer
password=passw0rd
```

All cluster members should be started with identical values for security-peer-password
and security-admin-password.

The security regions are persistent.  They will use a disk store named
_security-disk-store_ and, if passed, the disk store will be created in the
directory specified by _security-disk-store-dir_.

The security model supports the following roles.
MONITOR - can perform simple monitoring tasks (e.g. run Pulse but not view data)
READER - MONITOR privileges + view data
WRITER - READER privileges + modify or delete data
DBADMIN - WRITER privileges + the ability to add/remove regions, create indices
and do cluster admin tasks but not security administration tasks.
ADMIN - Can do anything including security administration tasks.

All attempts to access or modify
the _gemusers_ region or the _gemroles_ region require the ADMIN privilege.  

The examples below illustrate how to use gfsh to perform security administration.

```
# list users and their privileges
query --query="select key, value from /gemroles.entries"

#add a new user or reset an existing user's password (does not affect privileges)
put --region=gemusers --key=bob --value=bobpass

#set a users privileges (must be one of MONITOR, READER, WRITER, DBADMIN or ADMIN)
put --region=gemroles --key=bob --value=MONITOR

# remove a user
remove --region=gemusers --key=bob
remove --region=gemroles --key=bob
```

# Implementation Notes
- Creation of the _gemusers_ and _gemroles_ regions and all other initialization
happens the first time "authorize" is called for "gfadmin".  Attempts to
initialize at other times, including in the security manager "init" method did
not work because the cluster was not fully formed.
