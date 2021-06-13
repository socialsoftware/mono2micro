package middlewares

import (
	"net/http"

	"github.com/rs/cors"
)

func CORS(allowedOrigin string) func(http.Handler) http.Handler {
	return func(next http.Handler) http.Handler {
		config := cors.New(cors.Options{
			AllowedOrigins: []string{allowedOrigin},
			AllowedMethods: []string{
				http.MethodGet,
				http.MethodPost,
				http.MethodPut,
				http.MethodPatch,
				http.MethodDelete,
				http.MethodOptions,
				http.MethodHead,
			},
			AllowedHeaders: []string{"*"},
		})

		return config.Handler(
			optionShortCircuit(next),
		)
	}
}

func optionShortCircuit(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if r.Method == http.MethodOptions {
			return
		}
		next.ServeHTTP(w, r)
	})
}
