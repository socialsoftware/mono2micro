# How to run:

On terminal run using maven:

`mvn compile exec:java`

When prompted input the clone link of the repository
you want to analyse and the ORM Technology 
that is used in that repository.

After running, a .json file with the sequence of calls 
of the program will be created at this folder, ready to
be given as input to the frontend.

#### Assumptions:
* A SpringDataJPA projects only access the database via SpringData Repositories, i.e.,
there are no references to EntityManager or SessionFactory.

* @Query annotations do not explicitly reference namedQueries.
This does not mean that the project can not have @NamedQuery(ies)
or @NamedNativeQuery(ies);

* We assume that interfaces have at most one explicit implementation
and we redirect calls to interfaces to the respective interface implementor 

* Inheritance Strategy (SINGLE_TABLE) has no more than one level of inheritance

* Assuming no fully qualified path names inside HQL 'From' clauses

* Assuming no comments on database access queries

* Assuming non-sensitive naming
