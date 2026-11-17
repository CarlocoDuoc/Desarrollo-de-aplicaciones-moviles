package com.esucar.api.repo;

import com.esucar.api.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepo extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByNombreNormalizado(String nombreNormalizado);
    boolean existsByNombreNormalizado(String nombreNormalizado);
}
