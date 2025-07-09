package ventas.usuario.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode; 
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel; 
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario extends RepresentationModel<Usuario> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    @JsonIgnore 
    private String password;

    @Column(nullable = false)
    @Email
    private String email;

    @Column(nullable = false)
    @Size(min = 2)
    private String firstName;

    @Column(nullable = false)
    @Size(min = 2)
    private String lastName;

    private String address;
    
    private String phone;
    
    private boolean enabled = true; 
    
    private String role = "ROLE_USER"; 
}