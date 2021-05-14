from typing import List
from ..datasets.helpers import read_dataset, mean, stdev
from ..plots.metrics.plot_reduction_per_merge_percentage import plot_complexity_reduction_per_merge_percentage


class RowData:
    initial_frc_row="Initial Functionality Complexity"
    final_frc_row="Final Functionality Complexity"
    frc_reduction_row="Functionality Complexity Reduction"
    initial_sac_row="Initial System Complexity"
    final_sac_row="Final System Complexity"
    sac_reduction_row="System Complexity Reduction"
    initial_invocations_row="Initial Invocations Count W/ Empties"
    final_invocations_row="Final Invocations Count"
    merges_row="Total Invocation Merges"
    functionality_name="Feature"
    sweeps_row="Total Trace Sweeps w/ Merges"
    initial_accesses_row="Initial Accesses count"
    final_accesses_row="Final Accesses count"


class Extraction:

    def __init__(
        self,
        complexities_csv: str,
        complexities_csv_rows: List[str],
        training_csv: str,
        training_csv_rows: List[str],
        codebase: str = None,
        functionality: str = None,
        features: List[str] = [],
        use_system_complexity: bool = False,
    ):
        self.complexities_csv = complexities_csv
        self.complexities_csv_rows = complexities_csv_rows
        self.training_csv = training_csv
        self.training_csv_rows = training_csv_rows
        self.codebase = codebase
        self.functionality = functionality
        self.features = features

        self.complexities_dataset = None
        self.training_dataset = None
        
        self.rows = RowData()

        self._read_datasets()
    
    def _read_datasets(self):
        self.complexities_dataset = read_dataset(self.complexities_csv, self.complexities_csv_rows, self.functionality, self.codebase)
        self.training_dataset = read_dataset(self.training_csv, self.training_csv_rows, self.functionality, self.codebase)

    def do_stuff(self, best_clusters, other_clusters):
        print("Functionalities: " + str(best_clusters.total_features_including_zero_complexity_reduction))
        
        print("\nInitial FRC:")
        print("Average " + str(mean(best_clusters.initial_frc_complexities)))
        print("Stdev " + str(stdev(best_clusters.initial_frc_complexities)))

        print("\nFinal FRC:")
        print("Average " + str(mean(best_clusters.final_frc_complexities)))
        print("Stdev " + str(stdev(best_clusters.final_frc_complexities)))

        print("\nFRC Reduction %:")
        print("Average " + str(mean(best_clusters.frc_reductions)))
        print("Stdev " + str(stdev(best_clusters.frc_reductions)))


        print("\nInitial SAC:")
        print("Average " + str(mean(best_clusters.initial_sac_complexities)))
        print("Stdev " + str(stdev(best_clusters.initial_sac_complexities)))

        print("\nFinal SAC:")
        print("Average " + str(mean(best_clusters.final_sac_complexities)))
        print("Stdev " + str(stdev(best_clusters.final_sac_complexities)))

        print("\nSAC Reduction %:")
        print("Average " + str(mean(best_clusters.sac_reductions)))
        print("Stdev " + str(stdev(best_clusters.sac_reductions)))


        print("\nInitial invocations count:")
        print("Average " + str(mean(best_clusters.initial_invocations_count)))
        print("Stdev " + str(stdev(best_clusters.initial_invocations_count)))

        print("\nFinal invocations count:")
        print("Average " + str(mean(best_clusters.final_invocations_count)))
        print("Stdev " + str(stdev(best_clusters.final_invocations_count)))

        print("\nMerges count:")
        print("Average " + str(mean(best_clusters.merges)))
        print("Stdev " + str(stdev(best_clusters.merges)))


        print("\nInitial accesses count:")
        print("Average " + str(mean(best_clusters.initial_accesses_count)))
        print("Stdev " + str(stdev(best_clusters.initial_accesses_count)))

        print("\nFinal accesses count:")
        print("Average " + str(mean(best_clusters.final_accesses_count)))
        print("Stdev " + str(stdev(best_clusters.final_accesses_count)))

        print("\nAccess reduction %:")
        print("Average " + str(mean(best_clusters.access_reduction_percentage)))
        print("Stdev " + str(stdev(best_clusters.access_reduction_percentage)))


        print("\nMerge %:")
        print("Average " + str(mean(best_clusters.merge_percentages)))
        print("Stdev " + str(stdev(best_clusters.merge_percentages)))

        print("\nSweeps:")
        print("Average " + str(mean(best_clusters.sweeps)))
        print("Stdev " + str(stdev(best_clusters.sweeps)))


        plot_complexity_reduction_per_merge_percentage(best_clusters)
