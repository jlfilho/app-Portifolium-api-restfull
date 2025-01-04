package edu.uea.acadmanage.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.uea.acadmanage.DTO.CategoriaDTO;
import edu.uea.acadmanage.DTO.CategoriaResumidaDTO;
import edu.uea.acadmanage.model.Categoria;
import edu.uea.acadmanage.service.CategoriaService;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private CategoriaService categoriaService;


    public CategoriaController(
            CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public ResponseEntity<List<CategoriaResumidaDTO>> listarTodasCategorias() {
        List<CategoriaResumidaDTO> categorias = categoriaService.listarTodasCategorias();

        if (categorias.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204 No Content
        }
        return ResponseEntity.ok(categorias); // 200 OK com a lista de categorias
    }

    @GetMapping("/{categoriaId}")
    public ResponseEntity<CategoriaResumidaDTO> recuperarCategoriaPorId(@PathVariable Long categoriaId) {
        try {
            CategoriaResumidaDTO categoria = categoriaService.recuperarCategoriaPorId(categoriaId);
            return ResponseEntity.ok(categoria); // 200 OK com a categoria encontrada
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build(); // 404 Not Found
        }
    }

    @GetMapping("/usuario")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('GERENTE') or hasRole('SECRETARIO')")
    public ResponseEntity<List<CategoriaDTO>> getCategoriasComAtividades(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<CategoriaDTO> categorias = categoriaService.getCategoriasComAtividadesByUsuario(userDetails.getUsername());

        if (categorias.isEmpty()) {
            return ResponseEntity.noContent().build(); // Retorna 204 No Content se não houver categorias
        }

        return ResponseEntity.ok(categorias); // Retorna 200 OK com a lista de categorias
    }

    @PutMapping("/{categoriaId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<CategoriaResumidaDTO> atualizarCategoria(
            @PathVariable Long categoriaId,
            @Validated @RequestBody CategoriaResumidaDTO categoria) {
        Categoria novaCategoria = new Categoria(categoriaId, categoria.nome(), null);
        Categoria categoriaAtualizada = categoriaService.atualizar(categoriaId, novaCategoria);
        return ResponseEntity.ok(new CategoriaResumidaDTO(categoriaAtualizada.getId(), categoriaAtualizada.getNome()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<CategoriaResumidaDTO> salvarCategoria(@Validated @RequestBody CategoriaResumidaDTO categoriaDTO) {
        System.out.println("Salvando categoria: " + categoriaDTO);
        Categoria categoria = new Categoria(categoriaDTO.id(), categoriaDTO.nome(), null);
        Categoria novaCategoria = categoriaService.salvar(categoria);
        return ResponseEntity.status(HttpStatus.CREATED).body(new CategoriaResumidaDTO(novaCategoria.getId(), novaCategoria.getNome()));
    }

    // Endpoint para deletar uma categoria
    @DeleteMapping("/{categoriaId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> deletarCategoria(@PathVariable Long categoriaId) {
        categoriaService.deletar(categoriaId);
        return ResponseEntity.noContent().build();
    }

}
