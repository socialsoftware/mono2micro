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
	HandleRefactorCodebase(context.Context, *values.RefactorCodebaseRequest) (*values.RefactorCodebaseResponse, error)
	HandleViewRefactorization(context.Context, string, string, string) (*values.RefactorCodebaseResponse, error)
}

type DefaultHandler struct {
	logger          log.Logger
	filesHandler    files.FilesHandler
	refactorHandler refactor.RefactorHandler
}

func New(
	logger log.Logger,
	filesHandler files.FilesHandler,
	refactorHandler refactor.RefactorHandler,
) Handler {
	var svc Handler
	svc = &DefaultHandler{
		logger:          log.With(logger, "module", "Handler"),
		filesHandler:    filesHandler,
		refactorHandler: refactorHandler,
	}

	return svc
}

func (svc *DefaultHandler) HandleRefactorCodebase(ctx context.Context, request *values.RefactorCodebaseRequest) (*values.RefactorCodebaseResponse, error) {
	codebase, err := svc.filesHandler.ReadCodebase(request.CodebaseName)
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

	response := svc.refactorHandler.RefactorDecomposition(
		decomposition,
		request,
	)

	return response, nil
}

func (svc *DefaultHandler) HandleViewRefactorization(ctx context.Context, codebaseName string, dendrogramName string, decompositionName string) (*values.RefactorCodebaseResponse, error) {
	return svc.filesHandler.ReadDecompositionRefactorization(codebaseName, dendrogramName, decompositionName)
}
