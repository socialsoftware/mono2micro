package handler

import (
	"context"
	"fmt"
	"functionality_refactor/app/files"
	"functionality_refactor/app/refactor"
	"functionality_refactor/app/refactor/values"

	"github.com/go-kit/kit/log"
)

type Handler interface {
	HandleRefactorCodebase(ctx context.Context, configuration *values.RefactorCodebaseRequest) (*values.RefactorCodebaseResponse, error)
}

type DefaultHandler struct {
	logger          log.Logger
	filesHandler    files.FilesHandler
	refactorHandler refactor.RefactorHandler
	codebasesPath   string
}

func New(
	logger log.Logger,
	filesHandler files.FilesHandler,
	refactorHandler refactor.RefactorHandler,
	codebasesPath string,
) Handler {
	var svc Handler
	svc = &DefaultHandler{
		logger:          log.With(logger, "module", "Handler"),
		filesHandler:    filesHandler,
		refactorHandler: refactorHandler,
		codebasesPath:   codebasesPath,
	}

	return svc
}

func (svc *DefaultHandler) HandleRefactorCodebase(ctx context.Context, request *values.RefactorCodebaseRequest) (*values.RefactorCodebaseResponse, error) {
	codebase, err := svc.filesHandler.ReadCodebase(svc.codebasesPath, request.CodebaseName)
	if err != nil {
		svc.logger.Log("codebase", request.CodebaseName, "error", err.Error())
		return nil, fmt.Errorf("failed to read codebase %s", request.CodebaseName)
	}

	dendogram := codebase.GetDendogram(request.DendrogramName)
	if dendogram == nil {
		svc.logger.Log("codebase", request.CodebaseName, "error", "failed to read dendogram")
		return nil, fmt.Errorf("failed to read dendogram %s from codebase %s", request.DendrogramName, request.CodebaseName)
	}

	decomposition := dendogram.GetDecomposition(request.DecompositionName)
	if decomposition == nil {
		svc.logger.Log("codebase", request.CodebaseName, "error", "failed to read decomposition")
		return nil, fmt.Errorf("failed to read decomposition %s from dendogram %s", request.DecompositionName, request.DendrogramName)
	}

	controllers := svc.refactorHandler.RefactorDecomposition(
		decomposition,
		request,
	)

	response := &values.RefactorCodebaseResponse{
		CodebaseName:            codebase.Name,
		DendrogramName:          dendogram.Name,
		DecompositionName:       decomposition.Name,
		Controllers:             controllers,
		DataDependenceThreshold: request.DataDependenceThreshold,
	}

	return response, nil
}
