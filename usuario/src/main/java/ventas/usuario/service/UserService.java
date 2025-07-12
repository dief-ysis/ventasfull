package ventas.usuario.service;

import ventas.usuario.model.Usuario;
import ventas.usuario.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<Usuario> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Usuario> getAllUsersWithFilters(String username, String email, String role, String fullName) {
        if (username != null) {
            return userRepository.findByUsername(username).map(List::of).orElse(List.of());
        } else if (email != null) {
            return userRepository.findByEmailContainingIgnoreCase(email);
        } else if (role != null) {
            return userRepository.findByRole(role);
        } else if (fullName != null && fullName.contains(" ")) {
            String[] parts = fullName.split(" ", 2);
            return userRepository.findByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCase(parts[0], parts[1]);
        } else {
            return getAllUsers();
        }
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Transactional
    public Usuario createUser(Usuario user) {
        // Validaciones más robustas
        if (user == null) {
            throw new IllegalArgumentException("Usuario no puede ser nulo");
        }
        
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username es requerido");
        }
        
        if (user.getPassword() == null || user.getPassword().length() < 8) {
            throw new IllegalArgumentException("Password debe tener al menos 8 caracteres");
        }
        
        if (user.getEmail() == null || !user.getEmail().matches(".+@.+\\..+")) {
            throw new IllegalArgumentException("Email debe ser válido");
        }
        
        if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("Nombre es requerido");
        }
        
        if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Apellido es requerido");
        }
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Transactional
    public Optional<Usuario> updateUser(Long id, Usuario userDetails) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    if (userDetails.getUsername() != null) {
                        if (!userDetails.getUsername().equals(existingUser.getUsername()) && 
                            existsByUsername(userDetails.getUsername())) {
                            throw new IllegalArgumentException("El nombre de usuario ya existe");
                        }
                        existingUser.setUsername(userDetails.getUsername());
                    }
                    
                    if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
                        existingUser.setPassword(passwordEncoder.encode(userDetails.getPassword()));
                    }
                    
                    if (userDetails.getEmail() != null) {
                        if (!userDetails.getEmail().equals(existingUser.getEmail()) && 
                            existsByEmail(userDetails.getEmail())) {
                            throw new IllegalArgumentException("El email ya está registrado");
                        }
                        existingUser.setEmail(userDetails.getEmail());
                    }
                    
                    if (userDetails.getFirstName() != null) {
                        existingUser.setFirstName(userDetails.getFirstName());
                    }
                    
                    if (userDetails.getLastName() != null) {
                        existingUser.setLastName(userDetails.getLastName());
                    }
                    
                    if (userDetails.getAddress() != null) {
                        existingUser.setAddress(userDetails.getAddress());
                    }
                    
                    if (userDetails.getPhone() != null) {
                        existingUser.setPhone(userDetails.getPhone());
                    }
                    
                    if (userDetails.getRole() != null) {
                        existingUser.setRole(userDetails.getRole());
                    }
                    
                    existingUser.setEnabled(userDetails.isEnabled());
                    return userRepository.save(existingUser);
                });
    }

    @Transactional
    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }
}