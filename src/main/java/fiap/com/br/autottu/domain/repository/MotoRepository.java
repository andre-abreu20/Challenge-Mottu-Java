package fiap.com.br.autottu.domain.repository;

import fiap.com.br.autottu.domain.model.Moto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MotoRepository extends JpaRepository<Moto,Integer> {
    Optional<Moto> findByPlaca(String placa);

    List<Moto> findBySlotIsNull();
}

