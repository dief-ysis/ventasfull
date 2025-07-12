package ventas.usuario.controller;

import ventas.usuario.model.Usuario;
import ventas.usuario.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Collections;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "API para la gestión de usuarios del sistema de ventas")
public class UsuarioController {

    @Autowired
    private UserService userService;

    private EntityModel<Usuario> toEntityModel(Usuario usuario) {
        return EntityModel.of(usuario,
                linkTo(methodOn(UsuarioController.class).getUserById(usuario.getId())).withSelfRel(),
                linkTo(methodOn(UsuarioController.class).getAllUsers(null, null, null, null)).withRel("usuarios"),
                linkTo(methodOn(UsuarioController.class).updateUser(usuario.getId(), null)).withRel("update"),
                linkTo(methodOn(UsuarioController.class).deleteUser(usuario.getId())).withRel("delete")
        );
    }

    private CollectionModel<EntityModel<Usuario>> toCollectionModel(List<Usuario> usuarios) {
        List<EntityModel<Usuario>> userModels = usuarios.stream()
                .map(user -> EntityModel.of(user,
                        linkTo(methodOn(UsuarioController.class).getUserById(user.getId())).withSelfRel(),
                        linkTo(methodOn(UsuarioController.class).updateUser(user.getId(), null)).withRel("update"),
                        linkTo(methodOn(UsuarioController.class).deleteUser(user.getId())).withRel("delete")
                ))
                .collect(Collectors.toList());
        
        return CollectionModel.of(userModels,
                linkTo(methodOn(UsuarioController.class).getAllUsers(null, null, null, null)).withSelfRel()
        );
    }

    @Operation(summary = "Obtener todos los usuarios o filtrar por criterios")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida exitosamente"),
            @ApiResponse(responseCode = "204", description = "No se encontraron usuarios")
    })
    @GetMapping
    public ResponseEntity<?> getAllUsers(
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "fullName", required = false) String fullName) {
        
        List<Usuario> usuarios = userService.getAllUsersWithFilters(username, email, role, fullName);
        
        if (usuarios.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        
        List<Map<String, Object>> response = usuarios.stream()
                .map(user -> {
                    Map<String, Object> userMap = new LinkedHashMap<>();
                    userMap.put("id", user.getId());
                    userMap.put("username", user.getUsername());
                    userMap.put("email", user.getEmail());
                    userMap.put("firstName", user.getFirstName() != null ? user.getFirstName() : "");
                    userMap.put("lastName", user.getLastName() != null ? user.getLastName() : "");
                    userMap.put("enabled", user.isEnabled());
                    userMap.put("role", user.getRole() != null ? user.getRole() : "");
                    return userMap;
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "Obtener un usuario por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable("id") Long id) {
        try {
            Optional<Usuario> userOptional = userService.getUserById(id);
            
            if (userOptional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Usuario user = userOptional.get();
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("firstName", user.getFirstName() != null ? user.getFirstName() : "");
            response.put("lastName", user.getLastName() != null ? user.getLastName() : "");
            response.put("enabled", user.isEnabled());
            response.put("role", user.getRole() != null ? user.getRole() : "");
            response.put("address", user.getAddress() != null ? user.getAddress() : "");
            response.put("phone", user.getPhone() != null ? user.getPhone() : "");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al procesar la solicitud"));
        }
    }

    @Operation(summary = "Crear un nuevo usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "409", description = "Conflicto: usuario o email ya existen"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody Usuario usuario, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> 
                errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errors);
        }
        
        if (usuario.getPassword().length() < 8) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Password debe tener al menos 8 caracteres"));
        }

        try {
            if (userService.existsByUsername(usuario.getUsername())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "El nombre de usuario ya existe"));
            }
            
            if (userService.existsByEmail(usuario.getEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "El email ya está registrado"));
            }
            
            Usuario createdUser = userService.createUser(usuario);
            return ResponseEntity
                    .created(linkTo(methodOn(UsuarioController.class).getUserById(createdUser.getId())).toUri())
                    .body(toEntityModel(createdUser));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Conflicto de datos: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error interno del servidor"));
        }
    }

    @Operation(summary = "Actualizar un usuario existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "409", description = "Conflicto: usuario o email ya existen"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable("id") Long id,
            @RequestBody Usuario userDetails) {
        
        try {
            Optional<Usuario> existingUser = userService.getUserById(id);
            if (existingUser.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            if (userDetails.getUsername() != null && 
                !userDetails.getUsername().equals(existingUser.get().getUsername()) &&
                userService.existsByUsername(userDetails.getUsername())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "El nombre de usuario ya está en uso"));
            }
            
            if (userDetails.getEmail() != null && 
                !userDetails.getEmail().equals(existingUser.get().getEmail()) &&
                userService.existsByEmail(userDetails.getEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "El email ya está registrado"));
            }
            
            return userService.updateUser(id, userDetails)
                    .map(this::toEntityModel)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
                    
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al actualizar el usuario"));
        }
    }

    @Operation(summary = "Eliminar un usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuario eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable("id") Long id) {
        try {
            if (!userService.getUserById(id).isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            if (userService.deleteUser(id)) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "No se pudo eliminar el usuario"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al eliminar el usuario"));
        }
    }
}