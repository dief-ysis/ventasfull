package ventas.usuario;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ventas.usuario.model.Usuario;
import ventas.usuario.repository.UserRepository;
import ventas.usuario.service.UserService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private UserService userService;

    @Test
    void getUserById_ShouldReturnUser_WhenUserExists() {
        // Configurar usuario de prueba con todos los campos
        Usuario user = new Usuario();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("password");
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        
        Optional<Usuario> result = userService.getUserById(1L);
        
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getUsername()).isEqualTo("testuser");
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void createUser_ShouldEncodePassword() {
        // Configurar usuario de prueba con todos los campos requeridos
        Usuario user = new Usuario();
        user.setUsername("testuser");
        user.setPassword("plainpassword");
        user.setEmail("test@example.com"); // Email válido
        user.setFirstName("Test");
        user.setLastName("User");
        
        // Configurar mocks
        when(passwordEncoder.encode("plainpassword")).thenReturn("hashedpassword");
        when(userRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        
        // Ejecutar
        Usuario created = userService.createUser(user);
        
        // Verificar
        assertThat(created.getPassword()).isEqualTo("hashedpassword");
        verify(passwordEncoder).encode("plainpassword");
    }

    @Test
    void createUser_ShouldThrowException_WhenPasswordTooShort() {
        Usuario user = new Usuario();
        user.setUsername("testuser");
        user.setPassword("short");
        
        assertThatThrownBy(() -> userService.createUser(user))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Password debe tener al menos 8 caracteres");
    }

    @Test
    void updateUser_ShouldUpdateFields() {
        Usuario existingUser = new Usuario();
        existingUser.setId(1L);
        existingUser.setUsername("olduser");
        
        Usuario updateData = new Usuario();
        updateData.setUsername("newuser");
        updateData.setFirstName("New");
        updateData.setLastName("Name");
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        
        Optional<Usuario> result = userService.updateUser(1L, updateData);
        
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("newuser");
        assertThat(result.get().getFirstName()).isEqualTo("New");
        assertThat(result.get().getLastName()).isEqualTo("Name");
    }

    @Test
    void deleteUser_ShouldReturnTrue_WhenUserExists() {
        when(userRepository.existsById(1L)).thenReturn(true);
        
        boolean result = userService.deleteUser(1L);
        
        assertThat(result).isTrue();
        verify(userRepository).deleteById(1L);
    }
}