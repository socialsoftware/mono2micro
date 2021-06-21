package transport

import (
	"context"
	"encoding/json"
	"fmt"
	"net/http"

	"functionality_refactor/app/refactor/values"

	"github.com/go-kit/kit/log"
)

type RefactorCodebaseProvider interface {
	HandleRefactorCodebase(ctx context.Context, req *values.RefactorCodebaseRequest) (*values.RefactorCodebaseResponse, error)
}

type RefactorCodebaseHTTPHandler struct {
	logger   log.Logger
	refactor RefactorCodebaseProvider
}

func NewRefactorCodebaseHandler(logger log.Logger, refactor RefactorCodebaseProvider) *RefactorCodebaseHTTPHandler {
	return &RefactorCodebaseHTTPHandler{logger: logger, refactor: refactor}
}

func (r *RefactorCodebaseHTTPHandler) ServeHTTP(w http.ResponseWriter, req *http.Request) {
	var payload values.RefactorCodebaseRequest
	err := json.NewDecoder(req.Body).Decode(&payload)
	if err != nil {
		r.logger.Log("transport", "refactor/decode", "error", err.Error())
		http.Error(w, err.Error(), http.StatusBadRequest)
		return
	}

	err = r.validateRequest(&payload)
	if err != nil {
		r.logger.Log("transport", "refactor/validate", "error", err.Error())
		http.Error(
			w,
			err.Error(),
			http.StatusBadRequest,
		)
		return
	}

	response, err := r.refactor.HandleRefactorCodebase(req.Context(), &payload)
	if err != nil {
		r.logger.Log("transport", "refactor/handleRefactorCodebase", "error", err.Error())
		http.Error(
			w,
			err.Error(),
			http.StatusInternalServerError,
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

func (r *RefactorCodebaseHTTPHandler) validateRequest(req *values.RefactorCodebaseRequest) error {
	if req.CodebaseName == "" {
		return fmt.Errorf("A codebase name must be set.")
	}
	if req.DecompositionName == "" {
		return fmt.Errorf("A decomposition name must be set.")
	}
	if req.DendrogramName == "" {
		return fmt.Errorf("A dendrogram name must be set.")
	}
	return nil
}
