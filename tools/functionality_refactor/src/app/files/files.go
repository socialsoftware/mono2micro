package files

import (
	"encoding/json"
	"io/ioutil"
	"os"
	"path/filepath"

	"github.com/go-kit/kit/log"
)

const (
	codebaseFileName   = "/codebase.json"
	idToEntityFileName = "/IDToEntity.json"
)

type FilesHandler interface {
	ReadCodebase(string, string) (*Codebase, error)
	ReadIDToEntityFile(string, string) (map[string]string, error)
}

type DefaultHandler struct {
	logger log.Logger
}

func New(logger log.Logger) FilesHandler {
	return &DefaultHandler{
		logger: log.With(logger, "module", "filesHandler"),
	}
}

func (svc *DefaultHandler) ReadCodebase(codebasesPath string, codebaseFolder string) (*Codebase, error) {
	path, _ := filepath.Abs(codebasesPath + codebaseFolder + codebaseFileName)
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

	var codebase Codebase
	err = json.Unmarshal(byteValue, &codebase)
	if err != nil {
		svc.logger.Log(err)
		return nil, err
	}

	return &codebase, nil
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
