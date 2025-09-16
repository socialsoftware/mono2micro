# CodeQL collector

A command-line tool for code collection using CodeQL

---

## Prerequisites

* **Java 17+** (check with `java -version`)
* **Maven 3.9.6+** (check with `mvn -v`)
* **CodeQL CLI** (check with `codeql version`)

---

## Build

Clone the repository and build the project:

```bash
mvn clean package
```

This will create a runnable JAR in the `target/` directory (e.g. `target/collector-1.0-SNAPSHOT.jar`).

---

## Run

You can run the CLI directly with Maven:

```bash
mvn exec:java -Dexec.args="--framework=spring my-proj /home/user/myproject-codeql-db"
```

## Usage

```bash
Usage: collector [-hV] [--no-queries] --framework=<frameworkOption> <projectName> <dbPath>
Collects data for a given project and CodeQL database.
      <projectName>   Project name
      <dbPath>        Path to the CodeQL database
      --framework     Framework option (required)
      --no-queries    Disable running queries (default: enabled)
  -h, --help          Show this help message and exit
  -V, --version       Print version information and exit
```

---

Got it 👍 — I can polish and reformat your section so it reads more clearly, is easier to follow, and looks good in the README. Here’s an improved version with better structure, Markdown styling, and examples:

---

## 🔧 Extending the Collector: Adding a Library File

The Collector can be extended to support new frameworks and languages by adding a **library file**. Follow the steps below:

### 1. Check for an Existing Language Library

Before creating anything new, look under the `*-library` folders to see if the language is already supported (e.g., `java-library`, `python-library`).

* ✅ If it exists → Add your new `.qll` file to the existing `language-folder/frameworks/` folder.
* ❌ If it does not exist → Continue with step 2.

---

### 2. Create a New Language Library

If no library exists for your language, create a new folder following the naming convention:

```
<language>-library/
```

Inside this folder, add a `qlpack.yml` file:

```yaml
name: codeql-collector/LANGUAGE-FOLDER
version: 0.0.1
dependencies:
  codeql/LANGUAGE-all: "*"
```

* **`LANGUAGE-FOLDER`** → the folder you just created (e.g., `python-library`)
* **`LANGUAGE`** → the language name in lowercase (e.g., `python`)

---

### 3. Add Frameworks

Inside your new library folder, create a `frameworks/` directory. Place your `.qll` files there.

Each `.qll` should implement the following classes and predicates:

#### Required Classes

* `class DomainEntity`

    * `predicate hasField(...)`
    * `predicate hasSuperclass(...)`
* `class DomainField`

    * `predicate getFieldName(...)`
    * `predicate getFieldType(...)`
* `class DomainSuperclass`
* `class CallableFunction`

    * `predicate getFullName(...)`
    * `predicate getId(...)`
* `class FunctionInvoc`

#### Required Predicates

* `predicate callsCallee(CallableFunction caller, CallableFunction callee, FunctionInvoc call)`
* `predicate isEndpoint(CallableFunction fun)`
* `predicate callableAccessesEntity(CallableFunction cf, DomainEntity entity, string operation, Location loc)`

---