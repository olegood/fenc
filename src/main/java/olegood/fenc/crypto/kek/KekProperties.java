package olegood.fenc.crypto.kek;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

@ConfigurationProperties(prefix = "crypto.kek")
public record KekProperties(
    String keyStoreType, Resource location, String password, String alias) {}
