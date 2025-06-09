## CodeQL collector project

### How to run the project

```shell
mvn exec:java -Dexec.args="ORM_OPTION QUERIES_OPTION PROJECT_NAME /path/to/codeql/database"
```

| Item           |                0                |             1             |       2        |
|:---------------|:-------------------------------:|:-------------------------:|:--------------:|
| ORM_OPTION     |         Spring Data JPA         |      Fenix Framework      | Python Django  |
| QUERIES_OPTION | Existing query results are used | CodeQL queries always run |                |


### Useful commands

```shell
codeql database create <database> --language=<language-identifier>
```