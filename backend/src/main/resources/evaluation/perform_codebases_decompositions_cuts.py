"""
This script is meant to assist in creating the decompositions required for analysis, by taking the human aspect out of
data input.
For each codebase, the analyzer should've already been run so that we can properly create the static decompositions.
"""
import json

import requests
import os
import pandas as pd

from MetadataCreator import create_csv_files

codebases_path = "../../../../../../rq1/all-codebases-data"
base_url = "http://localhost:8080/mono2micro"
create_dendrogram_url = base_url + "/codebase/{}/dendrogram/create"
create_cut_url = base_url + "/codebase/{}/dendrogram/{}/cut"
create_codebase_url = base_url + "/codebase/create"
analyzer_url = "{}/codebase/{}/analyser"


def get_filename_from_cut(cut):
    return ",".join([str(int(x)) for x in cut])


def best_cuts_for_codebase(codebase_name: str):
    cuts = []
    all_codebase_cuts = None
    for file in os.listdir("data/"):
        if codebase_name in file:
            all_codebase_cuts = pd.read_csv(f"data/{file}")
            break

    if all_codebase_cuts is None:
        return

    for n in range(3, 11):
        min_weights = []
        min_complexity = float("inf")
        min_complexity_weights = []  # a, w, r, s
        for entry in all_codebase_cuts.values:
            if entry[0] != n:
                continue

            if entry[8] <= min_complexity:
                min_complexity = entry[8]
                min_complexity_weights = [entry[1], entry[2], entry[3], entry[4]]
                min_weights.append([entry[1], entry[2], entry[3], entry[4]])

        if min_complexity == float("inf"):  # no entries for this N
            continue

        cuts.append(min_complexity_weights + [n])

    return cuts


def create_dendrogram(cut, codebase_name):
    data = {
        "codebaseName": codebase_name,
        "name": get_filename_from_cut(cut),
        "base": "STATIC",
        "linkageType": "average",
        "accessMetricWeight": cut[0],
        "writeMetricWeight": cut[1],
        "readMetricWeight": cut[2],
        "sequenceMetricWeight": cut[3],
        "profile": "Generic",
        "tracesMaxLimit": 0,
        "traceType": "ALL"
    }
    r = requests.post(url=create_dendrogram_url.format(codebase_name), json=data)
    return r.status_code


def create_cut(cut, codebase_name):
    data = {
        "codebaseName": codebase_name,
        "dendrogramName": get_filename_from_cut(cut),
        "expert": False,
        "cutValue": cut[-1],
        "cutType": "N"
    }
    r = requests.post(url=create_cut_url.format(codebase_name, get_filename_from_cut(cut)), json=data)
    return r.status_code


def dendrogram_exists(cut, codebase_name):
    base_path = "../../../../../codebases"
    return os.path.exists(base_path + "/" + codebase_name + "/" + get_filename_from_cut(cut))


def cut_exists(cut, codebase_name):
    base_path = "../../../../../codebases"
    return os.path.exists(base_path + "/" + codebase_name + "/" + get_filename_from_cut(cut) + "/N" + str(cut[-1]))


def do_static_analysis_cuts(best_cuts, codebase_name):
    for cut in best_cuts:
        if not dendrogram_exists(cut, codebase_name):
            print("  + Creating Dendrogram with name {}...".format(get_filename_from_cut(cut)), end="")
            status = create_dendrogram(cut, codebase_name)
            if status == 201:
                print("\033[92m Success. \033[0m")
            else:
                print(f"\033[91m Unexpected status code {status}, was expecting 201. \033[0m")
        else:
            print("  - Dendrogram with name {} already exists.".format(get_filename_from_cut(cut)))

        if not cut_exists(cut, codebase_name):
            print("  + Creating cut with {} clusters...".format(cut[-1]), end="")
            status = create_cut(cut, codebase_name)
            if status == 200:
                print("\033[92m Success. \033[0m")
            else:
                print(f"\033[91m Unexpected status code {status}, was expecting 200. \033[0m")
        else:
            print("  - Cut with {} clusters already exists.".format(cut[-1]))


def create_commit_dendrogram(codebase_name):
    data = {
        "codebaseName": codebase_name,
        "name": "commit",
        "base": "COMMIT",
        "linkageType": "average",
        "accessMetricWeight": 25,
        "writeMetricWeight": 25,
        "readMetricWeight": 25,
        "sequenceMetricWeight": 25,
        "commitMetricWeight": 100,
        "profile": "Generic",
        "tracesMaxLimit": 0,
        "traceType": "ALL"
    }
    r = requests.post(url=create_dendrogram_url.format(codebase_name), json=data)
    return r.status_code


def create_commit_cut(cut, codebase_name):
    data = {
        "codebaseName": codebase_name,
        "dendrogramName": "commit",
        "expert": False,
        "cutValue": cut[-1],
        "cutType": "N"
    }

    r = requests.post(url=create_cut_url.format(codebase_name, "commit"), json=data)
    return r.status_code


def commit_dendrogram_exists(codebase_name):
    base_path = "../../../../../codebases"
    return os.path.exists(base_path + "/" + codebase_name + "/commit")


def commit_cut_exists(cut, codebase_name):
    base_path = "../../../../../codebases"
    return os.path.exists(base_path + "/" + codebase_name + "/commit/N" + str(cut[-1]))


def do_commit_analysis_cuts(best_cuts, codebase_name):
    # There is only one dendrogram for all cuts, so create it if it does not exist
    if not commit_dendrogram_exists(codebase_name):
        print("  + Creating commit Dendrogram...", end="")
        status = create_commit_dendrogram(codebase_name)
        if status == 201:
            print("\033[92m Success. \033[0m")
        else:
            print(f"\033[91m Unexpected status code {status}, was expecting 201. \033[0m")
    else:
        print("  - Commit Dendrogram already exists.")

    for cut in best_cuts:
        if not commit_cut_exists(cut, codebase_name):
            print("  + Creating cut with {} clusters...".format(cut[-1]), end="")
            status = create_commit_cut(cut, codebase_name)
            if status == 200:
                print("\033[92m Success. \033[0m")
            else:
                print(f"\033[91m Unexpected status code {status}, was expecting 200. \033[0m")
        else:
            print("  - Cut with {} clusters already exists.".format(cut[-1]))

    # Perform max complexity cut
    base_path = "../../../../../codebases"
    with open(f"{base_path}/{codebase_name}/IDToEntity.json", "r") as f:
        id_to_entity = json.load(f)
        n_clusters_max = len(id_to_entity.keys())
        max_complexity_cut = [n_clusters_max]
        print("  + Creating max complexity cut with {} clusters...".format(n_clusters_max), end="")
        status = create_commit_cut(max_complexity_cut, codebase_name)
        if status == 200:
            print("\033[92m Success. \033[0m")
        else:
            print(f"\033[91m Unexpected status code {status}, was expecting 200. \033[0m")

def create_codebase(codebase_name):
    data = {
        'codebaseName': codebase_name + "__latest"
    }
    files = {
        'datafile': open(f"{codebases_path}/{codebase_name}/{codebase_name}.json"),
        'translationFile': open(f"{codebases_path}/{codebase_name}/{codebase_name}_IDToEntity.json"),
        'commitFile': open(f"{codebases_path}/{codebase_name}/{codebase_name}-commit.json"),
        'authorFile': open(f"{codebases_path}/{codebase_name}/{codebase_name}-authors.json")
    }
    r = requests.post(create_codebase_url, files=files, data=data)
    return r.status_code


def run_analyzer(codebase_name):
    data = {
        "expert": {},
        "profile": "Generic",
        "requestLimit": 0,
        "traceType": 0,
        "tracesMaxLimit": 0,
    }
    print("URL: " + analyzer_url.format(base_url, codebase_name))
    r = requests.post(analyzer_url.format(base_url, codebase_name + "__latest"), json=data)
    return r.status_code


def main():
    if len(os.listdir("data/")) != len(list(os.listdir(codebases_path))):
        print("Creating csv files from analyzer...")
        create_csv_files()

    for folder in os.listdir(codebases_path):

        print(f"Codebase: {folder}")
        # print("\033[1m Running analyser codebase \033[0m")
        # status = run_analyzer(folder)
        # if status == 200:
        #     print("\033[92m Success. \033[0m")
        # else:
        #     print(f"\033[91m Unexpected status code {status}, was expecting 200. \033[0m")

        # print("\033[1m Creating codebase \033[0m")
        # status = create_codebase(folder)
        # if status == 200:
        #     print("\033[92m Success. \033[0m")
        # else:
        #     print(f"\033[91m Unexpected status code {status}, was expecting 200. \033[0m")

        print("\033[1m Static Dendrograms/Cuts: \033[0m")
        best_cuts = best_cuts_for_codebase(folder + "__latest")
        if best_cuts is None:
            print("Something went wrong computing the best cuts. Are the .csv files from the MetadataCreator properly "
                  "formatted?")

        do_static_analysis_cuts(best_cuts, folder + "__latest")

        print("\033[1m Commit Dendrograms/Cuts: \033[0m")
        do_commit_analysis_cuts(best_cuts, folder + "__latest")


if __name__ == "__main__":
    main()
