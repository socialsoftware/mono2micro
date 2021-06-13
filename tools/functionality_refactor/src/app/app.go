package app

import (
	"net/http"

	"functionality_refactor/app/common/middlewares"

	"github.com/go-kit/kit/log"
	httpkit "github.com/go-kit/kit/transport/http"
	"github.com/gorilla/mux"
)

type App struct{ handler http.Handler }

type EndpointRegister = func(router *mux.Router, options ...httpkit.ServerOption)

func New(
	logger log.Logger,
	allowedOrigin string,
	endpointRegister ...EndpointRegister,
) App {
	router := mux.NewRouter().StrictSlash(true)

	router.Use(
		middlewares.JSONContentTypeEnforcer,
	)

	v1Router := router.PathPrefix("/api/v1/").Subrouter()
	for _, register := range endpointRegister {
		register(v1Router)
	}

	return App{
		handler: middlewares.CORS(allowedOrigin)(router),
	}
}

func (a App) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	a.handler.ServeHTTP(w, r)
}
