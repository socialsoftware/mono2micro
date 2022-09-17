"""
A mixed collection strategy, where the static analysis data influences the commits.

The algorithm is like so:
- Retrieve, parse, fix the history and store it in a dataframe.
- Convert the dataframe into Commit objects, but store only the entities.
- For each functionality:
    * For each commit:
        + Does it have at least two entities from the functionality trace? Then, create a new commit with those entities
- Convert the "new" commits back into a dataframe, with the same format as before
- Compute the couplings using this custom history as if it was the original history.
"""
import itertools
import json
from collections import defaultdict

import pandas as pd
from rich import print

from collector.repository import Repository
from helpers.constants import Constants


def findsubsets(s: set, m: int):
    return set(itertools.permutations(s, m))


class Functionality:
    def __init__(self, entities_ids_accessed, name):
        self.entities_ids_accessed = entities_ids_accessed
        self.name = name

    def __str__(self):
        return f"{self.name} - {len(self.entities_ids_accessed)} entities"

    def __repr__(self):
        return self.__str__()

    def get_entities_in_commit(self, commit, codebase_repo):
        files = commit.commit_data["filename"]
        files_ids = set([int(codebase_repo.get_file_id(file)) for file in list(files)])
        return self.entities_ids_accessed.intersection(files_ids)


class Commit:
    def __init__(self, commit_hash, commit_data):
        self.commit_hash = commit_hash
        self.commit_data = commit_data

    def __repr__(self):
        return f"{self.commit_hash[:8]} - {len(self.commit_data)} entities"


def parse_functionalities(static_analysis_file_path):
    functionalities = []
    with open(static_analysis_file_path, "r") as f:
        data = json.load(f)
    for controller in data:
        accesses = data[controller]["t"][0]["a"]
        entities_ids = set()
        for access in accesses:
            entities_ids.add(access[1])
        if len(entities_ids) > 1:
            functionalities.append(Functionality(entities_ids, controller))
    return functionalities


def get_commits_from_history(history):
    commits = []
    for commit_hash, commit_data in history.commits():
        commits.append(Commit(commit_hash, commit_data))
    return commits


def increment_hash(commit_hash, hash_hash_table):
    # keep track of a hash hash table, where we increment a hash by 1 so we know which commit it sort of belongs to
    # but we still keep it unique hopefully. may not work.
    previous_hash = hash_hash_table.get(commit_hash, None)
    if previous_hash is None:
        # Get new hash based on commit_hash
        new_value = hex(int(commit_hash, 16) + 1)
        hash_hash_table[commit_hash] = hex(int(commit_hash, 16) + 1)
    else:
        new_value = hex(int(previous_hash, 16) + 1)
        hash_hash_table[commit_hash] = hex(int(previous_hash, 16) + 1)
    return new_value


def get_adapted_history(functionalities, commits, codebase_repo):
    new_commit_list: [Commit] = []
    hash_hash_table = {}
    for functionality in functionalities:
        for commit in commits:
            entities_from_functionality_in_commit = functionality.get_entities_in_commit(commit, codebase_repo)
            if len(entities_from_functionality_in_commit) > 1:
                new_commit_list.append(Commit(
                    increment_hash(commit.commit_hash, hash_hash_table),
                    entities_from_functionality_in_commit
                ))
    # Convert the commits to DF: basically rbind them all
    commit_data = []
    for commit in new_commit_list:
        for entity_id in commit.commit_data:
            commit_data.append([commit.commit_hash, entity_id])
    return pd.DataFrame(commit_data, columns=["commit_hash", "file_id"])


def get_couplings(adapted_history):
    couplings = []
    for a, commit in adapted_history.groupby('commit_hash'):
        couplings += findsubsets(commit["file_id"], 2)
    return pd.DataFrame(couplings, columns=['first_file', 'second_file'])


def coupling_to_json(logical_coupling, repo: Repository) -> dict:
    logical_coupling_data = defaultdict(list)
    entities_ids = [int(x) for x in repo.id_to_entity.keys()]

    for file in entities_ids:
        changed_with_this_file = logical_coupling.loc[logical_coupling['first_file'] == file]['second_file']
        if len(list(changed_with_this_file)) == 0:
            logical_coupling_data[file] = []
        else:
            for file2 in list(changed_with_this_file):
                logical_coupling_data[file].append(file2)

    # Any entity that we missed for some reason? Also add it here. Otherwise, many tears will be shed 😭
    for entity_id in entities_ids:
        if entity_id not in logical_coupling_data:
            logical_coupling_data[entity_id] = []

    return logical_coupling_data


def collect(codebases):
    for codebase in codebases:
        codebase_repo = Repository(codebase)
        cutoff_value = 100  # Commits with 5 or more files are ignored
        print("Getting history")
        history = codebase_repo.cleanup_history(cutoff_value)

        print("Parsing functionalities")
        functionalities = parse_functionalities(f"{Constants.codebases_data_output_directory}/{codebase}/{codebase}.json")
        print("Getting commit objects")
        commits = get_commits_from_history(history)
        print("Converting history")
        adapted_history = get_adapted_history(functionalities, commits, codebase_repo)
        print("Getting couplings")
        couplings = get_couplings(adapted_history)
        print("Converting to json")
        couplings_json = coupling_to_json(couplings, codebase_repo)
        with open(f"{Constants.codebases_data_output_directory}/{codebase}/{codebase}_commit_mixed.json", "w") as f:
            json.dump(couplings_json, f)

# collect(["fenixedu-academic"])
