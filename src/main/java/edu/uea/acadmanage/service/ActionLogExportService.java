package edu.uea.acadmanage.service;

import edu.uea.acadmanage.DTO.ActionLogFilterDTO;
import edu.uea.acadmanage.model.ActionLog;
import edu.uea.acadmanage.repository.ActionLogRepository;
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
public class ActionLogExportService {
    
    private static final Logger logger = LoggerFactory.getLogger(ActionLogExportService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final ActionLogRepository actionLogRepository;
    
    public ActionLogExportService(ActionLogRepository actionLogRepository) {
        this.actionLogRepository = actionLogRepository;
    }
    
    public void exportToCsv(ActionLogFilterDTO filter, OutputStream outputStream) throws IOException {
        try (Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
            // Escrever BOM UTF-8 para melhor compatibilidade com Excel
            outputStream.write(0xEF);
            outputStream.write(0xBB);
            outputStream.write(0xBF);
            
            // Escrever cabeçalho CSV
            writer.write("ID,Data/Hora,Usuário,Tipo de Ação,Status,Descrição,Endpoint,IP,User Agent,Mensagem de Erro,Metadados\n");
            writer.flush();
            
            // Buscar logs com filtros e escrever em streaming
            List<ActionLog> logs = actionLogRepository.findWithFilters(
                filter.userEmail(),
                filter.actionType(),
                filter.success(),
                filter.startDate(),
                filter.endDate()
            );
            
            // Escrever dados em streaming (processar em lotes se necessário)
            int batchSize = 1000;
            int total = logs.size();
            
            for (int i = 0; i < total; i++) {
                ActionLog log = logs.get(i);
                writer.write(formatActionLogRow(log));
                
                // Flush a cada batch para manter streaming
                if ((i + 1) % batchSize == 0 || i == total - 1) {
                    writer.flush();
                }
            }
            
            writer.flush();
        } catch (Exception e) {
            logger.error("Erro ao exportar logs de ação para CSV", e);
            throw new IOException("Erro ao exportar logs de ação", e);
        }
    }
    
    private String formatActionLogRow(ActionLog log) {
        StringBuilder row = new StringBuilder();
        
        appendCsvField(row, log.getId() != null ? log.getId().toString() : "");
        appendCsvField(row, log.getTimestamp() != null ? log.getTimestamp().format(DATE_FORMATTER) : "");
        appendCsvField(row, log.getUserEmail() != null ? log.getUserEmail() : "");
        appendCsvField(row, log.getActionType() != null ? log.getActionType().name() : "");
        appendCsvField(row, log.getSuccess() != null ? (log.getSuccess() ? "Sucesso" : "Falha") : "");
        appendCsvField(row, log.getDescription() != null ? log.getDescription() : "");
        appendCsvField(row, log.getEndpoint() != null ? log.getEndpoint() : "");
        appendCsvField(row, log.getIpAddress() != null ? log.getIpAddress() : "");
        appendCsvField(row, log.getUserAgent() != null ? log.getUserAgent() : "");
        appendCsvField(row, log.getErrorMessage() != null ? log.getErrorMessage() : "");
        appendCsvField(row, log.getMetadata() != null ? log.getMetadata() : "");
        
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

