package com.esucar.api.repo;

import com.esucar.api.model.Progreso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProgresoRepo extends JpaRepository<Progreso, Long> {
    @Query("select p.materialId from Progreso p where p.usuario = :usuario")
    List<Integer> materialesRevisados(@Param("usuario") String usuario);

    boolean existsByUsuarioAndMaterialId(String usuario, Integer materialId);

    void deleteByUsuarioAndMaterialId(String usuario, Integer materialId);
}
