"""
This script checks the number of clusters that are unique.
"""

import json
import os

from helpers.constants import Constants


class Cut:
    def __init__(self, json_cut):
        self.cut = {}

        for key, value in json_cut['clusters'].items():
            self.cut[int(key)] = value

    @property
    def number_of_clusters(self):
        return len(self.cut.keys())

    def __str__(self):
        result = ""
        for i in range(len(self.cut.keys())):
            sorted_entities = sorted(self.cut[i])
            result += f"{i} -> {sorted_entities};"
        return result

    def __hash__(self):
        return hash(str(self))

    def __eq__(self, other_cut):
        if self.number_of_clusters == other_cut.number_of_clusters:
            # Are the entities in each cluster the same?
            for i in range(len(self.cut)):
                if set(self.cut[i]) != set(other_cut.cut[i]):
                    return False
            return True
        else:
            return False


all_cuts = []
for cut in os.listdir(f"{Constants.mono2micro_codebases_root}/edition_all/analyser/cuts/"):
    with open(f"{Constants.mono2micro_codebases_root}/edition_all/analyser/cuts/{cut}", "r") as f:
        all_cuts.append(Cut(json.load(f)))

print(len(all_cuts))
print(len(set(all_cuts)))
