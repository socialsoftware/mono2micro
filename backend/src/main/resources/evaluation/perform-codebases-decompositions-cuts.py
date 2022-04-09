"""
This script is meant to assist in creating the decompositions required for analysis, by taking the human aspect out of
data input.
For each codebase, the analyzer should've already been run so that we can properly create the static decompositions.
"""

import requests
import os
import pandas as pd

from MetadataCreator import create_csv_files

codebases_path = "../../../../../codebases"
base_url = "http://localhost:8080/mono2micro"
create_dendrogram_url = base_url + "/codebase/{}/dendrogram/create"
create_cut_url = base_url + "/codebase/{}/dendrogram/{}/cut"


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
    return os.path.exists(codebases_path + "/" + codebase_name + "/" + get_filename_from_cut(cut))


def cut_exists(cut, codebase_name):
    return os.path.exists(codebases_path + "/" + codebase_name + "/" + get_filename_from_cut(cut) + "/N" + str(cut[-1]))


def main():
    if len(os.listdir("data/")) == 0:
        print("Creating csv files from analyzer...")
        create_csv_files()

    for folder in os.listdir(codebases_path):
        print(f"Codebase: {folder}")
        best_cuts = best_cuts_for_codebase(folder)
        if best_cuts is None:
            print("Something went wrong computing the best cuts. Are the .csv files from the MetadataCreator properly "
                  "formatted?")

        for cut in best_cuts:
            if not dendrogram_exists(cut, folder):
                print("  + Creating Dendrogram with name {}.json...".format(get_filename_from_cut(cut)), end="")
                status = create_dendrogram(cut, folder)
                if status == 201:
                    print("\033[92m Success. \033[0m")
                else:
                    print(f"\033[91m Unexpected status code {status}, was expecting 201. \033[0m")
            else:
                print("  - Dendrogram with name {}.json already exists.".format(get_filename_from_cut(cut)))

            if not cut_exists(cut, folder):
                print("  + Creating cut with {} clusters...".format(cut[-1]), end="")
                status = create_cut(cut, folder)
                if status == 200:
                    print("\033[92m Success. \033[0m")
                else:
                    print(f"\033[91m Unexpected status code {status}, was expecting 200. \033[0m")
            else:
                print("  - Cut with {} clusters already exists.".format(cut[-1]))


main()
