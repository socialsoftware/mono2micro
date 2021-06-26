package transport

import (
	"context"
	"encoding/json"
	"fmt"
	"net/http"

	"functionality_refactor/app/refactor/values"

	"github.com/go-kit/kit/log"
	"github.com/gorilla/mux"
)

type ViewRefactorizationProvider interface {
	HandleViewRefactorization(context.Context, string, string, string) (*values.RefactorCodebaseResponse, error)
}

type ViewRefactorizationHTTPHandler struct {
	logger log.Logger
	view   ViewRefactorizationProvider
}

func NewViewRefactorizationHandler(logger log.Logger, view ViewRefactorizationProvider) *ViewRefactorizationHTTPHandler {
	return &ViewRefactorizationHTTPHandler{logger: logger, view: view}
}

func (r *ViewRefactorizationHTTPHandler) ServeHTTP(w http.ResponseWriter, req *http.Request) {
	codebase, dendrogram, decomposition, err := r.extractParameters(req)
	if err != nil {
		r.logger.Log("transport", "view/HTTP", "error", err.Error())
		http.Error(
			w,
			err.Error(),
			http.StatusBadRequest,
		)
		return
	}

	response, err := r.view.HandleViewRefactorization(req.Context(), codebase, dendrogram, decomposition)
	if err != nil {
		r.logger.Log("transport", "refactor/handleViewRefactorization", "error", err.Error())
		http.Error(
			w,
			err.Error(),
			http.StatusNotFound,
		)
		return
	}

	data, err := json.Marshal(response)
	if err != nil {
		r.logger.Log("transport", "refactor/marshal", "error", err.Error())
		http.Error(
			w,
			err.Error(),
			http.StatusInternalServerError,
		)
		return
	}

	w.WriteHeader(http.StatusOK)
	w.Header().Set("Content-Type", "application/json")
	w.Write(data)

	r.logger.Log("transport", "refactor/HTTP")
}

func (r *ViewRefactorizationHTTPHandler) extractParameters(req *http.Request) (string, string, string, error) {
	vars := mux.Vars(req)
	codebase, ok := vars["codebase"]
	if !ok {
		err := fmt.Errorf("no codebase provided as a path variable")
		return "", "", "", err
	}

	dendrogram, ok := vars["dendrogram"]
	if !ok {
		err := fmt.Errorf("no dendrogram provided as a path variable")
		return codebase, "", "", err
	}

	decomposition, ok := vars["decomposition"]
	if !ok {
		err := fmt.Errorf("no decomposition provided as a path variable")
		return codebase, dendrogram, "", err
	}

	return codebase, dendrogram, decomposition, nil
}
