package ventas.carrito.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ventas.carrito.Model.Carrito;
import ventas.carrito.Model.ItemCarrito;
import ventas.carrito.Repository.CarritoRepository;
import ventas.carrito.Repository.ItemCarritoRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CarritoService {

    private final CarritoRepository carritoRepository;
    private final ItemCarritoRepository itemCarritoRepository; 

    @Autowired
    public CarritoService(CarritoRepository carritoRepository, ItemCarritoRepository itemCarritoRepository) {
        this.carritoRepository = carritoRepository;
        this.itemCarritoRepository = itemCarritoRepository;
    }

    @Transactional(readOnly = true)
    public List<Carrito> getAllCarritos() {
        return carritoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Carrito getCarritoById(Long id) {
        return carritoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Carrito no encontrado con ID: " + id));
    }

    @Transactional
    public Carrito createCarrito(Long idUsuario) {
        Carrito carrito = new Carrito();
        carrito.setIdUsuario(idUsuario);
        carrito.setFechaCreacion(LocalDateTime.now());
        carrito.setFechaActualizacion(LocalDateTime.now());
        return carritoRepository.save(carrito);
    }

    @Transactional
    public void deleteCarrito(Long id) {
        if (!carritoRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Carrito no encontrado con ID: " + id);
        }
        carritoRepository.deleteById(id);
    }

    @Transactional
    public Carrito addItemToCarrito(Long carritoId, Long productoId, Integer cantidad, BigDecimal precioUnitario) {
        if (cantidad <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cantidad debe ser mayor a 0.");
        }

        Carrito carrito = getCarritoById(carritoId); 

        Optional<ItemCarrito> existingItem = carrito.getItems().stream()
                .filter(item -> item.getProductoId().equals(productoId))
                .findFirst();

        if (existingItem.isPresent()) {
            ItemCarrito item = existingItem.get();
            item.setCantidad(item.getCantidad() + cantidad);
            item.setPrecioUnitarioAlMomento(precioUnitario); 
        } else {
            ItemCarrito newItem = new ItemCarrito();
            newItem.setProductoId(productoId);
            newItem.setCantidad(cantidad);
            newItem.setPrecioUnitarioAlMomento(precioUnitario);
            carrito.addItem(newItem);
        }
        carrito.setFechaActualizacion(LocalDateTime.now());
        return carritoRepository.save(carrito);
    }

    @Transactional
    public Carrito updateItemQuantity(Long carritoId, Long itemId, Integer newCantidad) {
        if (newCantidad <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cantidad debe ser mayor a 0.");
        }

        Carrito carrito = getCarritoById(carritoId);

        ItemCarrito itemToUpdate = carrito.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ítem de carrito no encontrado con ID: " + itemId + " en el carrito " + carritoId));

        itemToUpdate.setCantidad(newCantidad);
        carrito.setFechaActualizacion(LocalDateTime.now());
        return carritoRepository.save(carrito);
    }

    @Transactional
    public Carrito removeItemFromCarrito(Long carritoId, Long itemId) {
        Carrito carrito = getCarritoById(carritoId);

        ItemCarrito itemToRemove = carrito.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ítem de carrito no encontrado con ID: " + itemId + " en el carrito " + carritoId));

        carrito.removeItem(itemToRemove);
        carrito.setFechaActualizacion(LocalDateTime.now());
        return carritoRepository.save(carrito); 
    }

    public static class AddItemRequest {
        public Long productoId;
        public Integer cantidad;
        public BigDecimal precioUnitarioAlMomento; 
    }
}