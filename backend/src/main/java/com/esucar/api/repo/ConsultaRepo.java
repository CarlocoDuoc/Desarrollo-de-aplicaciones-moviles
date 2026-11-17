package com.esucar.api.repo;

import com.esucar.api.model.Consulta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ConsultaRepo extends JpaRepository<Consulta, Integer> {
    List<Consulta> findAllByOrderByIdAsc();
    List<Consulta> findAllByEmisorIgnoreCaseOrderByIdAsc(String emisor);
}
