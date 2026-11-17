package com.esucar.api.web;

import com.esucar.api.model.Usuario;
import com.esucar.api.repo.UsuarioRepo;
import com.esucar.api.web.Dtos.*;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioRepo usuarios;
    private final PasswordEncoder encoder;

    public AuthController(UsuarioRepo usuarios, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.encoder = encoder;
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioRes registro(@Valid @RequestBody RegistroReq req) {
        String nombre = req.nombre().trim();
        if (usuarios.existsByNombreNormalizado(nombre.toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese nombre de usuario ya existe");
        }
        try {
            Usuario u = usuarios.save(new Usuario(nombre, encoder.encode(req.clave()), req.rol()));
            return UsuarioRes.de(u);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese nombre de usuario ya existe");
        }
    }

    @PostMapping("/login")
    public UsuarioRes login(@Valid @RequestBody LoginReq req) {
        Usuario u = usuarios.findByNombreNormalizado(req.nombre().trim().toLowerCase())
                .filter(x -> encoder.matches(req.clave(), x.getClaveHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Nombre de usuario o contraseña incorrectos"));
        return UsuarioRes.de(u);
    }
}
