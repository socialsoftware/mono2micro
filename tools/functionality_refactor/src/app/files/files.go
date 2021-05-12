package files

import (
	"encoding/csv"
	"encoding/json"
	"io/ioutil"
	"os"
	"path/filepath"

	"github.com/go-kit/kit/log"
)

const (
	codebaseFolderPath = "../../codebases/"
	codebaseFileName   = "/codebase.json"
	idToEntityFileName = "/IDToEntity.json"
	outputPath         = "../../output/"
)

type FilesHandler interface {
	ReadCodebase(string) (*Codebase, error)
	ReadIDToEntityFile(string) (map[string]string, error)
	GenerateCSV(string, [][]string) error
}

type DefaultHandler struct {
	logger log.Logger
}

func New(logger log.Logger) FilesHandler {
	return &DefaultHandler{
		logger: log.With(logger, "module", "filesHandler"),
	}
}

func (svc *DefaultHandler) ReadCodebase(codebaseFolder string) (*Codebase, error) {
	path, _ := filepath.Abs(codebaseFolderPath + codebaseFolder + codebaseFileName)
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

func (svc *DefaultHandler) ReadIDToEntityFile(codebaseFolder string) (map[string]string, error) {
	path := codebaseFolderPath + codebaseFolder + idToEntityFileName
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

func (svc *DefaultHandler) GenerateCSV(filename string, data [][]string) error {
	path := outputPath + filename

	file, err := os.Create(path)
	if err != nil {
		svc.logger.Log(err)
		return err
	}

	defer file.Close()

	writer := csv.NewWriter(file)
	defer writer.Flush()

	for _, value := range data {
		err := writer.Write(value)
		if err != nil {
			svc.logger.Log(err)
			return err
		}
	}

	return nil
}
