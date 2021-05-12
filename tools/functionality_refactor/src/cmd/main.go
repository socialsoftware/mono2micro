package main

import (
	"app/common/log"
	"app/files"
	"app/metrics"
	"app/redesign"
	"app/training"
	"fmt"
	"time"
)

const (
	ldodOnly                = false
	onlyJoaoControllers     = false
	generateMetricsCSV      = true
	generateComplexitiesCSV = true
	executions              = 1
)

type CodebaseData struct {
	Name                    string   `json:"name,omitempty"`
	CutValue                float32  `json:"cut_value,omitempty"`
	UseExpertDecompositions bool     `json:"use_expert_decompositions,omitempty"`
	Controllers             []string `json:"controllers,omitempty"`
}

func main() {
	logger := log.NewLogger()
	filesHandler := files.New(logger)
	redesignHandler := redesign.New(
		logger,
		metrics.New(logger),
		training.New(logger),
	)

	execution_times := make([]time.Duration, executions)

	for i := 0; i < executions; i++ {
		codebasesData := initializeCodebaseData()

		fmt.Printf("Estimating the best saga redesign for %v codebases...\n\n", len(codebasesData))

		csvData := files.InitializeDatasets()

		start := time.Now()

		for _, codebaseData := range codebasesData {
			codebase, err := filesHandler.ReadCodebase(codebaseData.Name)
			if err != nil {
				logger.Log("Failed to decode codebase %s | %s", codebaseData.Name, err.Error())
				continue
			}

			idToEntityMap, err := filesHandler.ReadIDToEntityFile(codebaseData.Name)
			if err != nil {
				logger.Log("Failed to decode id_to_entity map %s | %s", codebaseData.Name, err.Error())
				continue
			}

			datasets := redesignHandler.EstimateCodebaseOrchestrators(
				codebase,
				idToEntityMap,
				codebaseData.CutValue,
				codebaseData.UseExpertDecompositions,
				codebaseData.Controllers,
			)

			if generateComplexitiesCSV {
				for _, row := range datasets.ComplexitiesDataset {
					csvData.ComplexitiesDataset = append(csvData.ComplexitiesDataset, row)
				}
			}

			if generateMetricsCSV {
				for _, row := range datasets.MetricsDataset {
					csvData.MetricsDataset = append(csvData.MetricsDataset, row)
				}
			}

			fmt.Printf("Finished estimation for codebase %v\n", codebase.Name)
		}

		execution_times = append(execution_times, time.Since(start))
		generateCSVFiles(filesHandler, csvData)
	}

	fmt.Printf("\nDone!\n")
	fmt.Printf("\nExecution time: %s\n", execution_times)

	var sum time.Duration
	var length int64
	for _, value := range execution_times {
		if value != 0 {
			sum += value
			length += 1
		}
	}

	average_execution_time := int64(sum) / length
	fmt.Printf("\nAverage execution time: %s\n", time.Duration(average_execution_time))
}

func initializeCodebaseData() []CodebaseData {
	if ldodOnly {
		codebasesData := []CodebaseData{
			{
				Name:                    "ldod-static",
				UseExpertDecompositions: true,
			},
		}

		if onlyJoaoControllers {
			codebasesData[0].Controllers = []string{
				"VirtualEditionController.approveParticipant",
				"VirtualEditionController.mergeCategories",
				"FragmentController.getTaxonomy",
				"AdminController.removeTweets",
				"RecommendationController.createLinearVirtualEdition",
				"VirtualEditionController.dissociate",
				"VirtualEditionController.deleteTaxonomy",
				"SignupController.signup",
				"VirtualEditionController.associateCategory",
			}
		}

		return codebasesData
	}

	return []CodebaseData{
		{
			Name:                    "ldod-static",
			UseExpertDecompositions: true,
		},
		{
			Name:     "acme_cng_maven",
			CutValue: 4.0,
		},
		{
			Name:     "Acme-Academy-2.0-maven",
			CutValue: 5.0,
		},
		{
			Name:     "Acme-AnimalShelter-maven",
			CutValue: 8.0,
		},
		{
			Name:     "Acme-Certifications-maven",
			CutValue: 3.0,
		},
		{
			Name:     "Acme-Champions-maven",
			CutValue: 12.0,
		},
		{
			Name:     "Acme-Chollos-Rifas-maven",
			CutValue: 8.0,
		},
		{
			Name:     "Acme-Chorbies-maven",
			CutValue: 5.0,
		},
		{
			Name:     "Acme-CinemaDB-maven",
			CutValue: 4.0,
		},
		{
			Name:     "Acme-Conference-maven",
			CutValue: 5.0,
		},
		{
			Name:     "Acme-Events-maven",
			CutValue: 8.0,
		},
		{
			Name:     "Acme-Explorer-maven",
			CutValue: 6.0,
		},
		{
			Name:     "Acme-Food-launcher",
			CutValue: 5.0,
		},
		{
			Name:     "acme-furniture-launcher",
			CutValue: 6.0,
		},
		{
			Name:     "Acme-Gallery-maven",
			CutValue: 6.0,
		},
		{
			Name:     "Acme-Hacker-Rank-maven",
			CutValue: 5.0,
		},
		{
			Name:     "Acme-HandyWorker-maven",
			CutValue: 8.0,
		},
		{
			Name:     "Acme-Inmigrant-maven",
			CutValue: 4.0,
		},
		{
			Name:     "Acme-Meals-maven",
			CutValue: 6.0,
		},
		{
			Name:     "Acme-Newspaper-maven",
			CutValue: 4.0,
		},
		{
			Name:     "Acme-Parade-maven",
			CutValue: 6.0,
		},
		{
			Name:     "Acme-Patronage-maven",
			CutValue: 3.0,
		},
		{
			Name:     "Acme-Personal-Trainer-maven",
			CutValue: 7.0,
		},
		{
			Name:     "Acme-Pet-maven",
			CutValue: 6.0,
		},
		{
			Name:     "Acme-Polyglot-2.0-maven",
			CutValue: 3.0,
		},
		{
			Name:     "Acme-Recycling-maven",
			CutValue: 8.0,
		},
		{
			Name:     "Acme-Rendezvous-maven",
			CutValue: 5.0,
		},
		{
			Name:     "Acme-Restaurante-maven",
			CutValue: 8.0,
		},
		{
			Name:     "Acme-Rookie-mave",
			CutValue: 10.0,
		},
		{
			Name:     "Acme-Santiago-maven",
			CutValue: 7.0,
		},
		{
			Name:     "Acme-Series-maven",
			CutValue: 5.0,
		},
		{
			Name:     "Acme-Six-Pack-maven",
			CutValue: 3.0,
		},
		{
			Name:     "Acme-Supermarket-maven",
			CutValue: 8.0,
		},
		{
			Name:     "Acme-Taxi-maven",
			CutValue: 6.0,
		},
		{
			Name:     "Acme-Trip-maven",
			CutValue: 5.0,
		},
		{
			Name:     "Acme-Un-Viaje-maven",
			CutValue: 7.0,
		},
		{
			Name:     "AcmeDistributor-maven",
			CutValue: 3.0,
		},
		{
			Name:     "AcmeShop-maven",
			CutValue: 11.0,
		},
		{
			Name:     "alfut-maven",
			CutValue: 3.0,
		},
		{
			Name:     "AppPortal-maven",
			CutValue: 11.0,
		},
		{
			Name:     "APMHome-maven",
			CutValue: 4.0,
		},
		{
			Name:     "AppCan-coopMan",
			CutValue: 7.0,
		},
		{
			Name:     "bag-database_adapted-maven",
			CutValue: 4.0,
		},
		{
			Name:     "blog-maven",
			CutValue: 4.0,
		},
		{
			Name:     "bookstore-spring-maven",
			CutValue: 3.0,
		},
		{
			Name:     "cheybao-maven",
			CutValue: 5.0,
		},
		{
			Name:     "cloudstreetmarket.com-maven",
			CutValue: 5.0,
		},
		{
			Name:     "cloudunit-maven",
			CutValue: 3.0,
		},
		// {
		// 	Name:     "cms_wanzi-maven",
		// 	CutValue: 7.0,
		// },
		{
			Name:     "Corpore-Fit-maven",
			CutValue: 6.0,
		},
		// {
		// 	Name:     "CPIS_hindi-maven",
		// 	CutValue: 13.0,
		// },
		{
			Name:     "Curso-Systema-Web-brewer-maven",
			CutValue: 4.0,
		},
		{
			Name:     "echo-maven",
			CutValue: 6.0,
		},
		{
			Name:     "extremeworld-maven",
			CutValue: 13.0,
		},
		{
			Name:     "FirstWebShop-maven",
			CutValue: 3.0,
		},
		{
			Name:     "hrm_backend-maven",
			CutValue: 16.0,
		},
		{
			Name:     "incubator-wikift-jar",
			CutValue: 4.0,
		},
		{
			Name:     "JavaSpringMvcBlog-maven",
			CutValue: 3.0,
		},
		{
			Name:     "keta-custom-launcher",
			CutValue: 3.0,
		},
		{
			Name:     "learndemo-soufang-maven",
			CutValue: 3.0,
		},
		{
			Name:     "Logos-ShopingCartUnregisteredUser-maven",
			CutValue: 4.0,
		},
		{
			Name:     "maven-project-maven",
			CutValue: 11.0,
		},
		{
			Name:     "myweb-maven",
			CutValue: 3.0,
		},
		{
			Name:     "quizzes-tutor-launcher",
			CutValue: 3.0,
		},
		{
			Name:     "reddit-app-maven",
			CutValue: 3.0,
		},
		{
			Name:     "soad-maven",
			CutValue: 7.0,
		},
		{
			Name:     "SoloMusic-maven",
			CutValue: 4.0,
		},
		{
			Name:     "springblog-maven",
			CutValue: 3.0,
		},
		{
			Name:     "StudyOnlinePlatForm-maven",
			CutValue: 3.0,
		},
		{
			Name:     "TwitterAutomationWebApp-maven",
			CutValue: 3.0,
		},
		{
			Name:     "webofneeds-maven",
			CutValue: 4.0,
		},
		{
			Name:     "wish-maven",
			CutValue: 3.0,
		},
		{
			Name:     "WJKJ-center-admin-maven",
			CutValue: 10.0,
		},
		{
			Name:     "xs2a-maven",
			CutValue: 3.0,
		},
		{
			Name:     "zsymvp_shatou-maven",
			CutValue: 11.0,
		},
	}
}

func generateCSVFiles(filesHandler files.FilesHandler, datasets *files.Datasets) {
	identifier := "all"
	if ldodOnly {
		identifier = "ldod"
	}

	if generateComplexitiesCSV {
		t := time.Now()
		outputFileName := fmt.Sprintf("%s-complexities-%s.csv", identifier, t.Format("2006-01-02-15-04-05"))
		fmt.Printf("\nGenerating complexities .csv: %v\n", outputFileName)
		filesHandler.GenerateCSV(outputFileName, datasets.ComplexitiesDataset)
	}

	if generateMetricsCSV {
		t := time.Now()
		outputFileName := fmt.Sprintf("%s-metrics-%s.csv", identifier, t.Format("2006-01-02-15-04-05"))
		fmt.Printf("\nGenerating metrics .csv: %v\n", outputFileName)
		filesHandler.GenerateCSV(outputFileName, datasets.MetricsDataset)
	}
}
