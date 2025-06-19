package ventas.usuario;

import ventas.usuario.model.Usuario;
import ventas.usuario.repository.UserRepository;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import java.util.Random;

@Profile("dev")
@Component
public class DataLoader implements CommandLineRunner{
    @Autowired
    private UserRepository usuarioRepository;
    
    public void run(String... args) throws Exception {
        Faker faker = new Faker();
        Random random = new Random();
        for (int i = 0; i < 25; i++) {
            Usuario usuario = new Usuario();
            usuario.setUsername(faker.name().username());
            usuario.setPassword(faker.internet().password());
            usuario.setEmail(faker.internet().emailAddress());
            usuario.setFirstName(faker.name().firstName());
            usuario.setLastName(faker.name().lastName());
            usuario.setAddress(faker.address().fullAddress());
            usuario.setPhone(faker.phoneNumber().phoneNumber());
            usuario.setEnabled(random.nextBoolean());
            usuario.setRole("ROLE_USER");

            try {
                usuarioRepository.save(usuario);
            } catch (org.springframework.dao.DataIntegrityViolationException e) {
                System.err.println("Data integrity violation occurred: " + e.getMessage());
            }

        }

    }
    
}
