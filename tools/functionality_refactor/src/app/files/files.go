package files

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"os"
	"path/filepath"
	"sync"

	"functionality_refactor/app/mono2micro"
	"functionality_refactor/app/refactor/values"

	"github.com/go-kit/kit/log"
)

const (
	codebaseFileName              = "/codebase.json"
	refactorizationFileNameFormat = "/refactorization_%s_%s.json"
	idToEntityFileName            = "/IDToEntity.json"
)

var fileMutex sync.Mutex

type FilesHandler interface {
	ReadCodebase(string) (*mono2micro.Codebase, error)
	ReadDecompositionRefactorization(string, string, string) (*values.RefactorCodebaseResponse, error)
	StoreDecompositionRefactorization(*values.RefactorCodebaseResponse) error
	UpdateControllerRefactorization(string, string, string, *values.Controller) error
	ReadIDToEntityFile(string, string) (map[string]string, error)
}

type DefaultHandler struct {
	logger        log.Logger
	codebasesPath string
}

func New(logger log.Logger, codebasesPath string) FilesHandler {
	return &DefaultHandler{
		logger:        log.With(logger, "module", "filesHandler"),
		codebasesPath: codebasesPath,
	}
}

func (svc *DefaultHandler) ReadCodebase(codebaseFolder string) (*mono2micro.Codebase, error) {
	path, _ := filepath.Abs(svc.codebasesPath + codebaseFolder + codebaseFileName)
	byteValue, err := svc.readFile(path)
	if err != nil {
		svc.logger.Log(err)
		return nil, err
	}

	var codebase mono2micro.Codebase
	err = json.Unmarshal(byteValue, &codebase)
	if err != nil {
		svc.logger.Log(err)
		return nil, err
	}

	return &codebase, nil
}

func (svc *DefaultHandler) ReadDecompositionRefactorization(codebaseFolder string, dendrogramName string, decompositionName string) (*values.RefactorCodebaseResponse, error) {
	refactorizationFileName := fmt.Sprintf(refactorizationFileNameFormat, dendrogramName, decompositionName)

	path, _ := filepath.Abs(svc.codebasesPath + codebaseFolder + refactorizationFileName)
	byteValue, err := svc.readFile(path)
	if err != nil {
		svc.logger.Log(err)
		return nil, err
	}

	var refactotization values.RefactorCodebaseResponse
	err = json.Unmarshal(byteValue, &refactotization)
	if err != nil {
		svc.logger.Log(err)
		return nil, err
	}

	return &refactotization, nil
}

func (svc *DefaultHandler) StoreDecompositionRefactorization(refactorization *values.RefactorCodebaseResponse) error {
	file, err := json.MarshalIndent(refactorization, "", " ")
	if err != nil {
		svc.logger.Log(err)
		return err
	}

	refactorizationFileName := fmt.Sprintf(refactorizationFileNameFormat, refactorization.DendrogramName, refactorization.DecompositionName)

	path, _ := filepath.Abs(svc.codebasesPath + refactorization.CodebaseName + refactorizationFileName)
	return ioutil.WriteFile(path, file, 0644)
}

func (svc *DefaultHandler) UpdateControllerRefactorization(codebaseName string, dendrogramName string, decompositionName string, refactorization *values.Controller) error {
	// we lock the mutex so that only one goroutine reads and updates the file at a time
	fileMutex.Lock()
	defer fileMutex.Unlock()

	codebase, err := svc.ReadDecompositionRefactorization(codebaseName, dendrogramName, decompositionName)
	if err != nil {
		svc.logger.Log(err)
		return err
	}

	codebase.Controllers[refactorization.Name] = refactorization
	return svc.StoreDecompositionRefactorization(codebase)
}

func (svc *DefaultHandler) ReadIDToEntityFile(codebasesPath string, codebaseFolder string) (map[string]string, error) {
	path := codebasesPath + codebaseFolder + idToEntityFileName
	jsonFile, err := os.Open(path)
	if err != nil {
		svc.logger.Log(err)
		return nil, err
	}

	defer jsonFile.Close()

	byteValue, err := ioutil.ReadAll(jsonFile)
	if err != nil {
		svc.logger.Log(err)
		return nil, err
	}

	var idToEntityMap map[string]string
	err = json.Unmarshal(byteValue, &idToEntityMap)
	if err != nil {
		svc.logger.Log(err)
		return nil, err
	}

	return idToEntityMap, nil
}

func (svc *DefaultHandler) readFile(path string) ([]byte, error) {
	jsonFile, err := os.Open(path)
	if err != nil {
		svc.logger.Log(err)
		return nil, err
	}

	defer jsonFile.Close()

	return ioutil.ReadAll(jsonFile)
}
