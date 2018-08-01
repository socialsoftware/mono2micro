# MonolithicToMicroservices

This repository contains the code regarding the implementation for my Master Thesis at IST with the topic of "From a Monolithic to a Microservices Architecture".

The final product of this method is to transform a web application that is not divided into a microservices architecture and by of the controllers with the domain classes, cluster those classes into groups that could work as services, paired up with visualization capabilities.

To do so, follow the subsequent workflow:
  1. Use Java-CallGraph to generate the files of the web application (https://github.com/gousiosg/java-callgraph)
  2. Execute ParserJavaCallgraph.py on the generated .txt file, this will produce a serialized python dictionary in form another.txt file
  3. Run UserInterface.py and select the the file dictionary file. This will generate the dendrogram of the application
  4. Select the cut intended and the cluster formations will be formed in D3.JS with the browser Mozilla Firefox.
  
# Others:
Working applications are Blended Workflow and LdoD and to switch between these two there is a need to edit the ParserJavaCallgraph.py and its directories (line 30 with 33 and 40 with 42).
