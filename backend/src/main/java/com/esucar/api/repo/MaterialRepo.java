package com.esucar.api.repo;

import com.esucar.api.model.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MaterialRepo extends JpaRepository<Material, Integer> {
    List<Material> findAllByOrderByIdAsc();
}
