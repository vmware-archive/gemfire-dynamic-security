# Overview
This project is a GemFire 9.2+ SecurityManager implementation that allows
privileges  and passwords to be modified at runtime. All security administration
tasks can be accomplished with gfsh. There is no need for a separate security
admin. tool and security changes take effect immediately without the need
to restart cluster members. All administration tasks are supported through
Functions that are provided

# Setup #

To use gemfire-dynamic-security ensure that _gemfire-dynamic-security-n.n.jar_
is on the class path and set the following properties on all cluster members.

```
security-manager=io.pivotal.pde.gemfire.dynamic.security.DynamicSecurityManager
security-peer-password=passw0rd
security-admin-password=opensesame
security-disk-store-dir=/data/security
security-username=gfpeer
security-password=passw0rd
```

The _security-peer-password_ and _security-admin-password_ settings give the passwords for the two previously mentioned fixed accounts. __Please use different passwords for your cluster!__

The security regions are persistent.  They will use a disk store named
_security-disk-store_ and, if passed, the disk store will be created in the
directory specified by the _security-disk-store-dir_ setting.

The _security-username_ and _security-password_ settings control the
credentials that each member presents when joining the cluster.  The
_security-username_ setting must be "gfpeer" and the _security-password_
setting must have the same value as the _security-peer-password_ setting.

Lastly, the cluster must use persistent pdx metadata.  There are several
ways to accomplish this.  Below is one procedure for making pdx metadata persistent.

1. Start the cluster.
2. Connect with gfsh and sign in with the "gfadmin" user.
3. Run the following gfsh commands (adjust to put the metadata in your desired location)
   ```
   gfsh>create disk-store --name=pdx-disk-store --dir=data/pdx
   gfsh>configure pdx  --disk-store=pdx-disk-store
   gfsh>shutdown
   ```
   This will stop all data nodes.  It is necessary to do this in order for
   the changes to take effect.
4. Start the cluster

This procedure only needs to be done the first time the cluster is started.
Thereafter, assuming the cluster configuration service has not been disabled,
the configuration changes will stick.

Once the cluster is started, you can use the provided admin. functions to
create additional users and control who can do what.  The details are provided
below.

# Security Model #

The implementation has a built in user called "gfadmin" who will have all
privileges. The "gfadmin" user cannot be delete and it's privileges can't be changed.  
The password cannot be changed except by setting the property and performing a restart.  
However, additional security administrators can be created.

Similarly, the cluster members authenticate each other using a fixed user,
"gfpeer" which has only the minimum permissions required to join the cluster.
The "gfpeer" password is set with the "security-peer-password" setting and
cannot be changed.

The security model supports the following roles. _Each role has all the
permissions of all of the earlier roles except for those of PEER_.

| Role     | Description                                                       |
|:---------|:------------------------------------------------------------------|
| PEER     | Has the minimum credentials to join the cluster as a peer.  (i.e. CLUSTER:MANAGE ) |
| MONITOR  | Can run Pulse but cannot view data.                               |
| READER   | Can read all data other than the security data in the _gemusers region.|
| WRITER   | Can modify or delete data other than the security data in the _gemusers region.|
| ADMIN    | Create regions, disk stores, indices and perform all cluster management tasks not related to security. |
| SECADMIN | Manage users, their passwords and roles                           |


# Security Administration Procedures #

Security administration is provided by several built-in Functions.

The examples below illustrate how to use the admin Functions.  Note that all
functions should be invoked with the --member option. The command will run
on the specified member but the user and privilege information will be propagated
to all members.

```
# add a new user with the WRITER role
# if the user exists, both the password and the role will be set
gfsh> execute function --id=add_user --arguments=myuser,mypass,WRITER --member=datanode2


# add a new user or reset an existing user's password (does not affect privileges)
# new users created this way have the MONITOR role
gfsh> execute function --id=set_password --arguments=myuser,mypass  --member=datanode1

#set a users privileges (must be one of PEER, MONITOR, READER, WRITER, ADMIN or SECADMIN)
gfsh> execute function --id=set_role --arguments=myuser,WRITER  --member=datanode1

# remove a user
gfsh> execute function --id=remove_user --arguments=myuser  --member=datanode1

# to list users, use the following query
gfsh> query --query="select key,value.toString from /_gemusers.entries"

```



# Security Notes #

- User passwords are hashed together with a random salt.  Only the salt and the
hash are stored, not the password.  When setting the password via a Function
call the password will be sent to the server in clear text unless the connection
is SSL enabled.  Also, the password will potentially be in the gfsh history.  
This could be overcome by calling the Function via REST API.
