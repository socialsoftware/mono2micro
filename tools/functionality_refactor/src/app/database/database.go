package database

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"functionality_refactor/app/mono2micro"
	"functionality_refactor/app/refactor/values"
	"github.com/go-kit/kit/log"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/gridfs"
	"go.mongodb.org/mongo-driver/mongo/options"
	"sync"
)

var fileMutex sync.Mutex

type DatabaseHandler interface {
	ReadDecomposition(context.Context, string) (*mono2micro.Decomposition, error)
	ReadDecompositionRefactorization(context.Context, string) (*values.RefactorCodebaseResponse, error)
	UpdateFunctionalityRefactorization(string, *values.Functionality) error
	StoreDecompositionRefactorization(context.Context, *values.RefactorCodebaseResponse) error
}

type DefaultHandler struct {
	logger      log.Logger
	mongodb     string
	mongodbName string
}

func New(logger log.Logger, mongodb string, mongodbName string) DatabaseHandler {
	return &DefaultHandler{
		logger:      log.With(logger, "module", "filesHandler"),
		mongodb:     mongodb,
		mongodbName: mongodbName,
	}
}

func (svc *DefaultHandler) GetDbClient(ctx context.Context) (*mongo.Client, error) {
	client, err := mongo.Connect(ctx, options.Client().ApplyURI(svc.mongodb))
	if err != nil {
		svc.logger.Log("database", "mongodb", "cannot", "connect", err, "addr", svc.mongodb)
		return nil, err
	}
	return client, err
}

func (svc *DefaultHandler) CloseDbClient(ctx context.Context, client *mongo.Client) {
	err := client.Disconnect(ctx)
	if err != nil {
		svc.logger.Log("database", "mongodb", "error", "disconnect", err, "addr", svc.mongodb)
	}
}

func (svc *DefaultHandler) ReadDecompositionRefactorization(ctx context.Context, decompositionName string) (*values.RefactorCodebaseResponse, error) {
	client, err := svc.GetDbClient(ctx)
	defer svc.CloseDbClient(ctx, client)
	if err != nil {
		return nil, err
	}

	refactorizationFileName := fmt.Sprintf(decompositionName + "_refactorization")

	bucket, err := gridfs.NewBucket(client.Database(svc.mongodbName))
	if err != nil {
		return nil, err
	}

	var buffer bytes.Buffer
	var response values.RefactorCodebaseResponse
	if _, err := bucket.DownloadToStreamByName(refactorizationFileName, &buffer); err != nil {
		return nil, err
	}
	if err := json.Unmarshal(buffer.Bytes(), &response); err != nil {
		return nil, err
	}

	return &response, nil
}

func (svc *DefaultHandler) RemoveDecompositionRefactorization(ctx context.Context, decompositionName string) error {
	client, err := svc.GetDbClient(ctx)
	defer svc.CloseDbClient(ctx, client)
	if err != nil {
		return err
	}

	fileName := decompositionName + "_refactorization"

	cursor, err := client.Database(svc.mongodbName).Collection("fs.files").Find(ctx, bson.D{{"filename", fileName}})
	if err != nil {
		return err
	}

	var results []bson.M
	if err = cursor.All(ctx, &results); err != nil {
		return err
	}
	for _, result := range results {
		bucket, err := gridfs.NewBucket(client.Database(svc.mongodbName))
		if err != nil {
			return err
		}

		if err = bucket.Delete(result["_id"]); err != nil {
			return err
		}
	}
	return nil
}

func (svc *DefaultHandler) StoreDecompositionRefactorization(ctx context.Context, refactorization *values.RefactorCodebaseResponse) error {
	client, err := svc.GetDbClient(ctx)
	defer svc.CloseDbClient(ctx, client)
	if err != nil {
		return err
	}

	refactorizationFileName := fmt.Sprintf(refactorization.DecompositionName + "_refactorization")

	bucket, err := gridfs.NewBucket(client.Database(svc.mongodbName))
	if err != nil {
		return err
	}

	uploadStream, err := bucket.OpenUploadStream(refactorizationFileName)
	if err != nil {
		return err
	}
	defer uploadStream.Close()
	buffer, err := json.MarshalIndent(refactorization, "", " ")
	if err != nil {
		return err
	}

	if _, err = uploadStream.Write(buffer); err != nil {
		return err
	}

	return nil
}

func (svc *DefaultHandler) UpdateFunctionalityRefactorization(decompositionName string, functionality *values.Functionality) error {
	// we lock the mutex so that only one goroutine reads and updates the file at a time
	fileMutex.Lock()
	defer fileMutex.Unlock()

	refactorization, err := svc.ReadDecompositionRefactorization(context.TODO(), decompositionName)
	if err != nil {
		return err
	}
	refactorization.Functionalities[functionality.Name] = functionality

	if err := svc.RemoveDecompositionRefactorization(context.TODO(), refactorization.DecompositionName); err != nil {
		return err
	}

	if err := svc.StoreDecompositionRefactorization(context.TODO(), refactorization); err != nil {
		return err
	}
	return nil
}

func (svc *DefaultHandler) ReadDecomposition(ctx context.Context, decompositionName string) (*mono2micro.Decomposition, error) {
	client, err := svc.GetDbClient(ctx)
	defer svc.CloseDbClient(ctx, client)
	if err != nil {
		return nil, err
	}

	var databaseDecomposition Decomposition
	err = client.Database(svc.mongodbName).Collection("decomposition").FindOne(ctx, bson.D{{"_id", decompositionName}}).Decode(&databaseDecomposition)
	if err != nil {
		return nil, err
	}
	decomposition := newDecomposition(databaseDecomposition)

	functionalityCollection := client.Database(svc.mongodbName).Collection("functionality")
	bucket, err := gridfs.NewBucket(client.Database(svc.mongodbName))
	if err != nil {
		return nil, err
	}

	var representationInformation RepresentationInformations
	for _, representationInfo := range databaseDecomposition.RepresentationInformations {
		if len(representationInfo.Functionalities) != 0 {
			representationInformation = representationInfo
			break
		}
	}

	functionalities := map[string]*mono2micro.Functionality{}
	for _, dbref := range representationInformation.Functionalities {
		var databaseFunctionality Functionality
		err = functionalityCollection.FindOne(ctx, bson.D{{"_id", dbref.ID}}).Decode(&databaseFunctionality)
		if err != nil {
			return nil, err
		}
		functionalities[databaseFunctionality.Name], err = newFunctionality(bucket, databaseFunctionality)
		if err != nil {
			return nil, err
		}
	}
	decomposition.Functionalities = functionalities

	return decomposition, err
}

func newDecomposition(databaseDecomposition Decomposition) *mono2micro.Decomposition {
	decomposition := mono2micro.Decomposition{
		Name:       databaseDecomposition.Name,
		Expert:     databaseDecomposition.Expert,
		Complexity: databaseDecomposition.Metrics["Complexity"],
		Coupling:   databaseDecomposition.Metrics["Coupling"],
		Cohesion:   databaseDecomposition.Metrics["Cohesion"],
	}

	clusters := map[string]*mono2micro.Cluster{}
	entityIDToClusterName := map[int]string{}

	for key, cluster := range databaseDecomposition.Clusters {
		var entities []mono2micro.Entity
		for _, entity := range cluster.Entities {
			entities = append(entities, newEntity(entity))
			entityIDToClusterName[entity.Id] = cluster.Name
		}

		clusters[key] = &mono2micro.Cluster{
			Name:                 cluster.Name,
			Complexity:           cluster.Complexity,
			Cohesion:             cluster.Cohesion,
			Coupling:             cluster.Coupling,
			CouplingDependencies: cluster.CouplingDependencies,
			Entities:             entities,
		}
	}
	decomposition.Clusters = clusters
	decomposition.EntityIDToClusterName = entityIDToClusterName

	return &decomposition
}

func newEntity(entity Entity) mono2micro.Entity {
	return mono2micro.Entity{
		Id:   entity.Id,
		Name: entity.Name,
	}
}

func newFunctionality(bucket *gridfs.Bucket, databaseFunctionality Functionality) (*mono2micro.Functionality, error) {
	functionality := mono2micro.Functionality{
		Name:                                    databaseFunctionality.Name,
		Type:                                    databaseFunctionality.Type,
		Complexity:                              databaseFunctionality.Metrics["Complexity"],
		Performance:                             databaseFunctionality.Metrics["Performance"],
		Entities:                                databaseFunctionality.Entities,
		EntitiesPerCluster:                      databaseFunctionality.EntitiesPerCluster,
		FunctionalityRedesignNameUsedForMetrics: databaseFunctionality.FunctionalityRedesignNameUsedForMetrics,
	}

	var functionalityRedesigns []*mono2micro.FunctionalityRedesign
	for _, fileName := range databaseFunctionality.FunctionalityRedesigns {
		var buffer bytes.Buffer
		var databaseFunctionalityRedesign FunctionalityRedesign
		if _, err := bucket.DownloadToStreamByName(fileName, &buffer); err != nil {
			return nil, err
		}
		if err := json.Unmarshal(buffer.Bytes(), &databaseFunctionalityRedesign); err != nil {
			return nil, err
		}

		functionalityRedesign := newFunctionalityRedesign(databaseFunctionalityRedesign)
		functionalityRedesigns = append(functionalityRedesigns, functionalityRedesign)
	}
	functionality.FunctionalityRedesigns = functionalityRedesigns

	return &functionality, nil
}

func newFunctionalityRedesign(databaseFunctionalityRedesign FunctionalityRedesign) *mono2micro.FunctionalityRedesign {
	functionalityRedesign := mono2micro.FunctionalityRedesign{
		Name:                    databaseFunctionalityRedesign.Name,
		SystemComplexity:        databaseFunctionalityRedesign.Metrics["System Complexity"],
		FunctionalityComplexity: databaseFunctionalityRedesign.Metrics["Functionality Complexity"],
		InconsistencyComplexity: databaseFunctionalityRedesign.Metrics["Inconsistency Complexity"],
		PivotTransaction:        databaseFunctionalityRedesign.PivotTransaction,
	}

	var redesign []*mono2micro.Invocation
	for _, databaseInvocation := range databaseFunctionalityRedesign.Redesign {
		redesign = append(redesign, &mono2micro.Invocation{
			Name:              databaseInvocation.Name,
			ID:                databaseInvocation.ID,
			ClusterName:       databaseInvocation.ClusterName,
			ClusterAccesses:   databaseInvocation.ClusterAccesses,
			RemoteInvocations: databaseInvocation.RemoteInvocations,
			Type:              databaseInvocation.Type,
		})
	}
	functionalityRedesign.Redesign = redesign

	return &functionalityRedesign
}
