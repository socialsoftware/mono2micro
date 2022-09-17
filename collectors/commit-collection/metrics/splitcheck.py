"""
This script evaluates and checks the division of functionalities of a codebase.
"""
import json

from helpers.constants import Constants


class ReportData:
    def __init__(self):
        pass


def check_codebase(codebase_name):
    with open(f"{Constants.codebases_data_output_directory}/{codebase_name}/{codebase_name}.json", "r") as f:
        original_static = json.load(f)

    with open(f"{Constants.codebases_data_output_directory}/{codebase_name}/{codebase_name}-split.json", "r") as f:
        split_static = json.load(f)

    new_functionalities_count, old_functionalities_count = len(split_static.keys()), len(original_static.keys())

    print(f"{old_functionalities_count} -> {new_functionalities_count}")

check_codebase("quizzes-tutor")