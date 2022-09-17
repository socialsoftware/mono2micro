"""
A mixed collection strategy, where the commit data changes the static analysis data.

The algorithm is like so:
- Retrieve, parse, fix the history and store it in a dataframe.
- Compute the similarity of all entities
- For each functionality:
    * Create a similarity matrix with the entities in that functionality
    * Cluster those entities in the best way possible (according to scipy's fcluster method)
    * Create a new functionality for each of the generated clusters
"""
from collections import Counter
from dataclasses import dataclass, field
from functools import lru_cache

from collector.repository import Repository
from collector.service import get_logical_couplings
from helpers.constants import Constants
import json
from scipy.cluster import hierarchy
import numpy as np
from rich import print
import matplotlib.pyplot as plt


@dataclass
class DataAnalysis:
    entities_removed: int = 0
    entities_separated: list[str] = field(default_factory=list)
    pairs_with_split: list[str] = field(default_factory=list)

    def mean_entities_removed_per_functionality(self, number_of_functionalities):
        return self.entities_removed / number_of_functionalities

    def entities_separation_counts(self):
        return Counter(self.entities_separated)

    def pairs_split_counts(self):
        return Counter(self.pairs_with_split)


class StaticFunctionality:
    def __init__(self, accesses, name):
        self.accesses = accesses
        self.name = name
        self.clusters_colors = ["purple", "dark_orange3", "gold1"]

    def __str__(self):
        return f"{self.name} - {len(self.accesses)} accesses"

    def __repr__(self):
        return self.__str__()

    def get_entities_names(self, id_to_entity):
        names = []
        for _id in self.entities_ids():
            names.append(id_to_entity[str(_id)])
        return names

    def add_access(self, access):
        self.accesses.append(access)

    @lru_cache
    def entities_ids(self):
        entities_ids = set()
        for access in self.accesses:
            entities_ids.add(access[1])
        return entities_ids

    def split(self, couplings, id_to_entity, data_analysis, print_actions=True, show_graph=True):
        new_functionalities = []
        entities_in_functionality = set()
        for a in self.accesses:
            entities_in_functionality.add(a[1])
        if len(entities_in_functionality) == 1:
            return [self]

        similarities = []
        for entity1 in entities_in_functionality:
            entity1_similarity = []
            for entity2 in entities_in_functionality:
                if entity1 == entity2:
                    entity1_similarity.append(1)
                else:
                    entity1_similarity.append(
                        len(couplings.loc[(couplings["first_file"] == entity1) & (couplings["second_file"] == entity2)])
                    )
            similarities.append(entity1_similarity)
        matrix = np.array(similarities)
        hierarc = hierarchy.linkage(y=matrix)
        n_clusters = 2
        clustering = hierarchy.fcluster(hierarc, n_clusters, criterion='maxclust')

        # Test if only one cluster exists - situation where it's all 1s
        if np.sum(clustering) == len(clustering):
            if print_actions:
                for access in self.accesses:
                    entity_cluster = clustering[list(entities_in_functionality).index(access[1])]
                    if print_actions:
                        print(
                            f"[{self.clusters_colors[entity_cluster - 1]}] {access} [/{self.clusters_colors[entity_cluster - 1]}]",
                            end="")
                print("")
                print(f"[{self.clusters_colors[0]}] {self.name} [/{self.clusters_colors[0]}] has: "
                      f"{self.get_entities_names(id_to_entity)}")
                print("")
                if show_graph:
                    fig = plt.figure(figsize=(25, 10))
                    hierarchy.dendrogram(hierarc, labels=np.array(
                        [id_to_entity[str(entity_id)] for entity_id in entities_in_functionality]),
                                         distance_sort='descending')
                    plt.title(f"{self.name} - no split occured")
                    plt.show()
            return [self]

        for i in range(n_clusters):
            new_functionalities.append(StaticFunctionality([], f"{self.name}-{i}"))

        if print_actions:
            print(f"{self.name}'s trace is split this way: ")

        for access in self.accesses:
            entity_cluster = clustering[list(entities_in_functionality).index(access[1])]
            new_functionalities[entity_cluster-1].add_access(access)
            if print_actions:
                print(f"[{self.clusters_colors[entity_cluster-1]}] {access} [/{self.clusters_colors[entity_cluster-1]}]", end="")

        smallest_functionality = new_functionalities[0]
        for new_functionality in new_functionalities:
            if len(new_functionality.entities_ids()) < len(smallest_functionality.entities_ids()):
                smallest_functionality = new_functionality

        split_pairs = []
        tmp_new_functionalities = new_functionalities.copy()
        tmp_new_functionalities.remove(smallest_functionality)
        for entities in smallest_functionality.entities_ids():
            for other_functionalities in tmp_new_functionalities:
                for other_entities in other_functionalities.entities_ids():
                    split_pairs.append((id_to_entity[str(entities)], id_to_entity[str(other_entities)]))

        data_analysis.entities_removed += len(smallest_functionality.entities_ids())
        data_analysis.entities_separated += smallest_functionality.get_entities_names(id_to_entity)
        data_analysis.pairs_with_split += split_pairs

        if print_actions:
            print("")
            for i, new_functionality in enumerate(new_functionalities):
                print(f"[{self.clusters_colors[i]}] {new_functionality.name} [/{self.clusters_colors[i]}] has: "
                      f"{new_functionality.get_entities_names(id_to_entity)}")
            print("")
            if show_graph:
                fig = plt.figure(figsize=(25, 10))
                hierarchy.dendrogram(hierarc, labels=np.array(
                    [id_to_entity[str(entity_id)] for entity_id in entities_in_functionality]),
                                     distance_sort='descending')
                plt.title(f"{self.name} - split")
                plt.show()
        return new_functionalities

    def json_format(self):
        return {"t": [{"id": 0, "a": self.accesses}]}


def convert_names_ids(element, repo):
    return [int(repo.get_file_id(element[0])), int(repo.get_file_id(element[1]))]


def parse_full_functionalities(static_analysis_file_path):
    functionalities = []
    with open(static_analysis_file_path, "r") as f:
        data = json.load(f)
    for controller in data:
        accesses = data[controller]["t"][0]["a"]
        functionalities.append(StaticFunctionality(accesses, controller))
    return functionalities


def collect(codebases):
    for codebase in codebases:
        print(f":white_circle: {codebase}")
        print("  :white_circle: Initializing history")
        codebase_repo = Repository(codebase)

        cutoff_value = 100
        print("  :white_circle: Processing history")
        history = codebase_repo.cleanup_history(cutoff_value)

        print("  :white_circle: Parsing functionalities")
        functionalities = parse_full_functionalities(f"{Constants.codebases_data_output_directory}/{codebase}/{codebase}.json")

        print("  :white_circle: Getting coupling data")
        couplings = get_logical_couplings(history)

        print("  :white_circle: Converting to ids")
        couplings_ids = couplings.apply(convert_names_ids, args=[codebase_repo], axis=1, result_type='expand')
        couplings_ids.set_axis(["first_file", "second_file"], axis=1, inplace=True)

        print("  :white_circle: Splitting functionalities")
        final_data_collection = {}
        id_to_entity = codebase_repo.id_to_entity
        data_analysis = DataAnalysis()
        for functionality in functionalities:
            for new_functionality in functionality.split(couplings_ids, id_to_entity, data_analysis, print_actions=True, show_graph=False):
                final_data_collection[new_functionality.name] = new_functionality.json_format()

        print(f"{len(functionalities)} functionalities were split into {len(final_data_collection)} "
              f"({round(len(final_data_collection)/len(functionalities), 2)}x increase)")

        print(f"Average number of entities removed from main trace: "
              f"{data_analysis.mean_entities_removed_per_functionality(len(final_data_collection) - len(functionalities))}")
        print(f"Entities separations counts:")
        print(data_analysis.entities_separation_counts().most_common())

        print("Frequency of pairs that caused at least one of the entities to split")
        print(data_analysis.pairs_split_counts().most_common(10))
        print("")
        with open(f"{Constants.codebases_data_output_directory}/{codebase}/{codebase}-split.json", "w") as f:
            json.dump(final_data_collection, f)


# collect(["quizzes-tutor"])