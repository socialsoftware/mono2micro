# To run the Eclipse Plugin

## Repositories
It collects data from codebases implemented with Spring-Boot and the FénixFramework ORM. For instance, it can be applied it to the following repositories:
- Blended Workflow (https://github.com/socialsoftware/blended-workflow) to the engine/bw-core application
- LdoD (https://github.com/socialsoftware/edition) to the edition-ldod application
- FénixEdu Academic (https://github.com/FenixEdu/fenixedu-academic)

## Procedure to Install and Launch the Plugin
- Install Eclipse IDE for RCP and RAP Developers (https://www.eclipse.org/downloads/packages/)
- Launch Eclipse, select *Import Projects...* and *Plug-ins and Fragments*.  Then *Next*.
- In the new window browse the directory `mono2micro/collectors`, select *Select from all plug-ins and fragments found 
  at the specific location* and select *Projects with source folders*. Then *Finish*.
- Add `CallGraph` plugin in the next window.  Then *Finish*.
- Right click on the `CallGraph` project and *Run as -> Eclipse Application*. It launches a new Eclipse application

## Procedure to Collect Data
- `git clone` a codebase, e.g. LdoD
- In the new Eclipse application select *Import...* and *Existing Maven Projects* and select the codebase (directory 
  that contains the maven project)
- Click on the Eclipse icon (CallGraph) 4th icon on the top of the window
- Enter the project name, e.g. `edìtion-ldod`
- Wait until it finishes (it takes several minutes... in the other Eclipse application the processing of each 
  controller is logged)
- When finished it prompts for the directory to write the .json file

