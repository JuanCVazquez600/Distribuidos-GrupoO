package Distribuidos_GrupoO.ServidorGRPC.controller;

import Distribuidos_GrupoO.ServidorGRPC.service.IInventarioDeDonacionesService;
import Distribuidos_GrupoO.ServidorGRPC.model.InventarioDeDonaciones;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventario")
public class InventarioController {

    @Autowired
    private IInventarioDeDonacionesService inventarioService;

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
