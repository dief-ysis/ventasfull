package ventas.usuario.repository;

import ventas.usuario.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Usuario, Long> {
    // Métodos para filtros (al menos 3 endpoints con parámetros de consulta)
    Optional<Usuario> findByUsername(String username);
    List<Usuario> findByEmailContainingIgnoreCase(String email);
    List<Usuario> findByRole(String role);
    List<Usuario> findByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCase(String firstName, String lastName);
}