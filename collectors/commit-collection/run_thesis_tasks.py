import json
import csv

import pandas as pd
import requests

from rich.console import Console
from rich import print

from helpers import metadata
from mono2micro.interface import parse_analyser_result
from collector import service as collector
from helpers.constants import Constants


def single_cut(commit_weight, author_weight, clusters, codebase_name):
    r = requests.post(f'http://localhost:8080/mono2micro/codebase/{codebase_name}/{commit_weight}/{author_weight}/{clusters}/analyserCut')
    return r.status_code


def codebase_entities(codebase_name):
    with open(f"{Constants.codebases_data_output_directory}/{codebase_name}/{codebase_name}_IDToEntity.json", "r") as f:
        return len(json.load(f).keys())


def merge_analyser_csvs(codebases):
    dfs = []
    for codebase_data in codebases:
        codebase = codebase_data[0]
        print("Merging " + codebase)
        codebase_df = pd.read_csv(f"{Constants.codebases_data_output_directory}/{codebase}/analyserResult.csv")
        codebase_df['codebase_name'] = codebase
        dfs.append(codebase_df)
    all_together = pd.concat(dfs)
    all_together.to_csv(f"{Constants.resources_directory}/analyserCompilationCorrectMetrics.csv", index=False)


def convert_single_analyser_json(mono2micro_codebase_name, codebase_name):
    with open(f"{Constants.mono2micro_codebases_root}/{mono2micro_codebase_name}/analyser/analyserResult.json", "r") as f:
        analyser_result = parse_analyser_result(json.load(f))
    max_complexity = analyser_result['complexity'].max()
    if max_complexity != 0:
        analyser_result['pondered_complexity'] = analyser_result['complexity'] / max_complexity
        analyser_result = analyser_result.loc[analyser_result['complexity'] != max_complexity]
    else:
        analyser_result['pondered_complexity'] = 0
    analyser_result.to_csv(
        f"{Constants.codebases_data_output_directory}/{codebase_name}-analyser.csv",
        index=False)


def main():
    console = Console()
    codebases = []
    with open(f"{Constants.resources_directory}/codebases.csv", "r") as c:
        reader = csv.reader(c)
        next(reader)
        codebases = [row for row in reader]

    # Below are the different actions that can be taken with this script.
    # Could be separated in commands using a CLI interface.

    # console.rule("Gathering statistics")
    # collector.codebases_statistics(codebases)

    # console.rule("Running commit collection")
    # force_recollection = True
    # collector.collect_data(codebases, force_recollection)

    # console.rule("Creating codebases in Mono2Micro")
    # interface.create_codebases(codebases, "")

    # console.rule("Running analyser")
    # # print("Total: " + str(len(codebases)))
    # t0 = time.time()
    # interface.run_analyser(codebases, "")
    # t1 = time.time()
    # print(f"Total time: {datetime.timedelta(t1-t0)}")

    # console.rule("Merging csvs")
    # merge_analyser_csvs(codebases)

    # console.rule("Getting tsr")
    # tsr.get_data(codebases)

    console.rule("Getting statistics")
    data = metadata.get_data(codebases)
    data.to_csv(f"{Constants.resources_directory}/statistics.csv",index=False)


if __name__ == "__main__":
    main()
