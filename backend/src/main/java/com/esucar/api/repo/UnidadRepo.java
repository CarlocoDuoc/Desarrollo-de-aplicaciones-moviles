package com.esucar.api.repo;

import com.esucar.api.model.Unidad;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UnidadRepo extends JpaRepository<Unidad, Integer> {
    List<Unidad> findAllByOrderByAsignaturaIdAscNumeroAsc();
}
