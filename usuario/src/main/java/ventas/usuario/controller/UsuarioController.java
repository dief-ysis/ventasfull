package ventas.usuario.controller;

import ventas.usuario.model.Usuario;
import ventas.usuario.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.DataIntegrityViolationException; 

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "API para la gestión de usuarios del sistema de ventas")
public class UsuarioController {

    @Autowired
    private UserService userService;

    private EntityModel<Usuario> toEntityModel(Usuario usuario) {
        return EntityModel.of(usuario,
                linkTo(methodOn(UsuarioController.class).getUserById(usuario.getId())).withSelfRel(),
                linkTo(methodOn(UsuarioController.class).getAllUsers(null, null, null, null)).withRel("usuarios")
        );
    }

    private CollectionModel<EntityModel<Usuario>> toCollectionModel(List<Usuario> usuarios) {
        List<EntityModel<Usuario>> userModels = usuarios.stream()
                .map((Usuario u) -> this.toEntityModel(u))
                .collect(Collectors.toList());
        return CollectionModel.of(userModels,
                linkTo(methodOn(UsuarioController.class).getAllUsers(null, null, null, null)).withSelfRel()
        );
    }

    @Operation(summary = "Obtener todos los usuarios o filtrar por criterios",
               description = "Permite obtener una lista de todos los usuarios. " +
                             "Se pueden aplicar filtros por username, email, rol o nombre completo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida exitosamente",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Usuario.class)))),
            @ApiResponse(responseCode = "204", description = "No se encontraron usuarios que coincidan con los criterios (No Content)"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Usuario>>> getAllUsers(
            @Parameter(description = "Filtrar por nombre de usuario exacto") @RequestParam(required = false) String username,
            @Parameter(description = "Filtrar por parte del email (ignorando mayúsculas/minúsculas)") @RequestParam(required = false) String email,
            @Parameter(description = "Filtrar por rol") @RequestParam(required = false) String role,
            @Parameter(description = "Filtrar por parte del nombre y apellido") @RequestParam(required = false) String fullName
    ) {
        List<Usuario> usuarios;
        if (username != null) {
            usuarios = userService.getUserByUsername(username).map(List::of).orElse(List.of());
        } else if (email != null) {
            usuarios = userService.getUsersByEmail(email);
        } else if (role != null) {
            usuarios = userService.getUsersByRole(role);
        } else if (fullName != null && fullName.contains(" ")) {
            String[] parts = fullName.split(" ", 2);
            usuarios = userService.getUsersByFullName(parts[0], parts[1]);
        } else {
            usuarios = userService.getAllUsers();
        }

        if (usuarios.isEmpty()) {
            return ResponseEntity.noContent().build(); 
        }
        return ResponseEntity.ok(toCollectionModel(usuarios));
    }

    @Operation(summary = "Obtener un usuario por ID", description = "Obtiene los detalles de un usuario específico por su ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado", content = @Content(schema = @Schema(implementation = Usuario.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Usuario>> getUserById(
            @Parameter(description = "ID del usuario a obtener", required = true) @PathVariable Long id
    ) {
        return userService.getUserById(id)
                .map(usuario -> ResponseEntity.ok(toEntityModel(usuario)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Crear un nuevo usuario", description = "Crea un nuevo usuario en el sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente", content = @Content(schema = @Schema(implementation = Usuario.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "409", description = "Conflicto: el nombre de usuario o correo electrónico ya existe"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping
    public ResponseEntity<EntityModel<Usuario>> createUser(
            @Parameter(description = "Detalles del usuario a crear", required = true) @RequestBody Usuario usuario
    ) {
        try {
            Usuario createdUser = userService.createUser(usuario);
            Link selfLink = linkTo(methodOn(UsuarioController.class).getUserById(createdUser.getId())).withSelfRel();
            return ResponseEntity.created(selfLink.toUri()).body(toEntityModel(createdUser));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); 
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); 
        }
    }

    @Operation(summary = "Actualizar un usuario existente", description = "Actualiza los detalles de un usuario por su ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario actualizado exitosamente", content = @Content(schema = @Schema(implementation = Usuario.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "409", description = "Conflicto: el nombre de usuario o correo electrónico ya existe"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Usuario>> updateUser(
            @Parameter(description = "ID del usuario a actualizar", required = true) @PathVariable("id") Long id,
            @Parameter(description = "Detalles del usuario para actualizar", required = true) @RequestBody Usuario userDetails
    ) {
        try {
            return userService.updateUser(id, userDetails)
                    .map(usuario -> ResponseEntity.ok(toEntityModel(usuario)))
                    .orElse(ResponseEntity.notFound().build()); 
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); 
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); 
        }
    }

    @Operation(summary = "Eliminar un usuario", description = "Elimina un usuario del sistema por su ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuario eliminado exitosamente (No Content)"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(
            @Parameter(description = "ID del usuario a eliminar", required = true) @PathVariable("id") Long id
    ) {
        if (userService.deleteUser(id)) {
            return ResponseEntity.noContent().build(); 
        } else {
            return ResponseEntity.notFound().build(); 
        }
    }
}