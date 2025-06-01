package ventas.carrito.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ventas.carrito.Model.Carrito;

@Repository
public interface CarritoRepository extends JpaRepository<Carrito, Long> {
    
}
