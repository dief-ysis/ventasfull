package ventas.usuario.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario extends RepresentationModel<Usuario> {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username es requerido")
    @Size(min = 3, max = 20, message = "Username debe tener entre 3 y 20 caracteres")
    @Column(nullable = false, unique = true)
    private String username;

    @NotBlank(message = "Password es requerido")
    @Size(min = 8, message = "Password debe tener al menos 8 caracteres")
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "Email es requerido")
    @Email(message = "Email debe ser válido")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Nombre es requerido")
    @Size(min = 2, max = 50, message = "Nombre debe tener entre 2 y 50 caracteres")
    @Column(nullable = false)
    private String firstName;

    @NotBlank(message = "Apellido es requerido")
    @Size(min = 2, max = 50, message = "Apellido debe tener entre 2 y 50 caracteres")
    @Column(nullable = false)
    private String lastName;

    private String address;
    private String phone;
    private boolean enabled = true;
    private String role = "ROLE_USER";
}