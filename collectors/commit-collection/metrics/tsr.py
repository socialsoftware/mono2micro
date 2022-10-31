import json
import os
import difflib
import pandas as pd
from scipy.cluster import hierarchy
from rich import print
from helpers.constants import Constants


def get_total_authors_count(authors_data):
    authors = list(authors_data.values())
    authors_set = set()
    for author_list in authors:
        for author in author_list:
            authors_set.add(author)
    return len(authors_set)


def compute_average_contributors_per_microservice(decomposition, commit_data, authors_data):
    authors_per_cluster_sum = 0
    authors_per_cluster = []
    files = list(commit_data.keys())
    for cluster in decomposition.keys():
        contributors_in_this_cluster = []
        for file_id in decomposition[cluster]:
            try:
                contributors_in_this_cluster += authors_data[files[int(file_id)]]
            except KeyError:
                pass
        authors_per_cluster_sum += len(set(contributors_in_this_cluster))
        authors_per_cluster.append(contributors_in_this_cluster)
    return authors_per_cluster_sum / len(decomposition.keys())


def contributors_per_microservice(clusters_four_data, author_data, n_clusters):
    authors_per_cluster_sum = 0
    authors_per_cluster = []

    for cluster in clusters_four_data['clusters'].keys():
        contributors_in_this_cluster = []
        for file_id in clusters_four_data['clusters'][cluster]:
            try:
                contributors_in_this_cluster += author_data[str(file_id)]
            except KeyError:
                pass
        authors_per_cluster_sum += len(set(contributors_in_this_cluster))
        authors_per_cluster.append(contributors_in_this_cluster)

    return authors_per_cluster_sum / n_clusters


def get_all_clusters_files(codebase):
    static_clusters = []
    commit_clusters = []
    both_clusters = []
    for decomposition in os.listdir(f"{Constants.mono2micro_codebases_root}/{codebase}/analyser/cuts/"):
        decomposition_path = f"{Constants.mono2micro_codebases_root}/{codebase}/analyser/cuts/{decomposition}"
        access, write, read, sequence, commit, authors, n_clusters = [int(x) for x in decomposition.replace(".json", "").split(",")]
        if n_clusters > 10:
            continue
        if (access > 0 or write > 0 or read > 0 or sequence > 0) and (commit == 0 and authors == 0):
            static_clusters.append((n_clusters, access, write, read, sequence, commit, authors, decomposition_path))
        elif (access == 0 and write == 0 and read == 0 and sequence == 0) and (commit > 0 or authors > 0):
            commit_clusters.append((n_clusters, access, write, read, sequence, commit, authors, decomposition_path))
        else:
            both_clusters.append((n_clusters, access, write, read, sequence, commit, authors, decomposition_path))
    return static_clusters, commit_clusters, both_clusters


def get_tsr_data_for_clusters(clusters_files, source, author_data, codebase):
    data = []
    for cluster in clusters_files:
        with open(cluster[-1], "r") as f:
            cluster_data = json.load(f)
        n_monolith_authors = get_total_authors_count(author_data)
        cpm = contributors_per_microservice(cluster_data, author_data, int(cluster[0]))
        tsr = cpm / n_monolith_authors
        data.append([codebase, tsr, *(cluster[:7]), source])
    return data


def get_data(codebases_of_interest):
    data = []
    for codebase_data in codebases_of_interest:
        codebase = codebase_data[0]
        print(f":white_circle: Evaluating {codebase}")
        with open(f"{Constants.codebases_data_output_directory}/{codebase}/{codebase}_author.json", "r") as f:
            author_data = json.load(f)

        print(f"  :white_circle: Getting cluster information")
        static_only_clusters, commit_only_clusters, both_clusters = get_all_clusters_files(codebase)

        print(f"  :white_circle: Computing tsr for static decompositions")
        data += get_tsr_data_for_clusters(static_only_clusters, "static", author_data, codebase)

        print(f"  :white_circle: Computing tsr for commit decompositions")
        data += get_tsr_data_for_clusters(commit_only_clusters, "commit", author_data, codebase)

        print(f"  :white_circle: Computing tsr for both types of decompositions")
        data += get_tsr_data_for_clusters(both_clusters, "both", author_data, codebase)
        # print(data)

    data = pd.DataFrame(data, columns=["codebase_name", "tsr", "n_clusters", "access", "write", "read", "sequence", "commit", "authors", "type"])
    data.to_csv(f"{Constants.codebases_data_output_directory}/tsrResultCorrectMetrics.csv", index=False)
    return data
