test: unit-test integration-test

unit-test:
	go test -timeout=10s -race -benchmem -tags=unit ./...

integration-test:
	go test -timeout=10s -race -benchmem -tags=integration ./...

lint: bin/golangci-lint
	go fmt ./...
	go vet ./...
	bin/golangci-lint -c .golangci.yml run ./...

bin/golangci-lint:
	wget -O- -nv https://raw.githubusercontent.com/golangci/golangci-lint/master/install.sh | sh -s v1.32.0

build:
	./scripts/compile.sh
