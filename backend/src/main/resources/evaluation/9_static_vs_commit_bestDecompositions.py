"""
This script compares decompositions made using commits as base with decompositions made using static analysis as base.
The comparison itself is done with a Java program that is executed from this script.
"""
import os
import json

import pandas as pd
from py4j.java_gateway import JavaGateway
import plotly.express as px

from MetadataCreator import create_csv_files
from perform_codebases_decompositions_cuts import best_cuts_for_codebase, get_filename_from_cut


codebases_path = "../../../../../codebases"
DISTR_SRC_FILE_PATH = '../../java/pt/ist/socialsoftware/mono2micro/utils/mojoCalculator/src/main/resources/distrSrc.rsf'
DISTR_TARGET_FILE_PATH = '../../java/pt/ist/socialsoftware/mono2micro/utils/mojoCalculator/src/main/resources' \
                         '/distrTarget.rsf'


def get_cluster_of_cut(cut, codebase_name, is_static):
    if is_static:
        with open(codebases_path + "/" + codebase_name + "/" + get_filename_from_cut(cut) + "/N" + str(cut[-1]) + "/clusters.json", "r") as f:
            cluster = json.load(f)
    else:
        with open(codebases_path + "/" + codebase_name + "/commit/N" + str(cut[-1]) + "/clusters.json", "r") as f:
            cluster = json.load(f)
    return cluster


def format_cluster(cluster):
    end_string = ""
    for cluster_id in cluster.keys():
        for entity_id in cluster[cluster_id]:
            end_string += f"contain {cluster_id} {entity_id}\n"
    return end_string


def write_files(static_cluster_mojo_info, commit_cluster_mojo_info):
    with open(DISTR_SRC_FILE_PATH, "w+") as src:
        src.write(static_cluster_mojo_info)

    with open(DISTR_TARGET_FILE_PATH, "w+") as target:
        target.write(commit_cluster_mojo_info)


def execute_mojo_script():
    try:
        gateway = JavaGateway()
        result = gateway.entry_point.runMoJo()
    except Exception:
        print("Warning: Entry point for the MoJoFM calculator not running")
        raise SystemExit
    return result


def main():
    if len(os.listdir("data/")) == 0:
        print("Creating csv files from analyzer...")
        create_csv_files()

    data = []

    for folder in os.listdir(codebases_path):
        best_cuts = best_cuts_for_codebase(folder)
        for cut in best_cuts:
            print(f"Computing MoJo for {folder}, N = {cut[-1]}")
            static_cut_cluster = get_cluster_of_cut(cut, folder, is_static=True)
            commit_cut_cluster = get_cluster_of_cut(cut, folder, is_static=False)
            write_files(format_cluster(static_cut_cluster), format_cluster(commit_cut_cluster))
            result = execute_mojo_script()
            print(f"Result: {result}")
            data.append([folder, cut[-1], result])

    df = pd.DataFrame(data)
    df.columns = ["codebase", "clusters", "mojo"]

    df.to_csv("data/mojo-comparison.csv", index=False)

    fig = px.box(df, x="clusters", y="mojo", points="all")
    fig.show()


main()
