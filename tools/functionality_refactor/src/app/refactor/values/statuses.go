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

type ControllerStatuses int

const (
	UnknownControllerStatus ControllerStatuses = iota
	RefactoringController
	ControllerRefactorComplete
	ControllerRefactorTimedOut
)

var controllerStatusesNames = []string{
	"UNKNOWN",
	"REFACTORING",
	"COMPLETED",
	"TIMED_OUT",
}

func (status ControllerStatuses) String() string {
	if status < UnknownControllerStatus || status > ControllerRefactorTimedOut {
		return "UNKNOWN"
	}
	return controllerStatusesNames[status]
}
