package ventas.usuario;

import ventas.usuario.model.Usuario;
import ventas.usuario.repository.UserRepository;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.dao.DataIntegrityViolationException; // Importar DataIntegrityViolationException
import java.util.Random;

@Profile("dev") // Esto asegura que solo se ejecuta en el perfil 'dev'
@Component
public class DataLoader implements CommandLineRunner{
    @Autowired
    private UserRepository usuarioRepository;

    @Override // Añadir @Override
    public void run(String... args) throws Exception {
        // No vaciamos la base de datos aquí, eso es tarea del test profile con ddl-auto=create-drop

        Faker faker = new Faker();
        Random random = new Random();
        int createdCount = 0;
        System.out.println("Cargando 10 usuarios de ejemplo en el perfil DEV...");
        while (createdCount < 10) { // Aseguramos al menos 10 registros
            Usuario usuario = new Usuario();
            usuario.setUsername(faker.name().username() + random.nextInt(10000)); // Para evitar duplicados en username
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
                createdCount++;
            } catch (DataIntegrityViolationException e) {
                System.err.println("Advertencia: No se pudo crear el usuario (posible duplicado de username/email): " + e.getMessage());
                // Continuar intentando crear nuevos usuarios
            }
        }
        System.out.println("Carga de usuarios de ejemplo finalizada.");
    }
}