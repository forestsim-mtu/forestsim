# Getting Started with ForestSim

This document serves as an introduction to using ForestSim for the first time. While familiarity with [MASON](https://cs.gmu.edu/~eclab/projects/mason/) is not assumed, it is encouraged that the [MASON user guide](https://cs.gmu.edu/~eclab/projects/mason/manual.pdf) (PDF) is read since ForestSim builds on the MASON environment, including the user interface. This document was prepared using Eclipse 2021-12 (4.22.0) and while older version of Eclipse are similar, some menu options may be different.

## Building ForestSim

The ForestSim environment can be downloaded from GitHub natively within Eclipse:

1. Select "File > Import" a new dialog will appear.
2. Expand the "Git" option and select "Projects from Git" and click "Next", the dialog will advance. 
3. Select "Clone URI" and click "Next", the dialog will advance.
4. Enter "https://github.com/forestsim-mtu/forestsim.git" (without quotes) into the "URI" text box, the dialog will update, then click "Next".
5. Select "Next" when the "Branch Selection" dialog appears.
6. The directory you wish to download the repository to, or leave the default suggestion in place and click "Next".
7. Eclipse will work for a bit downloading the repository, once it is complete the option to "Import Existing Eclipse Projects" will appear, click "Next" and then "Finish".

Once complete, the ForestSim project will appear in the "Package Explorer" and Eclipse will start attempting to build the project. A red exclamation mark (<span style="color:red">!</span>) may appear indicating that the project still needs to be configured. The first step is to ensure that the libraries supplied are registered with Eclipse and the build environment:

| JAR | Artifact ID | Group ID |
| --- | --- | --- |
| geomason-1.5.jar | GeoMason | GeoMason |
| mason-17.jar | Mason | Mason |
| quaqua-colorchooser-only.jar | quaqua-colorchooser-only | quaqua-colorchooser-only |


1. Expand the "lib" directory, you should see three or more JAR file listed.
2. Right click on one of the JAR files (ex., quaqua-colorchooser-only.jar) and select "Import", a new dialog will appear.
3. Expand "Maven" and select "Install or deploy an artifact to a Maven repository", then click "Next".
4. Enter the appropriate Artifact ID and Group ID based upon the JAR file and click "Finish".
5. The dialog will the close, repeat this process for each of the JAR files.
6. Once all JAR files have been imported, right click on "pom.xml" and select "Maven > Update Project" a new dialog will appear.
7. Ensure "ForestSim" is listed as a Maven project and click "OK", the project will be updated in the background and the red exclamation mark (<span style="color:red">!</span>) should disappear.