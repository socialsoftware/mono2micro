package values

type CodebaseStatuses int

const (
	UnknownCodebaseStatus CodebaseStatuses = iota
	RefactoringCodebase
	CodebaseRefactorComplete
)

var codebaseStatusesNames = []string{
	"UNKNOWN",
	"REFACTORING",
	"COMPLETED",
}

func (status CodebaseStatuses) String() string {
	if status < UnknownCodebaseStatus || status > CodebaseRefactorComplete {
		return "UNKNOWN"
	}
	return codebaseStatusesNames[status]
}

type FunctionalityStatuses int

const (
	UnknownFunctionalityStatus FunctionalityStatuses = iota
	RefactoringFunctionality
	FunctionalityRefactorComplete
	FunctionalityRefactorTimedOut
)

var functionalityStatusesNames = []string{
	"UNKNOWN",
	"REFACTORING",
	"COMPLETED",
	"TIMED_OUT",
}

func (status FunctionalityStatuses) String() string {
	if status < UnknownFunctionalityStatus || status > FunctionalityRefactorTimedOut {
		return "UNKNOWN"
	}
	return functionalityStatusesNames[status]
}
