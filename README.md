# cyclotron

A reporting tool for querying and viewing LG's E2E test runs.

## Installation

Install Clojure. Clone the repo. Create a `config.edn` file at the project root that looks something
like this:

```
{:repo-path "/home/you/logicgate"
 :aws {:profile "lg"
       :bucket "s3://gitlab-logicgate-artifacts/"}}
```

## Usage

Start a repl. Experiment in the siderail/user.clj namespace. Extract what's good to other namespaces.
