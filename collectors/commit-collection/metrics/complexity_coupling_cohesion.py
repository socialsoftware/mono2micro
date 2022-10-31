import json
import logging
from functools import lru_cache
from itertools import groupby
from operator import itemgetter

import pandas as pd
from funcy import log_durations, print_durations

from helpers.constants import Constants

from rich import print


class Decomposition:
    @print_durations()
    def __init__(self, decomposition_json_path):
        self.clusters = []
        self.entities_cluster_map = {}
        with open(decomposition_json_path, "r") as f:
            decomposition = json.load(f)
            for cluster_id, entities in decomposition['clusters'].items():
                self.clusters.append(Cluster(entities, cluster_id))
                for entity in entities:
                    self.entities_cluster_map[entity] = cluster_id

    @print_durations()
    def complexity(self, static_collection):
        final_complexity = 0
        for controller in static_collection.controllers:
            controller_complexity = 0
            local_transactions = controller.get_local_transactions(self.entities_cluster_map)
            for transaction in local_transactions:
                controller_complexity += static_collection.controllers_reading([a[1] for a in transaction if a[0] == "W"]) + \
                                               static_collection.controllers_writing([a[1] for a in transaction if a[0] == "R"])
            print(f"{controller} has complexity: {controller_complexity}")
            final_complexity += controller_complexity

        return final_complexity


class Cluster:
    def __init__(self, entities, cluster_id):
        self.entities = entities
        self.id = cluster_id


class Controller:
    def __init__(self, name, accesses):
        self.name = name
        self.accesses_df = pd.DataFrame(accesses, columns=["mode", "id"])
        self.accesses = accesses

    def __str__(self):
        return f"{self.name}: {len(self.accesses)}"

    def __repr__(self):
        return self.__str__()

    def get_local_transactions(self, entities_cluster_map):
        local_transactions = []
        new_transaction = []
        accesses_with_clusters = []
        for i in range(1, len(self.accesses)):
            current_access = self.accesses[i]
            current_access_cluster = entities_cluster_map[current_access[1]]
            previous_access = self.accesses[i-1]
            previous_access_cluster = entities_cluster_map[previous_access[1]]

            if entities_cluster_map[self.accesses[i][1]] == entities_cluster_map[self.accesses[i-1][1]]:
                # previous has same cluster as current
                new_transaction.append(self.accesses[i-1])
            else:
                new_transaction.append(self.accesses[i-1])
                local_transactions.append(new_transaction)
                new_transaction = []
        return local_transactions


class StaticCollection:
    def __init__(self, path):
        self.path = path
        self.controllers = []

        with open(path, "r") as f:
            data_collection = json.load(f)
        for controller, trace in data_collection.items():
            accesses = trace["t"][0]["a"]
            self.controllers.append(Controller(controller, accesses))

    def controllers_reading(self, entities):
        count = 0
        for controller in self.controllers:
            count += len(controller.accesses_df.loc[(controller.accesses_df["mode"] == "R") & controller.accesses_df["id"].isin(entities)])
        return count

    def controllers_writing(self, entities):
        count = 0
        for controller in self.controllers:
            count += len(controller.accesses_df.loc[(controller.accesses_df["mode"] == "W") & controller.accesses_df["id"].isin(entities)])
        return count


def compute_complexity():
    static_collection = StaticCollection(f"{Constants.mono2micro_codebases_root}/fenixedu-academic_all/datafile.json")
    decomposition = Decomposition(f"{Constants.mono2micro_codebases_root}/fenixedu-academic_all/analyser/cuts/"
                                  f"20,10,0,10,60,0,5.json")

    print(decomposition.complexity(static_collection))


if __name__ == "__main__":
    compute_complexity()
