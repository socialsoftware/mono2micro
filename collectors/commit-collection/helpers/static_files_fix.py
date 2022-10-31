"""
There are plenty of static data collection files, but they have two problems:
- None is in the most up-to-date format
- Some don't have an id to entity file.
This script fixes the problem for the desired codebases.
"""
import json
import os
import difflib
from rich import print
from functools import lru_cache

from helpers.constants import Constants


@lru_cache()
def get_codebases_of_interest(codebases_root):
    results = []
    for folder in os.listdir(codebases_root):
        if "_adapted" in folder:
            continue
        if ".git" in os.listdir(f"{codebases_root}/{folder}"):
            commit_count = get_commit_count(folder, codebases_root)
            author_count = get_author_count(folder, codebases_root)
            if commit_count >= 100 and author_count > 1:
                # print(f"{folder} has {commit_count} commits and {author_count} authors")
                results.append(folder)

    return results


def update_collection_file(codebase, data_collection_root):
    entity_to_id = {}
    data_collection = {}
    original_data = None
    next_entity_id = 1

    all_jsons = os.listdir(data_collection_root)
    matches = difflib.get_close_matches(codebase + ".json", all_jsons)

    try:
        with open(f"{data_collection_root}{matches[0]}", "r") as f:
            original_data = json.load(f)
    except IndexError:  # No matches found
        return entity_to_id, data_collection

    if original_data is not None:
        for controller_method in original_data.keys():
            accesses = []
            for access in original_data[controller_method]:
                entity_name = access[0]
                access_type = access[1]
                if entity_name in entity_to_id:
                    accesses.append([access_type, entity_to_id[entity_name]])
                else:
                    entity_to_id[entity_name] = next_entity_id
                    accesses.append([access_type, next_entity_id])
                    next_entity_id += 1
            new_trace = {
                "t": [{
                    "id": 0,
                    "a": accesses
                }]
            }
            data_collection[controller_method] = new_trace

    return entity_to_id, data_collection


def reverse_dict(_dict):
    return {v: k for k, v in _dict.items()}


def write_files(entity_to_id, data_collection, output_directory, codebase):
    output_folder = f"{output_directory}/{codebase}"
    if not os.path.isdir(output_folder):
        os.makedirs(output_folder)

    data_collection_location = f"{output_folder}/{codebase}.json"
    entity_to_id_location = f"{output_folder}/{codebase}_entityToID.json"
    id_to_entity_location = f"{output_folder}/{codebase}_IDToEntity.json"

    with open(data_collection_location, "w") as f:
        json.dump(data_collection, f)

    with open(entity_to_id_location, "w") as f:
        json.dump(entity_to_id, f)

    with open(id_to_entity_location, "w") as f:
        json.dump(reverse_dict(entity_to_id), f)

    return True


def get_commit_count(codebase, codebases_root):
    count = os.popen(f"cd {codebases_root}/{codebase} && git rev-list --count HEAD").read()
    if count != '':
        return int(count)
    else:
        return 0


def get_author_count(codebase, codebases_root):
    count = os.popen(f"cd {codebases_root}/{codebase} && git log --pretty='%ae' | sort | uniq | wc -l").read().replace('\n', '')
    return int(count)


def correct_static_files():
    static_data_collection_root = "../../mono2micro-mine/data/static/CodebasesDetails/CodebasesStaticCollectionDatafiles" \
                           "/staticCollectionDatafiles/staticCollectionDatafiles/"
    codebases = get_codebases_of_interest(Constants.codebases_root_directory)
    for codebase in codebases:
        entity_to_id, data_collection = update_collection_file(codebase, static_data_collection_root)
        if len(entity_to_id) == 0:
            print(f"* [red]{codebase}[/red] :cross_mark:")
            continue
        if write_files(entity_to_id, data_collection, Constants.codebases_data_output_directory, codebase):
            print(f"* [green]{codebase}[/green] :heavy_check_mark:")
        else:
            print(f"* [red]{codebase}[/red] :cross_mark:")


if __name__ == "__main__":
    correct_static_files()
