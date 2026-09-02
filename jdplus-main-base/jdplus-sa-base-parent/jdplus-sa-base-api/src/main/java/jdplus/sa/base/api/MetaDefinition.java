package jdplus.sa.base.api;

import java.util.List;
import java.util.Set;
import nbbrd.service.ServiceDefinition;

@ServiceDefinition
public interface MetaDefinition {

    @lombok.NonNull
    List<Class> getSupportedClasses();

    @lombok.NonNull
    Set<String> getAllMetaKeys();
}
