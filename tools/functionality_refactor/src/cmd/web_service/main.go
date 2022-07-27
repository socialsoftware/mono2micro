package main

import (
	"fmt"
	"functionality_refactor/app/database"
	"net"
	"net/http"
	"os"
	"os/signal"
	"syscall"

	"functionality_refactor/app"
	"functionality_refactor/app/handler"
	"functionality_refactor/app/metrics"
	"functionality_refactor/app/refactor"
	"functionality_refactor/app/training"
	"functionality_refactor/app/transport"

	"functionality_refactor/app/common/log"

	"github.com/oklog/oklog/pkg/group"
)

const (
	httpPort           = ":5001"
	allowedOrigin      = "*"
	mongodbDefault     = "mongodb://mono2micro:mono2microPass@mongo"
	mongodbNameDefault = "mono2micro"
)

func main() {
	logger := log.NewLogger()

	mongodb := mongodbDefault
	if mdb := os.Getenv("MONGO_DB"); mdb != "" {
		mongodb = mdb
	}

	mongodbName := mongodbNameDefault
	if mdbName := os.Getenv("MONGO_DB_NAME"); mdbName != "" {
		mongodbName = mdbName
	}

	databaseHandler := database.New(logger, mongodb, mongodbName)
	refactorHandler := refactor.New(
		logger,
		metrics.New(logger),
		training.New(logger),
		databaseHandler,
	)

	svc := handler.New(
		logger,
		databaseHandler,
		refactorHandler,
	)

	var g group.Group
	{
		listener, err := net.Listen("tcp", httpPort)
		if err != nil {
			logger.Log("transport", "functionality_refactor/HTTP", "cannot", "Listen", "err", err, "addr", httpPort)
			os.Exit(1)
		}
		logger.Log("service", "functionality_refactor", "addr", httpPort)
		g.Add(func() error {
			http.Handle("/",
				app.New(
					logger,
					allowedOrigin,
					transport.BuildEndpointRegister(
						logger,
						svc,
					),
				),
			)
			return http.Serve(
				listener,
				http.DefaultServeMux,
			)
		}, func(error) {
			listener.Close()
		})
	}
	{ // signal handling
		cancelInterrupt := make(chan struct{})
		g.Add(func() error {
			c := make(chan os.Signal, 1)
			signal.Notify(c, syscall.SIGINT, syscall.SIGTERM)
			select {
			case sig := <-c:
				return fmt.Errorf("received signal %s", sig)
			case <-cancelInterrupt:
				return nil
			}
		}, func(error) {
			close(cancelInterrupt)
		})
	}

	logger.Log("exit", g.Run())
}
