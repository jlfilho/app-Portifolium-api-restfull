package edu.uea.acadmanage.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração de cache usando armazenamento em memória simples.
 * Redis foi desativado para evitar dependências externas.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * CacheManager usando armazenamento em memória (ConcurrentHashMap).
     * Substitui o RedisCacheManager para não depender do Redis.
     */
    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        // Definir os nomes dos caches que serão usados
        cacheManager.setCacheNames(java.util.Arrays.asList(
            "usuarios", 
            "cursos", 
            "atividades", 
            "categorias", 
            "pessoas"
        ));
        // Permitir criação dinâmica de caches se necessário
        cacheManager.setAllowNullValues(false);
        return cacheManager;
    }
}


