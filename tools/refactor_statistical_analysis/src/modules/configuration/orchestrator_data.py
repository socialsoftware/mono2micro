from typing import List
from .extraction import Extraction, RowData


class OrchestratorsData:
    
    def __init__(self, config: Extraction, row_data: RowData, features: List[str], color="red"):
        self.config = config
        self.row_data = row_data
        self.features = features

        self.total_features_including_zero_complexity_reduction = 0

        self.metrics = {}
        self.initial_frc_complexities = []
        self.final_frc_complexities = []
        self.frc_reductions = []
        self.initial_sac_complexities = []
        self.final_sac_complexities = []
        self.sac_reductions = []
        self.functionality_names = []
        self.initial_invocations_count = []
        self.final_invocations_count = []
        self.merge_percentages = []
        self.initial_accesses_count = []
        self.final_accesses_count = []
        self.access_reduction_percentage = []
        self.merges = []
        self.sweeps = []
        self.color = color

        for metric in self.features:
            self.metrics[metric] = []
    
    def extract_data(self, best: bool = True):
        for idx in self.config.complexities_dataset.index:
            adapted_cluster = self.config.training_dataset.query(
                f'Feature == "{self.config.complexities_dataset["Feature"][idx]}" and Cluster == {self.config.complexities_dataset["Orchestrator"][idx]}'
            )
            if len(adapted_cluster.index) == 0:
                continue

            if best and adapted_cluster["Orchestrator"][adapted_cluster.index[0]] == 1:
                self._set_metrics(idx)

            elif not best:
                self._set_metrics(idx)
    
    def _set_metrics(self, index: int):
        complexities_dataset = self.config.complexities_dataset

        reduction_percentage = (
            complexities_dataset[self.row_data.frc_reduction_row][index] * 100
        )/complexities_dataset[self.row_data.initial_frc_row][index]

        self.total_features_including_zero_complexity_reduction += 1

        if reduction_percentage <= 0:
            return
        
        self.initial_frc_complexities.append(complexities_dataset[self.row_data.initial_frc_row][index])
        self.final_frc_complexities.append(complexities_dataset[self.row_data.final_frc_row][index])

        self.frc_reductions.append(reduction_percentage)

        self.initial_sac_complexities.append(complexities_dataset[self.row_data.initial_sac_row][index])
        self.final_sac_complexities.append(complexities_dataset[self.row_data.final_sac_row][index])

        reduction_percentage = (
            complexities_dataset[self.row_data.sac_reduction_row][index] * 100
        )/complexities_dataset[self.row_data.initial_sac_row][index]

        self.sac_reductions.append(reduction_percentage)

        self.merges.append(complexities_dataset[self.row_data.merges_row][index])
        self.merge_percentages.append(
            (
                complexities_dataset[self.row_data.merges_row][index]*100
            )/complexities_dataset[self.row_data.initial_invocations_row][index]
        )

        self.functionality_names.append(complexities_dataset[self.row_data.functionality_name][index])

        self.initial_invocations_count.append(complexities_dataset[self.row_data.initial_invocations_row][index])
        self.final_invocations_count.append(complexities_dataset[self.row_data.final_invocations_row][index])

        self.sweeps.append(complexities_dataset[self.row_data.sweeps_row][index])

        for metric in self.features:
            self.metrics[metric].append(self.config.complexities_dataset[metric][index])
        
        self.initial_accesses_count.append(complexities_dataset[self.row_data.initial_accesses_row][index])
        self.final_accesses_count.append(complexities_dataset[self.row_data.final_accesses_row][index])

        access_reduction = complexities_dataset[self.row_data.initial_accesses_row][index] - complexities_dataset[self.row_data.final_accesses_row][index]
        self.access_reduction_percentage.append(
            (
                access_reduction*100
            )/complexities_dataset[self.row_data.initial_accesses_row][index]
        )
