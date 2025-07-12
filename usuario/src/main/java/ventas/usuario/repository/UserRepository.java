package ventas.usuario.repository;

import ventas.usuario.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
    List<Usuario> findByEmailContainingIgnoreCase(String email);
    Optional<Usuario> findByEmail(String email);
    List<Usuario> findByRole(String role);
    
    @Query("SELECT u FROM Usuario u WHERE LOWER(u.firstName) LIKE LOWER(concat('%', :firstName,'%')) " +
           "AND LOWER(u.lastName) LIKE LOWER(concat('%', :lastName,'%'))")
    List<Usuario> findByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCase(
            @Param("firstName") String firstName, 
            @Param("lastName") String lastName);
}