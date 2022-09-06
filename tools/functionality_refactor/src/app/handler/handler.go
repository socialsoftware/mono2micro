package handler

import (
	"context"
	"fmt"
	"functionality_refactor/app/database"
	"functionality_refactor/app/refactor"
	"functionality_refactor/app/refactor/values"

	"github.com/go-kit/kit/log"
)

type Handler interface {
	HandleRefactorCodebase(context.Context, *values.RefactorCodebaseRequest) (*values.RefactorCodebaseResponse, error)
	HandleViewRefactorization(context.Context, string) (*values.RefactorCodebaseResponse, error)
}

type DefaultHandler struct {
	logger          log.Logger
	refactorHandler refactor.RefactorHandler
	databaseHandler database.DatabaseHandler
}

func New(
	logger log.Logger,
	databaseHandler database.DatabaseHandler,
	refactorHandler refactor.RefactorHandler,
) Handler {
	var svc Handler
	svc = &DefaultHandler{
		logger:          log.With(logger, "module", "Handler"),
		databaseHandler: databaseHandler,
		refactorHandler: refactorHandler,
	}

	return svc
}

func (svc *DefaultHandler) HandleRefactorCodebase(ctx context.Context, request *values.RefactorCodebaseRequest) (*values.RefactorCodebaseResponse, error) {
	decomposition, err := svc.databaseHandler.ReadDecomposition(ctx, request.DecompositionName)
	if err != nil {
		svc.logger.Log("decomposition", request.DecompositionName, "error", "failed to read decomposition")
		return nil, fmt.Errorf("failed to read decomposition %s", request.DecompositionName)
	}

	response := svc.refactorHandler.RefactorDecomposition(
		ctx,
		decomposition,
		request,
	)

	return response, nil
}

func (svc *DefaultHandler) HandleViewRefactorization(ctx context.Context, decompositionName string) (*values.RefactorCodebaseResponse, error) {
	return svc.databaseHandler.ReadDecompositionRefactorization(ctx, decompositionName)
}
