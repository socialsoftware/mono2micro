# CML Decomposition Generator

This tool provides a way to generate CML code based on candidate decompositions from Mono2Micro. This code can then be viewed and worked on in [Context Mapper](https://contextmapper.org), using the available Eclipse IDE plugin or VSCode extension.

The tool provides several discovery strategies to generate *Bounded Contexts*, *Aggregates*, *Entities*, *Services* and *Coordinations* based on the elements of candidate decompositions, which include entities, clusters and sequences of entity accesses (functionalities) redesigned as Sagas.

It also provides heuristics to generate service call names based on the entity accesses that occur inside services.

## Dependencies

You need to manually install the following Discovery Library version:

`mvn install:install-file
   -Dfile=./lib/context-map-discovery-0.2.0.jar
   -DgroupId=org.contextmapper
   -DartifactId=context-map-discovery
   -Dversion=0.3.0-dev
   -Dpackaging=jar
   -DgeneratePom=true`

## How to run

Firstly, a candidate decomposition must be exported from the Mono2Micro tool.
The result from exporting is a JSON formatted contract describing the properties of the candidate decomposition.

You can then use maven to run the tool:

`mvn compile exec:java -Dexec.mainClass=pt.ist.socialsoftware.cml.converter.Converter -Dexec.args="<path/to/contract.json> <output_name> <service_naming_mode>"`

- `<path/to/contract.json>` is the path to the decomposition contract;
- `<output_name>` is the name of the output .cml file;
- `<service_naming_mode>` is the naming strategy for service names, accepting `0` for full access trace transcription; `1` for ignoring access types (read/write); and `2` for ignoring access types and order.

After running, the output .cml will be created in the `./out/` folder of the tool.

*Note*: Exceptions related to `org.eclipse.xtext.formatting2` may appear in the command line. You can safely ignore these.
