package ventas.carrito.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ventas.carrito.Model.Carrito;
import ventas.carrito.Service.CarritoService;
import ventas.carrito.Service.CarritoService.AddItemRequest;

import java.util.List;

@RestController
@RequestMapping("/api/carritos") 
public class CarritoController {

    private final CarritoService carritoService;

    @Autowired
    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    @GetMapping
    public ResponseEntity<List<Carrito>> getAllCarritos() {
        List<Carrito> carritos = carritoService.getAllCarritos();
        return ResponseEntity.ok(carritos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Carrito> getCarritoById(@PathVariable("id") Long id) {
        Carrito carrito = carritoService.getCarritoById(id);
        return ResponseEntity.ok(carrito);
    }

    @PostMapping // http://localhost:8081/api/carritos?idUsuario=x
    public ResponseEntity<Carrito> createCarrito(@RequestParam("idUsuario") Long idUsuario) {
        Carrito nuevoCarrito = carritoService.createCarrito(idUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoCarrito);
    }

    @PostMapping("/{carritoId}/items")
    public ResponseEntity<Carrito> addItemToCarrito(@PathVariable("carritoId") Long carritoId,
                                                    @RequestBody AddItemRequest itemRequest) {
        Carrito updatedCarrito = carritoService.addItemToCarrito(
            carritoId, itemRequest.productoId, itemRequest.cantidad, 
            itemRequest.precioUnitarioAlMomento);
        return ResponseEntity.ok(updatedCarrito);
    }

    @PutMapping("/{carritoId}/items/{itemId}") // http://localhost:8081/api/carritos/{carritoId}/items/{itemId}?cantidad=x
    public ResponseEntity<Carrito> updateItemQuantity(@PathVariable("carritoId") Long carritoId,
                                                      @PathVariable("itemId") Long itemId,
                                                      @RequestParam Integer cantidad) {
        Carrito updatedCarrito = carritoService.updateItemQuantity(carritoId, itemId, cantidad);
        return ResponseEntity.ok(updatedCarrito);
    }

    @DeleteMapping("/{carritoId}/items/{itemId}")
    public ResponseEntity<Carrito> removeItemFromCarrito(@PathVariable("carritoId") Long carritoId,
                                                          @PathVariable("itemId") Long itemId) {
        Carrito updatedCarrito = carritoService.removeItemFromCarrito(carritoId, itemId);
        return ResponseEntity.ok(updatedCarrito);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCarrito(@PathVariable("id") Long id) {
        carritoService.deleteCarrito(id);
        return ResponseEntity.noContent().build(); 
    }
}