"""
This script compares the complexity, cohesion, and coupling of cuts made using commit analysis and cuts made using
static analysis. The
Since the script assumes that the best cuts and decompositions have already been made, it's ideal to run the
9_static_vs_commit_bestDecompositions.py script before this one.
"""

import os
import json

import pandas as pd
import plotly.express as px

from MetadataCreator import create_csv_files
from perform_codebases_decompositions_cuts import best_cuts_for_codebase, get_filename_from_cut

pd.set_option("display.max_columns", None)
codebases_path = "../../../../../codebases"


def parse_codebase_json(codebase_name):
    codebase_data = {
        "commit-cuts": [],
        "static-cuts": []
    }
    with open(f"{codebases_path}/{codebase_name}/codebase.json", "r") as j:
        data = json.load(j)
        for dendrogram in data["dendrograms"]:
            for decomposition in dendrogram["decompositions"]:
                if dendrogram["base"] == "COMMIT":
                    codebase_data["commit-cuts"].append(
                        {
                            "clusters": decomposition["cutValue"],
                            "complexity": decomposition["complexity"],
                            "performance": decomposition["performance"],
                            "cohesion": decomposition["cohesion"],
                            "coupling": decomposition["coupling"],
                        }
                    )
                else:
                    codebase_data["static-cuts"].append(
                        {
                            "clusters": decomposition["cutValue"],
                            "complexity": decomposition["complexity"],
                            "performance": decomposition["performance"],
                            "cohesion": decomposition["cohesion"],
                            "coupling": decomposition["coupling"],
                        }
                    )
    return codebase_data


def get_max_complexities(codebase_folder, codebase_json):
    max_static_complexity = None
    n_entities = None
    for file in os.listdir("./data/"):
        if codebase_folder in file:
            max_static_complexity = float(file.split("_")[1])
            n_entities = int(file.split("_")[0])

    max_commit_complexity = None
    max_entities_cut = 0
    for cut in codebase_json["commit-cuts"]:
        if int(cut['clusters']) > max_entities_cut:
            max_commit_complexity = cut['complexity']
            max_entities_cut = int(cut['clusters'])

    return max_static_complexity, max_commit_complexity


def main():
    if len(os.listdir("data/")) == 0:
        print("Creating csv files from analyzer...")
        create_csv_files()

    codebases = []
    dendrogram_type = []
    clusters = []
    complexity = []
    pondered_complexity = []
    cohesion = []
    coupling = []

    for codebase_folder in os.listdir(codebases_path):
        if "__latest" not in codebase_folder:
            continue
        print("Reading " + codebase_folder)
        codebase_cuts_info = parse_codebase_json(codebase_folder)
        max_static_complexity, max_commit_complexity = get_max_complexities(codebase_folder, codebase_cuts_info)
        for commit_data, static_data in zip(codebase_cuts_info["commit-cuts"], codebase_cuts_info["static-cuts"]):
            codebases += [codebase_folder] * 2
            complexity.append(commit_data["complexity"])
            complexity.append(static_data["complexity"])
            if max_commit_complexity == 0:
                pondered_complexity.append(0)
            else:
                pondered_complexity.append(commit_data["complexity"]/max_commit_complexity)
            if max_static_complexity == 0:
                pondered_complexity.append(0)
            else:
                pondered_complexity.append(static_data["complexity"]/max_static_complexity)
            cohesion.append(commit_data["cohesion"])
            cohesion.append(static_data["cohesion"])
            coupling.append(commit_data["coupling"])
            coupling.append(static_data["coupling"])
            dendrogram_type += ["commit", "static"]
            clusters += [int(commit_data["clusters"])] * 2

        # Below is how the final dataframe should look like for plotly to display a Cleveland Dot Plot
        # codebase, complexity, type, clusters
        #

        # print(codebases)
        # print(commit_complexity)
        # print(static_complexity)
        # print(dendrogram_type)

    df = pd.DataFrame(
        {
            "Codebase": codebases,
            "Complexity": complexity,
            "PonderedComplexity": pondered_complexity,
            "Dendrogram Base": dendrogram_type,
            "Clusters": clusters,
            "Cohesion": cohesion,
            "Coupling": coupling
        }
    )
    print(df)
    df.to_csv("/home/joaolourenco/Thesis/development/rq1/data/static_commit_metrics.csv")
    complexity_fig = px.scatter(df, x="Codebase", y="Complexity", color="Dendrogram Base",
                                title="Complexity in cuts using "
                                      "commit logs as base vs "
                                      "static analysis as base",
                                facet_col="Clusters", facet_col_wrap=4, log_y=True,
                                labels={"Complexity": "Log Complexity"})
    cohesion_fig = px.scatter(df, x="Codebase", y="Cohesion", color="Dendrogram Base", title="Cohesion in cuts using "
                                                                                             "commit logs as base vs "
                                                                                             "static analysis as base",
                              facet_col="Clusters", facet_col_wrap=4)
    coupling_fig = px.scatter(df, x="Coupling", y="Codebase", color="Dendrogram Base", title="Coupling in cuts using "
                                                                                             "commit logs as base vs "
                                                                                             "static analysis as base",
                              facet_col="Clusters", facet_col_wrap=4)

    # complexity_fig.show()
    # cohesion_fig.show()
    # coupling_fig.show()



main()
