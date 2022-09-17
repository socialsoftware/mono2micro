"""
This script gathers some meta data on the codebases.
Data obtained:
- number of commits
- number of authors
- latest hash
- number of entities
- number of functionalities
- avg number of commits/day
"""
import json
import os

import pandas as pd
from helpers.constants import Constants
import subprocess
from io import StringIO
from datetime import datetime

def get_data(codebases):
    data = []
    for i, codebase_data in enumerate(codebases):

        commit_count = int(os.popen(f"cd {Constants.codebases_root_directory}/{codebase_data[0]} && git rev-list --count HEAD").read())

        command = f"{Constants.project_root}/collector/commit_log_script.sh {Constants.codebases_root_directory}/{codebase_data[0]}"
        try:
            output = subprocess.check_output(command, stderr=subprocess.STDOUT, shell=True, universal_newlines=True)
            history_df = pd.read_csv(StringIO(output), sep=";", names=[
                'commit_hash', 'change_type', 'previous_filename', 'filename', 'timestamp', 'author'])
        except subprocess.CalledProcessError as e:
            print("Error retrieving history.")
            print(e.output)
        print(codebase_data[0])

        with open(f"{Constants.codebases_data_output_directory}/{codebase_data[0]}/{codebase_data[0]}_IDToEntity.json",
                  "r") as e:
            n_entities = len(json.load(e).keys())
        with open(f"{Constants.codebases_data_output_directory}/{codebase_data[0]}/{codebase_data[0]}.json", "r") as f:
            n_functionalities = len(json.load(f).keys())

        development_days = (datetime.fromtimestamp(history_df.tail(1)["timestamp"].iloc[0]) -
                            datetime.fromtimestamp(history_df.head(1)["timestamp"].iloc[0])).days

        data.append([codebase_data[0], commit_count, len(history_df['author'].unique()),
                     n_entities, n_functionalities, len(history_df['commit_hash'].unique())/development_days, development_days])
    return pd.DataFrame(data, columns=["name", "commit", "author", "entities", "functionalities", "commitsperday", "devdays"])
