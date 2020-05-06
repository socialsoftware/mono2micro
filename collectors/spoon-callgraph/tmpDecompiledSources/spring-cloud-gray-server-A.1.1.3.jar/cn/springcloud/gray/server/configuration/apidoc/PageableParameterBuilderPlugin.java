/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Function
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.Lists
 *  org.springframework.data.domain.Pageable
 *  springfox.documentation.builders.OperationBuilder
 *  springfox.documentation.builders.ParameterBuilder
 *  springfox.documentation.schema.ModelReference
 *  springfox.documentation.schema.ResolvedTypes
 *  springfox.documentation.schema.TypeNameExtractor
 *  springfox.documentation.service.Parameter
 *  springfox.documentation.service.ResolvedMethodParameter
 *  springfox.documentation.spi.DocumentationType
 *  springfox.documentation.spi.schema.AlternateTypeProvider
 *  springfox.documentation.spi.schema.GenericTypeNamingStrategy
 *  springfox.documentation.spi.schema.contexts.ModelContext
 *  springfox.documentation.spi.service.OperationBuilderPlugin
 *  springfox.documentation.spi.service.contexts.DocumentationContext
 *  springfox.documentation.spi.service.contexts.OperationContext
 *  springfox.documentation.spi.service.contexts.ParameterContext
 */
package cn.springcloud.gray.server.configuration.apidoc;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Pageable;
import springfox.documentation.builders.OperationBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.schema.ModelReference;
import springfox.documentation.schema.ResolvedTypes;
import springfox.documentation.schema.TypeNameExtractor;
import springfox.documentation.service.Parameter;
import springfox.documentation.service.ResolvedMethodParameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.AlternateTypeProvider;
import springfox.documentation.spi.schema.GenericTypeNamingStrategy;
import springfox.documentation.spi.schema.contexts.ModelContext;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.contexts.DocumentationContext;
import springfox.documentation.spi.service.contexts.OperationContext;
import springfox.documentation.spi.service.contexts.ParameterContext;

public class PageableParameterBuilderPlugin
implements OperationBuilderPlugin {
    private final TypeNameExtractor nameExtractor;
    private final TypeResolver resolver;
    private final ResolvedType pageableType;

    public PageableParameterBuilderPlugin(TypeNameExtractor nameExtractor, TypeResolver resolver) {
        this.nameExtractor = nameExtractor;
        this.resolver = resolver;
        this.pageableType = resolver.resolve((Type)((Object)Pageable.class), new Type[0]);
    }

    public void apply(OperationContext context) {
        List methodParameters = context.getParameters();
        ArrayList parameters = Lists.newArrayList();
        for (ResolvedMethodParameter methodParameter : methodParameters) {
            ResolvedType resolvedType = methodParameter.getParameterType();
            if (!this.pageableType.equals(resolvedType)) continue;
            ParameterContext parameterContext = new ParameterContext(methodParameter, new ParameterBuilder(), context.getDocumentationContext(), context.getGenericsNamingStrategy(), context);
            Function<ResolvedType, ? extends ModelReference> factory = this.createModelRefFactory(parameterContext);
            ModelReference intModel = (ModelReference)factory.apply((Object)this.resolver.resolve(Integer.TYPE, new Type[0]));
            ModelReference stringModel = (ModelReference)factory.apply((Object)this.resolver.resolve((Type)((Object)List.class), new Type[]{String.class}));
            parameters.add(new ParameterBuilder().parameterType("queryRecords").name("page").modelRef(intModel).description("Results page you want to retrieve (0..N)").build());
            parameters.add(new ParameterBuilder().parameterType("queryRecords").name("size").modelRef(intModel).description("Number of records per page").build());
            parameters.add(new ParameterBuilder().parameterType("queryRecords").name("sort").modelRef(stringModel).allowMultiple(true).description("Sorting criteria in the format: property(,asc|desc). Default sort order is ascending. Multiple sort criteria are supported.").build());
            context.operationBuilder().parameters((List)parameters);
        }
    }

    public boolean supports(DocumentationType delimiter) {
        return true;
    }

    private Function<ResolvedType, ? extends ModelReference> createModelRefFactory(ParameterContext context) {
        ModelContext modelContext = ModelContext.inputParam((String)context.getGroupName(), (Type)context.resolvedMethodParameter().getParameterType(), (DocumentationType)context.getDocumentationType(), (AlternateTypeProvider)context.getAlternateTypeProvider(), (GenericTypeNamingStrategy)context.getGenericNamingStrategy(), (ImmutableSet)context.getIgnorableParameterTypes());
        return ResolvedTypes.modelRefFactory((ModelContext)modelContext, (TypeNameExtractor)this.nameExtractor);
    }
}

