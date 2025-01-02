package edu.uea.acadmanage.controller;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.uea.acadmanage.DTO.CategoriaResumidaDTO;
import edu.uea.acadmanage.model.Categoria;
import edu.uea.acadmanage.service.CategoriaService;
import edu.uea.acadmanage.service.exception.GlobalExceptionHandler;
import edu.uea.acadmanage.service.exception.RecursoNaoEncontradoException;

@ExtendWith(MockitoExtension.class)
class CategoriaControllerTest {

    @InjectMocks
    private CategoriaController categoriaController;
    @Mock
    private CategoriaService categoriaService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(categoriaController)
                .setControllerAdvice(new GlobalExceptionHandler()) // Adicione sua classe de tratamento global de
                                                                   // exceções, se houver
                .build();
    }

    /*
     * GET /categorias/{categoriaId}
     * 
     * Cenários:
     * Recuperar uma categoria existente com sucesso.
     * Categoria não encontrada (404 Not Found).
     */
    @Test
    void deveRetornarCategoriaExistente() {
        // Arrange
        Long categoriaId = 1L;
        CategoriaResumidaDTO categoriaMock = new CategoriaResumidaDTO(categoriaId, "Ensino");
        when(categoriaService.recuperarCategoriaPorId(categoriaId)).thenReturn(categoriaMock);

        // Act
        ResponseEntity<CategoriaResumidaDTO> response = categoriaController.recuperarCategoriaPorId(categoriaId);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals(categoriaMock, response.getBody());
    }

    @Test
    void deveRetornarNotFoundParaCategoriaNaoExistente() {
        // Arrange
        Long categoriaId = 99L;
        when(categoriaService.recuperarCategoriaPorId(categoriaId))
                .thenThrow(new RecursoNaoEncontradoException("Categoria não encontrada com o ID: 99"));

        // Act & Assert
        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> categoriaController.recuperarCategoriaPorId(categoriaId));

        assertEquals("Categoria não encontrada com o ID: 99", exception.getMessage());
    }

    /*
     * PUT /categorias/{categoriaId}
     * 
     * Cenários:
     * Atualizar uma categoria existente com sucesso.
     * Categoria não encontrada (404 Not Found).
     * Dados inválidos para atualização (400 Bad Request).
     */

    @Test
    void atualizarCategoria_ComSucesso() throws Exception {
        // Configuração do mock para uma categoria existente
        Categoria categoriaAtualizada = new Categoria(1L, "Categoria Atualizada", null);
        Mockito.when(categoriaService.atualizar(eq(1L), any(Categoria.class)))
                .thenReturn(categoriaAtualizada);

        // Execução e verificação
        mockMvc.perform(put("/categorias/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\": \"Categoria Atualizada\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nome", is("Categoria Atualizada")));
    }

    @Test
    void atualizarCategoria_CategoriaNaoEncontrada() throws Exception {
        // Configuração do mock para categoria inexistente
        Mockito.when(categoriaService.atualizar(eq(99L), any(Categoria.class)))
                .thenThrow(new RecursoNaoEncontradoException("Categoria não encontrada"));

        // Execução e verificação
        mockMvc.perform(put("/categorias/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\": \"Categoria Atualizada\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Categoria não encontrada")));
    }

    @Test
    void atualizarCategoria_DadosInvalidos() throws Exception {
        Long categoriaId = 1L;
        CategoriaResumidaDTO categoriaDTO = new CategoriaResumidaDTO(null, ""); // Dados inválidos

        mockMvc.perform(put("/categorias/{categoriaId}", categoriaId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(categoriaDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.nome").value("O nome da categoria é obrigatório")); // Ajuste conforme o
                                                                                           // manipulador
    }

    /*
     * DELETE /categorias/{categoriaId}
     * 
     * Cenários:
     * Deletar uma categoria existente com sucesso.
     * Categoria não encontrada (404 Not Found).
     */

     @Test
    void deletarCategoria_ComSucesso() throws Exception {
        Long categoriaId = 1L;

        // Simulação: O método não lança exceção, indicando que a categoria foi deletada
        doNothing().when(categoriaService).deletar(categoriaId);

        mockMvc.perform(delete("/categorias/{categoriaId}", categoriaId))
            .andExpect(status().isNoContent()); // Verifica se retorna 204 No Content

        verify(categoriaService, times(1)).deletar(categoriaId);
    }

    @Test
    void deletarCategoria_CategoriaNaoEncontrada() throws Exception {
        Long categoriaId = 999L;

        // Simulação: Lança exceção para indicar que a categoria não foi encontrada
        doThrow(new RecursoNaoEncontradoException("Categoria não encontrada")).when(categoriaService).deletar(categoriaId);

        mockMvc.perform(delete("/categorias/{categoriaId}", categoriaId))
            .andExpect(status().isNotFound()) // Verifica se retorna 404 Not Found
            .andExpect(jsonPath("$.error").value("Categoria não encontrada")); // Verifica se a mensagem está presente

        verify(categoriaService, times(1)).deletar(categoriaId);
    }

}
