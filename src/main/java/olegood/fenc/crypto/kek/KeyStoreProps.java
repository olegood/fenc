package olegood.fenc.crypto.kek;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

@ConfigurationProperties(prefix = "crypto.keystore")
public record KeyStoreProps(String type, Resource location, String password) {}
