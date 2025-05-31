package ventas.inventario.controller;

import ventas.inventario.model.Producto; 
import ventas.inventario.service.ProductoService; 
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

import java.util.List;

@RestController
@RequestMapping("/api/inventario")
public class ProductoController {

    private final ProductoService productoService;

    @Autowired
    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public ResponseEntity<List<Producto>> getAllProductos(
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<Producto> productoPage = productoService.getAllProductos(pageable);
        return ResponseEntity.ok(productoPage.getContent());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> getProductoById(@PathVariable("id") Long id) {
        Producto producto = productoService.getProductoById(id);
        return ResponseEntity.ok(producto);
    }

    @PostMapping
    public ResponseEntity<Producto> createProducto(@Valid @RequestBody Producto producto) {
        Producto nuevoProducto = productoService.createProducto(producto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoProducto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Producto> updateProducto(@PathVariable("id") Long id, @Valid @RequestBody Producto productoActualizado) {
        Producto productoActualizadoDB = productoService.updateProducto(id, productoActualizado);
        return ResponseEntity.ok(productoActualizadoDB);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProducto(@PathVariable("id") Long id) {
        productoService.deleteProducto(id);
        return ResponseEntity.noContent().build(); 
    }

    @PutMapping("/{id}/stock")
    public ResponseEntity<Producto> updateStock(@PathVariable("id") Long id, @RequestParam Integer cantidad) {
        Producto productoActualizado = productoService.updateStock(id, cantidad);
        return ResponseEntity.ok(productoActualizado);
    }

    @DeleteMapping("/{id}/stock")
    public ResponseEntity<Void> deleteStock(@PathVariable("id") Long id) {
        productoService.deleteProducto(id);
        return ResponseEntity.noContent().build(); 
    }
}
