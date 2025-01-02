package edu.uea.acadmanage.controller;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.uea.acadmanage.DTO.CategoriaDTO;
import edu.uea.acadmanage.DTO.CategoriaResumidaDTO;
import edu.uea.acadmanage.model.Categoria;
import edu.uea.acadmanage.service.CategoriaService;
import edu.uea.acadmanage.service.CustomUserDetailsService;
import edu.uea.acadmanage.service.exception.GlobalExceptionHandler;
import edu.uea.acadmanage.service.exception.RecursoNaoEncontradoException;

@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoriaControllerTest {

        @InjectMocks
        private CategoriaController categoriaController;
        @Mock
        private CategoriaService categoriaService;
        @MockBean
        private CustomUserDetailsService userDetailsService; // Mock do serviço de autenticação

        private MockMvc mockMvc;

        @BeforeEach
        void setUp() {
                mockMvc = MockMvcBuilders.standaloneSetup(categoriaController)
                                .setControllerAdvice(new GlobalExceptionHandler()) // Adicione sua classe de tratamento
                                                                                   // global de
                                                                                   // exceções, se houver
                                .build();

                UserDetails mockUser = User.withUsername("admin@uea.edu.br")
                                .password("admin123")
                                .roles("ADMINISTRADOR")
                                .build();
                when(userDetailsService.loadUserByUsername("admin@uea.edu.br")).thenReturn(mockUser);
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
                ResponseEntity<CategoriaResumidaDTO> response = categoriaController
                                .recuperarCategoriaPorId(categoriaId);

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
                                .andExpect(jsonPath("$.nome").value("O nome da categoria é obrigatório")); // Ajuste
                                                                                                           // conforme o
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
                doThrow(new RecursoNaoEncontradoException("Categoria não encontrada")).when(categoriaService)
                                .deletar(categoriaId);

                mockMvc.perform(delete("/categorias/{categoriaId}", categoriaId))
                                .andExpect(status().isNotFound()) // Verifica se retorna 404 Not Found
                                .andExpect(jsonPath("$.error").value("Categoria não encontrada")); // Verifica se a
                                                                                                   // mensagem está
                                                                                                   // presente

                verify(categoriaService, times(1)).deletar(categoriaId);
        }

        /*
         * GET /categorias
         * 
         * Cenários:
         * Retornar todas as categorias com sucesso.
         * Nenhuma categoria encontrada (204 No Content).
         */

        @Test
        void listarCategorias_ComSucesso() throws Exception {
                // Simulação de categorias
                List<CategoriaResumidaDTO> categorias = List.of(
                                new CategoriaResumidaDTO(1L, "Categoria 1"),
                                new CategoriaResumidaDTO(2L, "Categoria 2"));

                when(categoriaService.listarTodasCategorias()).thenReturn(categorias);

                mockMvc.perform(get("/categorias"))
                                .andExpect(status().isOk()) // Verifica se retorna 200 OK
                                .andExpect(jsonPath("$.length()").value(categorias.size())) // Verifica o tamanho da
                                                                                            // lista
                                .andExpect(jsonPath("$[0].id").value(1L)) // Verifica o ID da primeira categoria
                                .andExpect(jsonPath("$[0].nome").value("Categoria 1")) // Verifica o nome da primeira
                                                                                       // categoria
                                .andExpect(jsonPath("$[1].id").value(2L)) // Verifica o ID da segunda categoria
                                .andExpect(jsonPath("$[1].nome").value("Categoria 2")); // Verifica o nome da segunda
                                                                                        // categoria

                verify(categoriaService, times(1)).listarTodasCategorias();
        }

        @Test
        void listarCategorias_SemCategorias() throws Exception {
                // Simulação: Nenhuma categoria encontrada
                when(categoriaService.listarTodasCategorias()).thenReturn(Collections.emptyList());

                mockMvc.perform(get("/categorias"))
                                .andExpect(status().isNoContent()); // Verifica se retorna 204 No Content

                verify(categoriaService, times(1)).listarTodasCategorias();
        }

        /*
         * POST /categorias
         * 
         * Cenários:
         * Criar uma nova categoria com sucesso.
         * Dados inválidos para criação (400 Bad Request).
         */
        @Test
        void criarCategoria_ComSucesso() throws Exception {
                // Simulação de entrada e saída
                Categoria novaCategoria = new Categoria(null, "Nova Categoria", null);
                Categoria categoriaCriada = new Categoria(1L, "Nova Categoria", null);

                when(categoriaService.salvar(any(Categoria.class))).thenReturn(categoriaCriada);

                mockMvc.perform(post("/categorias")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(new ObjectMapper().writeValueAsString(novaCategoria))) // Envia o payload em
                                                                                                // JSON
                                .andExpect(status().isCreated()) // Verifica se retorna 201 Created
                                .andExpect(jsonPath("$.id").value(1L)) // Verifica o ID da categoria criada
                                .andExpect(jsonPath("$.nome").value("Nova Categoria")); // Verifica o nome da categoria
                                                                                        // criada

                verify(categoriaService, times(1)).salvar(any(Categoria.class));
        }

        @Test
        void criarCategoria_DadosInvalidos() throws Exception {
                // Categoria com nome inválido
                Categoria categoriaInvalida = new Categoria(null, "", null);

                mockMvc.perform(post("/categorias")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(new ObjectMapper().writeValueAsString(categoriaInvalida))) // Envia o payload
                                                                                                    // em JSON
                                .andExpect(status().isBadRequest()) // Verifica se retorna 400 Bad Request
                                .andExpect(jsonPath("$.nome").value("O nome da categoria é obrigatório")); // Verifica
                                                                                                           // se há uma
                                                                                                           // mensagem
                                                                                                           // de erro

                verify(categoriaService, never()).salvar(any(Categoria.class)); // Garante que o serviço não foi chamado
        }

}
