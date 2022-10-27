import json
import time
from collections import defaultdict

import pandas as pd
from funcy import print_durations

from helpers.constants import Constants
from metrics.complexity import Controller, add_access_to_accesses_controllers, init_decomposition, \
    get_controllers_to_clusters


def load_raw_data(codebase_name, cut_name):
    with open(f"{Constants.mono2micro_codebases_root}/{codebase_name}/analyser/cuts/"
              f"{cut_name}", "r") as f:
        clusters = json.load(f)["clusters"]
    with open(f"{Constants.mono2micro_codebases_root}/{codebase_name}/datafile.json", "r") as f:
        data_collection = json.load(f)

    return clusters, data_collection


def iter_data_collection(data_collection):
    for controller in data_collection:
        yield controller, data_collection[controller]["t"][0]["a"]


def controllers_costly_accesses(entity_id_to_cluster_id, controller_data):
    controllers = {}
    accesses_controllers = defaultdict(list)
    for controller_name, group in controller_data.groupby("name"):
        accesses = group[["mode", "entity_id"]].to_numpy()
        entity_id_to_mode = {}
        previous_cluster = '-2'
        controller = Controller(controller_name)
        for i, access in enumerate(accesses):
            entity_id = access[1]
            # R == 0, W == 1, RW == 2
            if access[0] == "R":
                mode = 0
            else:
                mode = 1
            cluster = entity_id_to_cluster_id.get(entity_id, None)
            if cluster is None:
                print(f"Entity {entity_id} was not assigned to a cluster, abort.")
                exit(0)
            if i == 0:
                entity_id_to_mode[entity_id] = mode
                added_entity = controller.add_entity(entity_id, mode)
                if added_entity is not None:
                    add_access_to_accesses_controllers(added_entity[0], added_entity[1], controller,
                                                       accesses_controllers)
            else:
                if cluster == previous_cluster:
                    has_cost = False
                    saved_mode = entity_id_to_mode.get(entity_id, None)
                    if saved_mode is None or (saved_mode == 0 and mode == 1):
                        has_cost = True
                    if has_cost:
                        entity_id_to_mode[entity_id] = mode
                        added_entity = controller.add_entity(entity_id, mode)
                        if added_entity is not None:
                            add_access_to_accesses_controllers(added_entity[0], added_entity[1], controller,
                                                               accesses_controllers)
                else:
                    added_entity = controller.add_entity(entity_id, mode)
                    if added_entity is not None:
                        add_access_to_accesses_controllers(added_entity[0], added_entity[1], controller,
                                                           accesses_controllers)
                    entity_id_to_mode = {entity_id: mode}
            previous_cluster = cluster
        if len(controller.entities) > 0:
            controllers[controller.name] = controller
    return controllers, accesses_controllers


def get_controller_complexity(param, controller, controllers_clusters_map):
    pass

@print_durations
def compute_complexity(decomposition, controller_data):
    complexity = 0
    all_local_transactions_sets = {}
    for controller, group in controller_data.groupby("name"):
        local_transaction_set = get_local_transactions_set(controller, group, decomposition)
        all_local_transactions_sets[controller] = local_transaction_set

    controllers_clusters_map = get_controllers_to_clusters(decomposition.clusters, decomposition.controllers.values())
    for controller in decomposition.controllers.keys():
        if len(controllers_clusters_map[decomposition.controllers[controller]]) == 1:
            continue
        complexity += decomposition.get_controller_complexity(all_local_transactions_sets[controller], controller,
                                                     controllers_clusters_map)

        # for transaction in all_local_transactions_sets[controller]:
        #     # Find next local transactions, add cluster dependencies

    complexity /= len(controllers_clusters_map)
    return complexity


class LocalTransaction:
    def __init__(self, cluster_id, new_cluster_access, first_accessed_entity_id):
        self.cluster_id = cluster_id
        self.cluster_accesses = set()
        self.cluster_accesses.add(tuple(new_cluster_access))
        self.cluster_accesses_entities = set()
        self.cluster_accesses_entities.add(new_cluster_access[1])
        self.first_accessed_entity_ids = {first_accessed_entity_id}

    def add_cluster_access(self, a):
        self.cluster_accesses.add(tuple(a))
        self.cluster_accesses_entities.add(a[1])


def get_local_transactions_set(controller, accesses, decomposition):
    first_accessed_cluster_id = None
    local_transaction_sequence = []
    entity_id_to_mode = {}
    current_local_transaction = None
    for a in accesses[["mode", "entity_id"]].to_numpy():
        entity_id = a[1]
        if a[0] == "R":
            mode = 0
        else:
            mode = 1
        current_cluster_id = decomposition.entity_id_to_cluster_id[entity_id]
        if current_cluster_id is None:
            print(f"{current_cluster_id} is not assigned to a cluster")
        if first_accessed_cluster_id is None:
            first_accessed_cluster_id = current_cluster_id

        if current_local_transaction is None:
            current_local_transaction = LocalTransaction(current_cluster_id, [mode, entity_id], entity_id)
            entity_id_to_mode[entity_id] = mode
        else:
            if current_cluster_id == current_local_transaction.cluster_id:
                has_cost = False
                saved_mode = entity_id_to_mode.get(entity_id, None)
                if saved_mode is None or (saved_mode == 0 and mode == 1):
                    has_cost = True
                if has_cost:
                    current_local_transaction.add_cluster_access([mode, entity_id])
                    entity_id_to_mode[entity_id] = mode
            else:
                local_transaction_sequence.append(current_local_transaction)
                current_local_transaction = LocalTransaction(current_cluster_id, [mode, entity_id], entity_id)
                entity_id_to_mode = {entity_id: mode}
    if current_local_transaction is not None and len(current_local_transaction.cluster_accesses) > 0:
        local_transaction_sequence.append(current_local_transaction)

    return local_transaction_sequence


def main():
    clusters, data_collection = load_raw_data("fenixedu-academic_all", "20,10,0,10,60,0,5.json")
    controller_data = []
    for controller, accesses in iter_data_collection(data_collection):
        for access in accesses:
            controller_data.append((controller, access[1], access[0]))

    decomposition = init_decomposition(clusters)

    controller_data = pd.DataFrame(controller_data, columns=["name", "entity_id", "mode"])
    cols = ["name", "entity_id", "mode"]
    controller_data = controller_data[cols].loc[(controller_data[cols].shift() != controller_data[cols]).any(axis=1)]

    # print(controller_data.groupby("name").get_group('ProjectSubmissionDispatchAction.submitProject'))
    decomposition.controllers, decomposition.accesses_controllers = controllers_costly_accesses(
        decomposition.entity_id_to_cluster_id, controller_data)
    # print(costly_accesses)
    t0 = time.time()
    complexity = compute_complexity(decomposition, controller_data)
    t1 = time.time()
    print(f"Final Complexity: {complexity}")
    print(f"Complexity calculation took {t1-t0} seconds.")


main()
