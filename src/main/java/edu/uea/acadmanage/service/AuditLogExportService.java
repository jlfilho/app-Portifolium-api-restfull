package edu.uea.acadmanage.service;

import edu.uea.acadmanage.DTO.AuditLogFilterDTO;
import edu.uea.acadmanage.model.AuditLog;
import edu.uea.acadmanage.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AuditLogExportService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuditLogExportService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final AuditLogRepository auditLogRepository;
    
    public AuditLogExportService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }
    
    public void exportToCsv(AuditLogFilterDTO filter, OutputStream outputStream) throws IOException {
        try (Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
            // Escrever BOM UTF-8 para melhor compatibilidade com Excel
            outputStream.write(0xEF);
            outputStream.write(0xBB);
            outputStream.write(0xBF);
            
            // Escrever cabeçalho CSV
            writer.write("ID,Data/Hora,Usuário,Ação,Entidade,ID Entidade,Descrição,Endpoint,Método HTTP,IP,User Agent,Valores Antigos,Valores Novos\n");
            writer.flush();
            
            // Buscar logs com filtros e escrever em streaming
            List<AuditLog> logs = auditLogRepository.findWithFilters(
                filter.userEmail(),
                filter.action(),
                filter.entityName(),
                filter.entityId(),
                filter.startDate(),
                filter.endDate()
            );
            
            // Escrever dados em streaming (processar em lotes se necessário)
            int batchSize = 1000;
            int total = logs.size();
            
            for (int i = 0; i < total; i++) {
                AuditLog log = logs.get(i);
                writer.write(formatAuditLogRow(log));
                
                // Flush a cada batch para manter streaming
                if ((i + 1) % batchSize == 0 || i == total - 1) {
                    writer.flush();
                }
            }
            
            writer.flush();
        } catch (Exception e) {
            logger.error("Erro ao exportar logs de auditoria para CSV", e);
            throw new IOException("Erro ao exportar logs de auditoria", e);
        }
    }
    
    private String formatAuditLogRow(AuditLog log) {
        StringBuilder row = new StringBuilder();
        
        appendCsvField(row, log.getId() != null ? log.getId().toString() : "");
        appendCsvField(row, log.getTimestamp() != null ? log.getTimestamp().format(DATE_FORMATTER) : "");
        appendCsvField(row, log.getUserEmail() != null ? log.getUserEmail() : "");
        appendCsvField(row, log.getAction() != null ? log.getAction().name() : "");
        appendCsvField(row, log.getEntityName() != null ? log.getEntityName() : "");
        appendCsvField(row, log.getEntityId() != null ? log.getEntityId().toString() : "");
        appendCsvField(row, log.getDescription() != null ? log.getDescription() : "");
        appendCsvField(row, log.getEndpoint() != null ? log.getEndpoint() : "");
        appendCsvField(row, log.getHttpMethod() != null ? log.getHttpMethod() : "");
        appendCsvField(row, log.getIpAddress() != null ? log.getIpAddress() : "");
        appendCsvField(row, log.getUserAgent() != null ? log.getUserAgent() : "");
        appendCsvField(row, log.getOldValues() != null ? log.getOldValues() : "");
        appendCsvField(row, log.getNewValues() != null ? log.getNewValues() : "");
        
        row.append("\n");
        return row.toString();
    }
    
    private void appendCsvField(StringBuilder row, String value) {
        // Adicionar vírgula antes de cada campo, exceto o primeiro
        if (row.length() > 0) {
            row.append(",");
        }
        
        // Se o valor for null ou vazio, adicionar string vazia
        if (value == null || value.isEmpty()) {
            return;
        }
        
        // Escapar aspas e quebras de linha
        String escaped = value.replace("\"", "\"\"")
                              .replace("\n", " ")
                              .replace("\r", " ");
        
        // Adicionar aspas se contiver vírgula, aspas ou espaços no início/fim
        if (escaped.contains(",") || escaped.contains("\"") || 
            escaped.trim().length() != escaped.length()) {
            row.append("\"").append(escaped).append("\"");
        } else {
            row.append(escaped);
        }
    }
}

