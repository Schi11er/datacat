package de.bentrm.datacat.graphql.resolver;

import graphql.TypeResolutionEnvironment;
import graphql.schema.GraphQLObjectType;
import org.springframework.stereotype.Component;

@Component
public class SimpleRecordResolver extends XtdRootResolver {

    public SimpleRecordResolver(XtdObjectResolver objectTypeResolver) {
        super(objectTypeResolver);
        //TODO Auto-generated constructor stub
    }

    @Override
    public String getTypeName() {
        return "SimpleRecord";
    }

    @Override
    public GraphQLObjectType getType(TypeResolutionEnvironment env) {
        return super.getType(env);
    }
}
