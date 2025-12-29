package olegood.fenc.crypto.kek;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "crypto.alias")
public record Alias(String kek, String cek) {}
