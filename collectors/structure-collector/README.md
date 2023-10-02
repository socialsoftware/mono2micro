# Structure Collector

Analyzes and collects structural data from source code 
by using the [Spoon Framework](https://spoon.gforge.inria.fr/index.html).

## How to run

You can use maven to open the tool GUI 
and provide the necessary information to run the collector:

`mvn compile exec:java`

After clicking submit, the collector will run, 
and a .json file with the collection results will be created 
at the `./data/collection` subfolder from this location.

## Collection

Currently, the tool supports the following source code framework implementations:
- SpringDataJpa
- FenixFramework

and has the following collection options:
- Domain Entity Data

### Domain Entity Data Collection

Collects field and inheritance information on all domain entities from the source code. 

