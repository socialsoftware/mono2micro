from modules.datasets.formats import CSV_ROWS, ADAPTED_CSV_ROWS
from modules.configuration.extraction import Extraction
from modules.configuration.orchestrator_data import OrchestratorsData


def main():
    datasets_folder = "../../functionality_refactor/output"

    config = Extraction(
        complexities_csv=f"{datasets_folder}/all-complexities-2021-05-12-17-18-17.csv",
        complexities_csv_rows=CSV_ROWS,
        training_csv=f"{datasets_folder}/all-metrics-2021-05-12-17-18-17.csv",
        training_csv_rows=ADAPTED_CSV_ROWS,
        features=["CLIP", "CRIP", "CROP", "CWOP", "CIP", "CDDIP", "COP", "CPIF"],
        use_system_complexity=False,
    )

    best_clusters = OrchestratorsData(
        config=config,
        row_data=config.rows,
        features=config.features,
    )

    best_clusters.extract_data()

    other_clusters = OrchestratorsData(
        config=config,
        row_data=config.rows,
        features=config.features,
        color="cornflowerblue",
    )

    other_clusters.extract_data(best=False)

    config.do_stuff(best_clusters, other_clusters)


if __name__ == "__main__":
    main()
