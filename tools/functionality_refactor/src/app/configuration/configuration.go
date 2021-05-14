package configuration

import "time"

type Execution struct {
	Configuration  *Configuration `json:"configuration,omitempty"`
	ResultsBatches []Results      `json:"results,omitempty"`
}

func (e *Execution) GenerateResults() Results {
	results := Results{
		Datasets: &Datasets{},
	}
	results.GenerateDefaultDatasets()

	e.ResultsBatches = append(e.ResultsBatches, results)
	return results
}

func (e *Execution) GetAverageExecutionTimes() time.Duration {
	var sum time.Duration
	var length int64
	for _, result := range e.ResultsBatches {
		if result.ExecutionTime != 0 {
			sum += result.ExecutionTime
			length += 1
		}
	}

	average_execution_time := int64(sum) / length
	return time.Duration(float32(average_execution_time))
}

type Configuration struct {
	LdodOnly                bool                    `json:"ldod_only,omitempty"`
	OnlyJoaoControllers     bool                    `json:"only_joao_controllers,omitempty"`
	GenerateComplexitiesCSV bool                    `json:"generate_complexities_csv,omitempty"`
	GenerateMetricsCSV      bool                    `json:"generate_metrics_csv,omitempty"`
	Executions              int                     `json:"executions,omitempty"`
	Codebases               []CodebaseConfiguration `json:"codebases,omitempty"`

	// Configuration relating to the execution
	MinimizeSumBothComplexities           bool    `json:"minimize_sum_both_complexities,omitempty"`
	DataDependenceThreshold               int     `json:"data_dependence_threshold,omitempty"`
	ExcludeLowDistanceRedesigns           bool    `json:"exclude_low_distance_redesigns,omitempty"`
	AcceptableComplexityDistanceThreshold float32 `json:"acceptable_complexity_distance_threshold,omitempty"`
	OnlyExportBestRedesign                bool    `json:"only_export_best_redesign,omitempty"`

	// StdOut configurations
	PrintTraces                bool   `json:"print_traces,omitempty"`
	PrintSpecificFunctionality string `json:"print_specific_functionality,omitempty"`
}

func (c *Configuration) GenerateDefaultCodebaseConfiguration() {
	if c.LdodOnly {
		codebasesConfig := []CodebaseConfiguration{
			{
				Name:                    "ldod-static",
				UseExpertDecompositions: true,
			},
		}

		if c.OnlyJoaoControllers {
			codebasesConfig[0].ControllersToRefactor = []string{
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

		c.Codebases = codebasesConfig
		return
	}

	c.Codebases = []CodebaseConfiguration{
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

type CodebaseConfiguration struct {
	Name                    string   `json:"name,omitempty"`
	CutValue                float32  `json:"cut_value,omitempty"`
	UseExpertDecompositions bool     `json:"use_expert_decompositions,omitempty"`
	ControllersToRefactor   []string `json:"controllers_to_refactor,omitempty"`
}

func (c *CodebaseConfiguration) ShouldRefactorController(name string, controllerType string, controllerEntitiesCount int) bool {
	var shouldRefactor bool

	if len(c.ControllersToRefactor) > 0 {
		for _, controllerName := range c.ControllersToRefactor {
			if name == controllerName {
				shouldRefactor = true
			}
		}
		return shouldRefactor
	}

	shouldRefactor = true
	if name == "VirtualEditionController.createTopicModelling" || controllerType == "QUERY" || controllerEntitiesCount <= 2 {
		shouldRefactor = false
	}
	return shouldRefactor
}

type Results struct {
	Datasets      *Datasets     `json:"datasets,omitempty"`
	ExecutionTime time.Duration `json:"execution_times,omitempty"`
}

func (r *Results) GenerateDefaultDatasets() {
	r.Datasets = &Datasets{
		MetricsDataset: [][]string{
			{
				"Codebase",
				"Feature",
				"Cluster",
				//"Entities",
				"CLIP",
				"CRIP",
				"CROP",
				"CWOP",
				"CIP",
				"CDDIP",
				"COP",
				"CPIF",
				"CIOF",
				"SCCP",
				"FCCP",
				"Orchestrator",
			},
		},
		ComplexitiesDataset: [][]string{
			{
				"Codebase",
				"Feature",
				"Orchestrator",
				"Entities",
				"Initial System Complexity",
				"Final System Complexity",
				"System Complexity Reduction",
				"Initial Functionality Complexity",
				"Final Functionality Complexity",
				"Functionality Complexity Reduction",
				"Initial Invocations Count",
				"Initial Invocations Count W/ Empties",
				"Final Invocations Count",
				"Total Invocation Merges",
				"Initial Accesses count",
				"Final Accesses count",
				"Total Trace Sweeps w/ Merges",
				"Clusters with multiple invocations",
				"CLIP",
				"CRIP",
				"CROP",
				"CWOP",
				"CIP",
				"CDDIP",
				"COP",
				"CPIF",
				"CIOF",
				"SCCP",
				"FCCP",
			},
		},
	}
}

type Datasets struct {
	MetricsDataset      [][]string `json:"metrics_dataset,omitempty"`
	ComplexitiesDataset [][]string `json:"complexities_dataset,omitempty"`
}
