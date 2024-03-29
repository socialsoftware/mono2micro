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

### Generate Representation Files

Start by creating your monolith representation files for domain entity accesses and structure.

- Run the [Spoon Collector](https://github.com/socialsoftware/mono2micro/blob/master/collectors/spoon-callgraph/README.md) to create access-related files;
- Run the [Structure Collector](https://github.com/socialsoftware/mono2micro/blob/master/collectors/structure-collector/README.md) to create structure-related files.

### Generate Decomposition

With the representation files, you can [run Mono2Micro](https://github.com/socialsoftware/mono2micro/blob/master/README.md) and create a codebase with a "Structure Based" representation that uses "Access" and "Structure" criteria for the strategy. From there, generate your decomposition.

### Refactor Functionalities and Create Contract

Once you have a decomposition your are happy with, click "Refactorization Tool" to refactor functionalities into Sagas. After that, go back and click "Export to CML". The result from exporting is a JSON formatted contract describing the properties of the candidate decomposition.

### Generate CML

With the contract, you can then use maven to run the tool:

`mvn compile exec:java -Dexec.mainClass=pt.ist.socialsoftware.cml.converter.Converter -Dexec.args="<path/to/m2m_contract.json> <output_name> <service_naming_mode>"`

- `<path/to/m2m_contract.json>` is the path to the decomposition contract (must be named m2m_contract!);
- `<output_name>` is the name of the output .cml file;
- `<service_naming_mode>` is the naming strategy for service names, accepting `1` for full access trace transcription; `2` for ignoring access types (read/write); and `3` for ignoring access types and order. Any other number will generate generic "stepN" names.

After running, the output .cml will be created in the `./out/` folder of the tool.

*Note*: Exceptions related to `org.eclipse.xtext.formatting2` may appear in the command line. You can safely ignore these.

## Pre-generated results

If you wish to see the results from the decomposition of the [Quizzes-Tutor](https://quizzes-tutor.tecnico.ulisboa.pt) monolith, the generated contract can be found [here](https://github.com/socialsoftware/mono2micro/blob/master/tools/cml-converter/src/test/resources/test-contract/m2m_contract.json), and the results using different heuristics [here](https://github.com/socialsoftware/mono2micro/tree/feature/export-functionality/tools/cml-converter/out).
