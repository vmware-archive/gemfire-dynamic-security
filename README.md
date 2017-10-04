
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
security-peer-auth-init=io.pivotal.pde.gemfire.dynamic.security.DynamicSecurityManager
security-peer-username=
security-peer-password=
security-gfadmin-password=
security-disk-store-dir=

Note that security-peer-username and security-peer-password are used both
for credential passing and credential checking.  All members of a cluster
should have the same values.

security-gfadmin-password will be used to initialize the gfadmin password
if it does not exist.  If the gfadmin user already exists, this value will
be ignored.

The security region should be persistent.  It will use a disk store named
security-disk-store and, if passed, the disk store will be created in the
directory specified by security-disk-store-dir.

The following functions are included for performing maintenance tasks

setUser(uname, password, privileges)
delUser(uname)
listUsers()

privileges are described as follows:


# TESTS
1. cluster joins with these properties set but members that have the wrong
   password or do not set a security manager to not join.

```
  security-manager=io.pivotal.pde.gemfire.dynamic.security.DynamicSecurityManager
  security-peer-auth-init=io.pivotal.pde.gemfire.dynamic.security.DynamicSecurityManager
  security-peer-username=gfpeer
  security-peer-password=GemF1re
```
