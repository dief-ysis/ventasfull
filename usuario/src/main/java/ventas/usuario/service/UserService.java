package ventas.usuario.service;

import ventas.usuario.model.Usuario;
import ventas.usuario.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<Usuario> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<Usuario> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Usuario createUser(Usuario user) {
        System.out.println("Creando usuario: " + user.getUsername());
        return userRepository.save(user);
    }

    public Usuario updateUser(Long id, Usuario userDetails) {
        Usuario user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        user.setUsername(userDetails.getUsername());
        user.setPassword(userDetails.getPassword());
        user.setEmail(userDetails.getEmail());
        user.setFirstName(userDetails.getFirstName());
        user.setLastName(userDetails.getLastName());
        user.setAddress(userDetails.getAddress());
        user.setPhone(userDetails.getPhone());
        user.setEnabled(userDetails.isEnabled());
        user.setRole(userDetails.getRole());

        System.out.println("Actualizando usuario: " + user.getUsername());
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        System.out.println("Eliminando usuario con ID: " + id);
        userRepository.deleteById(id);
    }
}