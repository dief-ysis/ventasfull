package ventas.usuario.service;

import ventas.usuario.model.Usuario;
import ventas.usuario.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; 

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<Usuario> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public Usuario createUser(Usuario user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        System.out.println("Creando usuario: " + user.getUsername());
        return userRepository.save(user);
    }

    @Transactional
    public Optional<Usuario> updateUser(Long id, Usuario userDetails) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    existingUser.setUsername(userDetails.getUsername());
                    if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
                        existingUser.setPassword(passwordEncoder.encode(userDetails.getPassword()));
                    }
                    existingUser.setEmail(userDetails.getEmail());
                    existingUser.setFirstName(userDetails.getFirstName());
                    existingUser.setLastName(userDetails.getLastName());
                    existingUser.setAddress(userDetails.getAddress());
                    existingUser.setPhone(userDetails.getPhone());
                    existingUser.setEnabled(userDetails.isEnabled());
                    existingUser.setRole(userDetails.getRole());
                    System.out.println("Actualizando usuario: " + existingUser.getUsername());
                    return userRepository.save(existingUser);
                });
    }

    @Transactional
    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            System.out.println("Eliminando usuario con ID: " + id);
            userRepository.deleteById(id);
            return true;
        }
        return false; 
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public List<Usuario> getUsersByEmail(String email) {
        return userRepository.findByEmailContainingIgnoreCase(email);
    }

    @Transactional(readOnly = true)
    public List<Usuario> getUsersByRole(String role) {
        return userRepository.findByRole(role);
    }

    @Transactional(readOnly = true)
    public List<Usuario> getUsersByFullName(String firstName, String lastName) {
        return userRepository.findByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCase(firstName, lastName);
    }
}