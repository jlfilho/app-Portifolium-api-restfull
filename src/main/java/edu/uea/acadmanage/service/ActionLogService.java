package edu.uea.acadmanage.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uea.acadmanage.model.ActionLog;
import edu.uea.acadmanage.repository.ActionLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class ActionLogService {
    
    private static final Logger logger = LoggerFactory.getLogger(ActionLogService.class);
    private final ActionLogRepository actionLogRepository;
    private final ObjectMapper objectMapper;

    public ActionLogService(ActionLogRepository actionLogRepository, ObjectMapper objectMapper) {
        this.actionLogRepository = actionLogRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Método público para capturar o contexto e chamar o método assíncrono
     */
    public void log(ActionLog.ActionType actionType, Boolean success, String description, 
                    String errorMessage, Map<String, Object> metadata) {
        // Capturar SecurityContext e RequestAttributes ANTES do método assíncrono
        SecurityContext securityContext = SecurityContextHolder.getContext();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        
        // Extrair informações do contexto antes de passar para thread assíncrona
        String userEmail = getCurrentUserEmail(securityContext);
        HttpServletRequest request = getCurrentRequest(requestAttributes);
        
        // Chamar método assíncrono com os valores já capturados
        logAsync(actionType, success, description, errorMessage, metadata, userEmail, request);
    }

    @Async
    private void logAsync(ActionLog.ActionType actionType, Boolean success, String description, 
                         String errorMessage, Map<String, Object> metadata,
                         String userEmail, HttpServletRequest request) {
        try {
            ActionLog actionLog = new ActionLog();
            actionLog.setActionType(actionType);
            actionLog.setUserEmail(userEmail != null && !userEmail.isEmpty() ? userEmail : "system");
            actionLog.setTimestamp(LocalDateTime.now());
            actionLog.setDescription(description);
            actionLog.setSuccess(success);
            actionLog.setErrorMessage(errorMessage);
            
            if (request != null) {
                actionLog.setIpAddress(getClientIpAddress(request));
                actionLog.setUserAgent(request.getHeader("User-Agent"));
                actionLog.setEndpoint(request.getRequestURI());
            }

            if (metadata != null && !metadata.isEmpty()) {
                actionLog.setMetadata(convertToJson(metadata));
            }

            actionLogRepository.save(actionLog);
        } catch (Exception e) {
            logger.error("Erro ao salvar log de ação", e);
        }
    }

    private String getCurrentUserEmail(SecurityContext securityContext) {
        try {
            if (securityContext == null) {
                return "system";
            }
            Authentication authentication = securityContext.getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() 
                || "anonymousUser".equals(authentication.getPrincipal())) {
                return "system";
            }
            String email = authentication.getName();
            return email != null && !email.isEmpty() ? email : "system";
        } catch (Exception e) {
            logger.warn("Erro ao obter email do usuário autenticado", e);
            return "system";
        }
    }

    private String convertToJson(Object obj) {
        try {
            if (obj == null) return null;
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            logger.warn("Erro ao converter objeto para JSON", e);
            return obj != null ? obj.toString() : null;
        }
    }

    private HttpServletRequest getCurrentRequest(RequestAttributes requestAttributes) {
        try {
            if (requestAttributes instanceof ServletRequestAttributes) {
                return ((ServletRequestAttributes) requestAttributes).getRequest();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        // Lista de headers a verificar (ordem de prioridade)
        String[] headers = {
            "X-Forwarded-For",
            "X-Real-IP",
            "CF-Connecting-IP",        // Cloudflare
            "True-Client-IP",          // Cloudflare Enterprise
            "X-Client-IP",
            "X-Forwarded",
            "Forwarded-For",
            "Forwarded"
        };
        
        // Verificar headers em ordem de prioridade
        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !ip.equalsIgnoreCase("unknown")) {
                // X-Forwarded-For pode conter múltiplos IPs separados por vírgula
                if (header.equals("X-Forwarded-For")) {
                    String[] ips = ip.split(",");
                    if (ips.length > 0) {
                        ip = ips[0].trim();
                    }
                }
                // Forwarded header tem formato diferente: for=192.0.2.60;proto=http;by=203.0.113.43
                if (header.equals("Forwarded")) {
                    String[] parts = ip.split(";");
                    for (String part : parts) {
                        if (part.trim().toLowerCase().startsWith("for=")) {
                            ip = part.substring(4).trim();
                            // Remover aspas se existirem
                            ip = ip.replace("\"", "").trim();
                            break;
                        }
                    }
                }
                
                ip = normalizeIp(ip);
                if (ip != null && !isLocalhost(ip)) {
                    return ip;
                }
            }
        }
        
        // Se não encontrou em headers, usar RemoteAddr
        String remoteAddr = request.getRemoteAddr();
        remoteAddr = normalizeIp(remoteAddr);
        return remoteAddr != null ? remoteAddr : "unknown";
    }
    
    /**
     * Normaliza o IP, convertendo IPv6 localhost para IPv4 quando apropriado
     */
    private String normalizeIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return null;
        }
        
        ip = ip.trim();
        
        // Converter IPv6 localhost para IPv4
        if (ip.equals("0:0:0:0:0:0:0:1") || ip.equals("::1")) {
            return "127.0.0.1";
        }
        
        // Remover colchetes de IPv6 se existirem
        if (ip.startsWith("[") && ip.endsWith("]")) {
            ip = ip.substring(1, ip.length() - 1);
        }
        
        return ip;
    }
    
    /**
     * Verifica se o IP é localhost
     */
    private boolean isLocalhost(String ip) {
        if (ip == null || ip.isEmpty()) {
            return true;
        }
        
        ip = ip.trim();
        
        // IPv4 localhost
        if (ip.equals("127.0.0.1") || ip.equals("localhost")) {
            return true;
        }
        
        // IPv6 localhost
        if (ip.equals("0:0:0:0:0:0:0:1") || ip.equals("::1")) {
            return true;
        }
        
        return false;
    }
}
