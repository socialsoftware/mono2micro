"""
Script to identify subtraces of functionalities using commit logs.

1. Obtain all commits
2. Initialize a "subtraces" list
3. For each functionality:
    For each commit:
      Create new empty list A
      For each access in the functionality:
         Is the entity in this access part of the set of entities modified in the commit? If so, place it in a list
         If it isn't, and list A is not empty, add the list to the dictionary
4. Return top 10 most common subtraces
"""
import os
from collections import Counter, defaultdict

from collector.legacy.functionalitysplit import parse_full_functionalities
from collector.repository import Repository
from helpers.constants import Constants


class Subtrace:
    def __init__(self, accesses, functionality_name):
        self.accesses = accesses
        self.functionality_name = functionality_name

    def __str__(self):
        return f"({str(self.accesses)}"

    def __repr__(self):
        return self.__str__()

    def __eq__(self, other):
        return self.accesses == other.accesses

    def __len__(self):
        return len(self.accesses)

    def __hash__(self):
        return hash(str(self.accesses))

    def add_access(self, access):
        self.accesses.append(tuple(access))


def main():
    codebase = "quizzes-tutor"
    subtraces_counts = []
    subtraces_functionalities = defaultdict(list)
    subtraces_commits = defaultdict(list)

    print(f":white_circle: {codebase}")
    print("  :white_circle: Initializing history")
    codebase_repo = Repository(codebase)

    cutoff_value = 100
    print("  :white_circle: Processing history")
    history = codebase_repo.cleanup_history(cutoff_value)

    print("  :white_circle: Parsing functionalities")
    functionalities = parse_full_functionalities(
        f"{Constants.codebases_data_output_directory}/{codebase}/{codebase}.json")

    entity_to_id = codebase_repo.entity_to_id

    for functionality in functionalities:
        print(f"    :white_circle: {functionality.name}")
        entities_in_functionality = set()
        for a in functionality.accesses:
            entities_in_functionality.add(a[1])
        for commit_hash, commit_data in history.commits():
            new_subtrace = Subtrace([], functionality.name)
            trace_with_commit_entities = False
            commit_data_ids = [int(codebase_repo.file_to_id[os.path.splitext(os.path.basename(f))[0]]) for f in commit_data["filename"]]
            for access in functionality.accesses:
                if access[1] in commit_data_ids:
                    if trace_with_commit_entities:
                        new_subtrace.add_access(access)
                    else:
                        if len(new_subtrace) != 0:
                            subtraces_counts.append(new_subtrace)
                            subtraces_functionalities[new_subtrace].append(functionality.name)
                            subtraces_commits[new_subtrace].append(commit_hash)

                        new_subtrace = Subtrace([tuple(access)], functionality.name)
                        trace_with_commit_entities = True
                else:
                    if not trace_with_commit_entities:
                        new_subtrace.add_access(access)
                    else:
                        if len(new_subtrace) != 0:
                            subtraces_counts.append(new_subtrace)
                            subtraces_functionalities[new_subtrace].append(functionality.name)
                            subtraces_commits[new_subtrace].append(commit_hash)
                        new_subtrace = Subtrace([tuple(access)], functionality.name)
                        trace_with_commit_entities = False
            subtraces_counts.append(new_subtrace)
            subtraces_functionalities[new_subtrace].append(functionality.name)
            subtraces_commits[new_subtrace].append(commit_hash)

    # print(subtraces_functionalities)

    counter_dict = Counter(subtraces_counts)
    print("Most common subtraces:")
    for mc in counter_dict.most_common(10):
        print(f"{mc[0]}: {mc[1]} ({round(mc[1]/len(subtraces_counts), 3)} %)")
        print(f"  Appears in the following functionalities: {set(subtraces_functionalities[mc[0]])}")
        print(f"  Appears in {len(set(subtraces_commits[mc[0]]))} commits")

    for i in range(2, 21):
        print(f"\nMost common subtraces with size {i}:")
        filtered_data = [x for x in subtraces_counts if len(x) == i]

        for mc in Counter(filtered_data).most_common(10):
            print(f"{mc[0]}: {mc[1]} ({round(mc[1]/len(subtraces_counts), 3)} %)")
            print(f"  Appears in the following functionalities: {set(subtraces_functionalities[mc[0]])}")
            print(f"  Appears in {len(set(subtraces_commits[mc[0]]))} commits")

main()
