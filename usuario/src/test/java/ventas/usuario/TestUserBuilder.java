package ventas.usuario;

import net.datafaker.Faker;
import ventas.usuario.model.Usuario;

public class TestUserBuilder {
    private static final Faker faker = new Faker();
    
    public static Usuario buildValidUser() {
        Usuario user = new Usuario();
        user.setUsername("testuser_" + System.currentTimeMillis());
        user.setPassword("ValidPass123!");
        user.setEmail("test_" + System.currentTimeMillis() + "@example.com");
        user.setFirstName(faker.name().firstName());
        user.setLastName(faker.name().lastName());
        user.setEnabled(true);
        user.setRole("ROLE_USER");
        return user;
    }
    
    public static Usuario buildUserWithShortPassword() {
        Usuario user = buildValidUser();
        user.setPassword("short");
        return user;
    }
}
