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
	decomposition, err := svc.filesHandler.ReadDecomposition(request.CodebaseName, request.StrategyName, request.DecompositionName)
	if err != nil {
		svc.logger.Log("codebase", request.CodebaseName, "error", "failed to read decomposition")
		return nil, fmt.Errorf("failed to read decomposition %s from strategy %s of codebase %s", request.DecompositionName, request.StrategyName, request.CodebaseName)
	}

	response := svc.refactorHandler.RefactorDecomposition(
		decomposition,
		request,
	)

	return response, nil
}

func (svc *DefaultHandler) HandleViewRefactorization(ctx context.Context, codebaseName string, strategyName string, decompositionName string) (*values.RefactorCodebaseResponse, error) {
	return svc.filesHandler.ReadDecompositionRefactorization(codebaseName, strategyName, decompositionName)
}
