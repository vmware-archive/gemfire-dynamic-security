
Holds users, passwords and privileges in the gemusers region in gemfire and
supports user maintenance tasks through Functions that can be called with
gfsh.

The implementation has a built in user called "gfadmin" who will have all
privileges. gfadmin cannot be delete and it's privileges can't be changed
but its password can be changed.

In addition to the usual GemFire CLUSTER/DATA MANAGE/READ/WRITE paradigm
this implementation adds a SECADMIN privilege.  All attempts to access or modify
the gemusers region require the SECADMIN privilege.  

The following security properties must be set in order to use this implementation:

security-manager=io.pivotal.pde.gemfire.dynamic.security.DynamicSecurityManager
security-peer-password=passw0rd
security-admin-password=opensesame
security-disk-store-dir=/data/security
user=gfpeer
password=passw0rd

The cluster members authenticate eachother using a fixed user, "gfpeer" which
has only the minimum permissions required to join the cluster.  The "gfpeer"
password is set with the "security-peer-password" setting.

All cluster members should be started with identical values for security-peer-password
and security-admin-password.

The security region is persistent.  It will use a disk store named
security-disk-store and, if passed, the disk store will be created in the
directory specified by security-disk-store-dir.

Functions are included for performing security administration.  Only gfadmin
can do security administration.

The security model supports the following roles.
MONITOR - can perform simple monitoring tasks (e.g. run Pulse but not view data)
READER - MONITOR privileges + view data
WRITER - READER privileges + modify or delete data
DBADMIN - WRITER privileges + the ability to add/remove regions, create indices and do cluster admin tasks.
Cannot do security administration tasks.
ADMIN - Can do anything including security administration tasks.

Only "gfadmin" can do security administration tasks.  Security administration
can be done within gfsh using the "execute function" command.  All security
admin tasks are illustrated below

```
# list users and their privileges
execute function --id=secadmin --member=any-member --arguments="list"

#add a new user or reset an existing user's password (does not affect privileges)
execute function --id=secadmin --member=any-member --arguments="setpass bob bobpass"

#set a users privileges (must be one of MONITOR, READER, WRITER, DBADMIN or ADMIN)
execute function --id=secadmin --member=any-member --arguments="permit bob MONITOR"

# remove a user
execute function --id=secadmin --member=any-member --arguments="remove bob"
```

setUser(uname, password, privileges)
delUser(uname)
listUsers()

privileges are described as follows:

# Implementation Notes
- Creation of the "gemusers" region and all other initialization happens
the first time "authorize" is called for "gfadmin".  Attempts to initialize at
other times, including in the security manager "init" method did not work because
the cluster was not fully formed.

# TESTS
1. cluster joins with these properties set but members that have the wrong
   password or do not set a security manager to not join.  PASS

```
security-manager=io.pivotal.pde.gemfire.dynamic.security.DynamicSecurityManager
security-peer-password=passw0rd
security-admin-password=opensesame
security-disk-store-dir=/data/security
user=gfpeer
password=passw0rd
```

2. The first time "gfadmin" logs in the necessary initilization will be performed
including creating the "gemusers" region and registering any required functions.

3. It works no matter which locator you hit.

4. It works if you log if the first thing you do with "gfadmin" is
log in to Pulse.

5. Directly accessing the gemusers region with gfsh (or an application) is not
permitted.
