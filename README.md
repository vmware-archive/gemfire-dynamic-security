# Overview
This project is a GemFire 9.2+ SecurityManager implementation that allows
privileges  and passwords to be modified at runtime using Functions which
can be called with gfsh or via the REST API. There is no need for a separate
security admin. tool.

The implementation has a built in user called "gfadmin" who will have all
privileges. gfadmin cannot be delete and it's privileges can't be changed.  The
password cannot be changed except by setting the property and performing a
restart.  However, additional security administrators can be created.

Similarly, the cluster members authenticate each other using a fixed user,
"gfpeer" which has only the minimum permissions required to join the cluster.  
The "gfpeer" password is set with the "security-peer-password" setting and
cannot be changed.

To use gemfire-dynamic-security ensure that _gemfire-dynamic-security-n.n.jar_
is on the class path and set the following properties on all cluster members.

```
security-manager=io.pivotal.pde.gemfire.dynamic.security.DynamicSecurityManager
security-peer-password=passw0rd
security-admin-password=opensesame
security-disk-store-dir=/data/security
```

The `security-peer-password` and `security-admin-password` are the passwords for
the two previously mentioned fixed accounts. __Please use different passwords
for your cluster!__

The settings above configure all cluster members as authenticators but all
cluster members are also authenticatees.  They need to pass credentials as
well as validate them.  

Add the following configuration for all members to
cause them to pass the correct peer credentials.

```
security-username=gfpeer
security-password=passw0rd
```

The security regions are persistent.  They will use a disk store named
_security-disk-store_ and, if passed, the disk store will be created in the
directory specified by the _security-disk-store-dir_ setting.  For example:

```
security-disk-store-dir=/data/security
```

The security model supports the following roles.

MONITOR - can perform simple monitoring tasks (e.g. run Pulse but not view data)

READER - MONITOR privileges + view data

WRITER - READER privileges + modify or delete data

ADMIN - WRITER privileges + the ability to add/remove regions, create indices
and do cluster admin tasks but not security administration tasks.

SECADMIN - Can do anything including security administration tasks.

The examples below illustrate how to use gfsh to perform security administration.

```
# add a new user with WRITER privilege
# if the user exists, both the password and the role will be set
gfsh> execute function --id=add_user --arguments=username,password,WRITER --member=datanode2


# add a new user or reset an existing user's password (does not affect privileges)
# new users created this way have MONITOR
gfsh> execute function --id=set_password --arguments=username,password  --member=datanode1

#set a users privileges (must be one of MONITOR, READER, WRITER, ADMIN or SECADMIN)
gfsh> execute function --id=set_role --arguments=uname,WRITER  --member=datanode1

# remove a user
gfsh> execute function --id=remove_user --arguments=fred  --member=datanode1
```

# to list users, use the following query
gfsh> query --query="select key,value.toString from /_gemusers.entries"


# Security Notes #

- User passwords are hashed together with a random salt.  Only the salt and the
hash are stored, not the password.  When setting the password via a Function
call the password will be sent to the server in clear text unless the connection
is SSL enabled.  Also, the password will potentially be in the gfsh history.  
This could be overcome by calling the Function via REST API.
