package main

import (
	"fmt"
	"net"
	"net/http"
	"os"
	"os/signal"
	"syscall"

	"functionality_refactor/app"
	"functionality_refactor/app/files"
	"functionality_refactor/app/handler"
	"functionality_refactor/app/metrics"
	"functionality_refactor/app/refactor"
	"functionality_refactor/app/training"
	"functionality_refactor/app/transport"

	"functionality_refactor/app/common/log"

	"github.com/oklog/oklog/pkg/group"
)

const (
	httpPort      = ":5001"
	allowedOrigin = "*"
)

func main() {
	logger := log.NewLogger()

	filesHandler := files.New(logger)
	refactorHandler := refactor.New(
		logger,
		metrics.New(logger),
		training.New(logger),
	)

	svc := handler.New(
		logger,
		filesHandler,
		refactorHandler,
		"../../../../../codebases/",
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
