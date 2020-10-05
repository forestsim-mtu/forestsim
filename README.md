# ForestSim

ForestSim is an agent-based model (ABM) that is intended for research into the policy and sustainability of woody-biomass based biofuels and bioenergy options. It is intended to be an advanced model that incorporates the following aspects of the socio-environmental system:

- Forest growth model
- Non-industrial private forest (NIPF) owners
- Logging companies
- Biorefinery and bioenergy plants

ForestSim is primarily a Java application that makes use of the [MASON Multiagent Simulation Toolkit](http://cs.gmu.edu/~eclab/projects/mason/) to manage to simulation and GeoMASON to support GIS data. The initial 1.0.1 release has been peer-reviewed and published in *SoftwareX*, please cite as,

Zupko, R., & Rouleau, M. (2019). ForestSim: Spatially explicit agent-based modeling of non-industrial forest owner policies. SoftwareX, 9, 117–125. https://doi.org/10.1016/j.softx.2019.01.008

## Development Environment

The following is the development environment:

- Eclipse IDE Neon Release (4.6.0 or 4.6.1)
- Java SE SDK 7 (JavaSE-1.7)

A number of JAR files are included in the repository that are dependencies of MASON and GeoMASON, additional project libraries not included are managed using the MAVEN POM file.

Additionally, the following Eclipse plug-ins are recommended for developers:

- ObjectAid UML Explorer for Eclipse (1.1.11)

## Examples

Currently only one example model is included in the repository. The relevant GIS files are not distributed with the code base and interested parties should contact the research group for access if desired. All of the code required to run the example is present in the /examples directory and /run contains example code for running the model from the command line. 

# Branches

master is the primary stable branch for ForestSim. Development by the author(s) will generally take place on feature branches and 1.0.2 is the current development branch. Contributors are encouraged to fork the repository and submit pull requests with stable code.

# Versioning

Version numbers for ForestSim will have the following structure:

[Major].[Minor].[Revision]

Where [Master] should be understood to be major changes that may break models dependent upon ForestSim. [Minor] contains significant changes to the API, but retains backwards compatibility with method signatures. [Revision]s are smaller updates that are primary focused on bug fixes or quality-of-life issues (ex., adding to an enumerated list).

# References

Wenger, K. F. (Ed.). (1984). Forestry Handbook (Second Edition). John Wiley & Sons, Inc.

