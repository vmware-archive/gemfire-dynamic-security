# Overview
This project is a GemFire 9.2+ SecurityManager implementation that allows
privileges  and passwords to be modified at runtime. All security administration
tasks can be accomplished with gfsh. There is no need for a separate security
admin. tool and security changes take effect immediately without the need
to restart cluster members. User administration  tasks are supported through
a set of Functions that are installed by the SecurityManager.

The solution comes with two built-in users that are used for bootstrapping
purposes.  The built-in users are "gfadmin" and "gfpeer".  "gfadmin" has all
privileges including the ability to add new users and grant permissions. "gfpeer"
is used by the cluster members to authenticate to each other.  The built-in users
cannot be removed but their passwords are set using properties.

# Setup #

Build gemfire-dynamic-security-n.n.jar

```
mvn package
```

To use gemfire-dynamic-security ensure that _gemfire-dynamic-security-n.n.jar_
is on the class path and set the following properties on all cluster members.

```
security-manager=io.pivotal.pde.gemfire.dynamic.security.DynamicSecurityManager
security-peer-password=passw0rd
security-admin-password=opensesame
security-username=gfpeer
security-password=passw0rd
```

The _security-peer-password_ and _security-admin-password_ settings give the
passwords for the two previously mentioned fixed accounts. __Please use
different passwords for your cluster!__

The _security-username_ and _security-password_ settings control the
credentials that each member presents when joining the cluster.  The
_security-username_ setting must be "gfpeer" and the _security-password_
setting must have the same value as the _security-peer-password_ setting.

Lastly, the cluster must be initialized before the first use.

## One Time Cluster Initialization Procedure ##

A persistent region called "_gemusers" must be created and because there is
persistent data you will need to configure pdx persistence as well.  A gfsh
script, "init_cluster.gfsh" is supplied for the purpose.

At least one data node must be up to create a region via gfsh but pdx related
configurations do not apply to data nodes that are already started.  Therefore,
the general procedure is for cluster initialization is:

1. Start the cluster
2. Review and edit the init script
3. Connect to the cluster and run the script.  The script will automatically
shut down all data nodes when it is done.
4. Start the cluster (it is now ready for use)

__The script contains locations for disks stores, don't forget to update these
to appropriate values before running it. __

The script contents are below.

```
deploy --jar=target/gemfire-dynamic-security-1.1.1.jar  
create disk-store --name=pdx-disk-store --dir=EDIT_THIS --allow-force-compaction=true
create disk-store --name=security-disk-store --dir=EDIT_THIS --allow-force-compaction=true
echo --string="waiting for disk store creation"
sleep --time=10
create region --name=_gemusers --type=REPLICATE_PERSISTENT --disk-store=security-disk-store
configure pdx --read-serialized=true --disk-store=pdx-disk-store
shutdown```

The script can be run with a command similar to the following (it will need to
  be run as "gfadmin").

```
gfsh -e "connect --locator=loctorhost[10334] --user=gfadmin --password=opensesame" -e "run --file=init_cluster.gfsh"
```

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
