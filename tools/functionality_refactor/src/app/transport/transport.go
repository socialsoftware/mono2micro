package transport

import (
	"functionality_refactor/app"
	"functionality_refactor/app/handler"

	"github.com/go-kit/kit/log"
	httpkit "github.com/go-kit/kit/transport/http"
	"github.com/gorilla/mux"
)

func BuildEndpointRegister(
	logger log.Logger,
	svc handler.Handler,
) app.EndpointRegister {
	return func(router *mux.Router, options ...httpkit.ServerOption) {

		router.Methods("POST").
			Path("/refactor").
			Handler(NewRefactorCodebaseHandler(logger, svc))

	}
}
