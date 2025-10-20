package Distribuidos_GrupoO.ServidorGRPC.controller;

import Distribuidos_GrupoO.ServidorGRPC.service.IInventarioDeDonacionesService;
import Distribuidos_GrupoO.ServidorGRPC.model.InventarioDeDonaciones;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para gestión del inventario de donaciones
 */
@RestController
@RequestMapping("/inventario")
public class InventarioController {

    @Autowired
    private IInventarioDeDonacionesService inventarioService;

    /**
     * Listar todo el inventario de donaciones
     */
    @GetMapping("/list")
    public ResponseEntity<List<InventarioDeDonaciones>> listarInventarios() {
        try {
            List<InventarioDeDonaciones> inventarios = inventarioService.listarInventarios();
            return ResponseEntity.ok(inventarios);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
