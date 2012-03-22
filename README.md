Cloud9 Utilities
================

This package contains utilities related to Cloud9

mars
----

mars is a command line development tool for deploying and synchronizing 
application resources to/from a Cloud9 cluster.

### create

This command creates a Cloud9 application and requires the user to specify the 
application name. A subdirectory within the directory the command was executed 
from is then created based on the entered application name.

    $ mars create <app-name>

### clone 

This command clones a remote Cloud9 application to your local environment. The 
application is mirrored within the directory the command was executed from.

    $ mars clone <app-name>

### push

This command pushes local changes to a remote cluster. An optional file argument
can be passed and only that file will be pushed. If no argument is specified, all
local files will be pushed.

    $ mars push [<file>]

### pull

This command pulls files from a remote cluster and updates the local copy. An optional 
file argument can be passed and only that file will be pulled down from the cluster.

    $ mars pull [<file>] 

settings
--------

Applications can be cloned from one cluster and pushed to another. For instance from a
local development cluster to a another developemnt or QA cluster.

The default host and port can be configured within the `.settings` file located at the 
root of the application directory.

    mars.default.host=localhost
    mars.default.port=2600

This file can edited to point to any valid cluster `host/port`.

