package kr.mclub.apiserver.shared.config;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * OAuth2 설정 Properties
 * OAuth2 configuration properties for social login providers
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "oauth2")
public class OAuth2Properties {

    /**
     * Provider별 OAuth2 설정
     * OAuth2 settings per provider (google, naver, apple)
     */
    private Map<String, ProviderConfig> provider;

    @Data
    public static class ProviderConfig {
        /**
         * OAuth2 Client ID
         */
        private String clientId;

        /**
         * OAuth2 Client Secret
         */
        private String clientSecret;

        /**
         * Redirect URI after OAuth authentication
         */
        private String redirectUri;

        /**
         * Authorization URL
         */
        private String authorizationUri;

        /**
         * Token exchange URL
         */
        private String tokenUri;

        /**
         * User info URL
         */
        private String userInfoUri;

        /**
         * OAuth2 scope
         */
        private String scope;
    }
}
