package com.skyeai.jarvis.grpc.resolver;

import io.grpc.NameResolver;
import io.grpc.NameResolverProvider;

import java.net.URI;

public class NacosNameResolverProvider extends NameResolverProvider {
    
    @Override
    protected boolean isAvailable() {
        return true;
    }
    
    @Override
    protected int priority() {
        return 5;
    }
    
    @Override
    public NameResolver newNameResolver(URI targetUri, NameResolver.Args args) {
        if ("nacos".equals(targetUri.getScheme())) {
            return new NacosNameResolver(targetUri);
        }
        return null;
    }
    
    @Override
    public String getDefaultScheme() {
        return "nacos";
    }
}
